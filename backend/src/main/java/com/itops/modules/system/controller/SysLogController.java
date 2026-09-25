package com.itops.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itops.common.api.R;
import com.itops.modules.system.annotation.OperLog;
import com.itops.modules.system.entity.SysLoginLog;
import com.itops.modules.system.entity.SysOperationLog;
import com.itops.modules.system.service.SysLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统管理-日志")
@RestController
@RequestMapping("/api/system/log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLogService logService;

    @PreAuthorize("hasAuthority('system:loginlog:list')")
    @GetMapping("/login/page")
    public R<IPage<SysLoginLog>> loginPage(@RequestParam(defaultValue = "1") long pageNum,
                                           @RequestParam(defaultValue = "10") long pageSize,
                                           @RequestParam(required = false) String username,
                                           @RequestParam(required = false) String status) {
        return R.ok(logService.loginLogPage(pageNum, pageSize, username, status));
    }

    @OperLog(title = "登录日志", businessType = 3)
    @PreAuthorize("hasAuthority('system:loginlog:list')")
    @DeleteMapping("/login/clear")
    public R<Void> clearLogin() {
        logService.clearLoginLog();
        return R.ok();
    }

    @PreAuthorize("hasAuthority('system:operlog:list')")
    @GetMapping("/operation/page")
    public R<IPage<SysOperationLog>> operationPage(@RequestParam(defaultValue = "1") long pageNum,
                                                   @RequestParam(defaultValue = "10") long pageSize,
                                                   @RequestParam(required = false) String title,
                                                   @RequestParam(required = false) String operName) {
        return R.ok(logService.operationLogPage(pageNum, pageSize, title, operName));
    }

    @OperLog(title = "操作日志", businessType = 3)
    @PreAuthorize("hasAuthority('system:operlog:list')")
    @DeleteMapping("/operation/clear")
    public R<Void> clearOperation() {
        logService.clearOperationLog();
        return R.ok();
    }
}
