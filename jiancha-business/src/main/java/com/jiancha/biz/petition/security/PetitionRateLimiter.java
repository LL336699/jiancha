package com.jiancha.biz.petition.security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.StringUtils;

/**
 * 举报箱提交限流器（单 IP 滑动窗口）
 *
 * <p>设计文档 §M3 安全设计要求「单会话 ≤5 请求/秒」：免登录入口必须防刷频，
 * 否则既是垃圾举报温床也是存储耗尽风险。实现为进程内滑动窗口，key 取客户端 IP，
 * 窗口长度与阈值均可配（{@code jiancha.petition.rate-limit.*}）。</p>
 *
 * <p>已知边界：多实例部署时限流为单机口径；如需集群级限流，后续可换 Redis 计数。</p>
 *
 * @author 小标
 */
@Component
public class PetitionRateLimiter {

    /** key -> 窗口内请求时间戳（毫秒） */
    private final Map<String, Deque<Long>> windows = new ConcurrentHashMap<>();

    /** 窗口长度（毫秒） */
    private final long windowMillis;

    /** 窗口内允许的最大请求数 */
    private final int maxRequests;

    public PetitionRateLimiter(
            @Value("${jiancha.petition.rate-limit.window-seconds:60}") long windowSeconds,
            @Value("${jiancha.petition.rate-limit.max-requests:5}") int maxRequests) {
        this.windowMillis = Math.max(1, windowSeconds) * 1000L;
        this.maxRequests = Math.max(1, maxRequests);
    }

    /**
     * 尝试放行一次请求。
     *
     * @param key 限流键（一般为客户端 IP）
     * @return true 放行；false 超出频率，应拒绝
     */
    public boolean tryAcquire(String key) {
        String k = StringUtils.isEmpty(key) ? "unknown" : key;
        long now = System.currentTimeMillis();
        Deque<Long> deque = windows.computeIfAbsent(k, x -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && now - deque.peekFirst() > windowMillis) {
                deque.pollFirst();
            }
            if (deque.size() >= maxRequests) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }

    /** 当前窗口内的请求数（供监控/测试） */
    public int currentCount(String key) {
        Deque<Long> deque = windows.get(key);
        if (deque == null) {
            return 0;
        }
        synchronized (deque) {
            long now = System.currentTimeMillis();
            while (!deque.isEmpty() && now - deque.peekFirst() > windowMillis) {
                deque.pollFirst();
            }
            return deque.size();
        }
    }
}
