package com.itops.modules.system.controller;

import com.itops.common.api.R;
import com.itops.modules.system.annotation.OperLog;
import com.itops.modules.system.entity.SysConfig;
import com.itops.modules.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "系统管理-参数配置")
@RestController
@RequestMapping("/api/system/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService configService;

    @PreAuthorize("hasAuthority('system:config:list')")
    @GetMapping("/list")
    public R<List<SysConfig>> list(@RequestParam(required = false) String configName,
                                   @RequestParam(required = false) String configKey) {
        return R.ok(configService.listAll(configName, configKey));
    }

    @GetMapping("/value/{configKey}")
    public R<String> value(@PathVariable String configKey) {
        return R.ok(configService.getValue(configKey));
    }

    @OperLog(title = "参数配置", businessType = 1)
    @PreAuthorize("hasAuthority('system:config:add')")
    @PostMapping
    public R<Void> create(@RequestBody SysConfig config) {
        configService.save(config);
        return R.ok();
    }

    @OperLog(title = "参数配置", businessType = 2)
    @PreAuthorize("hasAuthority('system:config:edit')")
    @PutMapping
    public R<Void> update(@RequestBody SysConfig config) {
        configService.updateById(config);
        return R.ok();
    }

    @OperLog(title = "参数配置", businessType = 3)
    @PreAuthorize("hasAuthority('system:config:remove')")
    @DeleteMapping("/{configId}")
    public R<Void> remove(@PathVariable Long configId) {
        configService.removeById(configId);
        return R.ok();
    }
}
