package com.jiancha.biz.edu.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.jiancha.biz.edu.service.IJcEduStatService;

/**
 * 廉政教育统计（M9）
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/edu/stat")
public class JcEduStatController extends BaseController {

    @Autowired
    private IJcEduStatService statService;

    @PreAuthorize("@ss.hasPermi('jiancha:edu:stat:list')")
    @GetMapping
    public AjaxResult stat() {
        Map<String, Object> stat = statService.stat();
        return success(stat);
    }
}
