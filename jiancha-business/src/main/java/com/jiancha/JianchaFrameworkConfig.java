package com.jiancha;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 纪检监察业务模块框架配置。
 *
 * <p>主应用 {@code RuoYiApplication} 已通过 {@code scanBasePackages} 纳入 {@code com.jiancha}，
 * 这里补上 MyBatis 的 Mapper 扫描（框架默认的 {@code @MapperScan} 只覆盖 {@code com.ruoyi}）。</p>
 *
 * @author 小标
 */
@Configuration
@MapperScan("com.jiancha.**.mapper")
public class JianchaFrameworkConfig {
}
