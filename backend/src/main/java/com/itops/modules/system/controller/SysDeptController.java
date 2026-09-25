package com.itops.modules.system.controller;

import com.itops.common.api.R;
import com.itops.modules.system.annotation.OperLog;
import com.itops.modules.system.entity.SysDept;
import com.itops.modules.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "系统管理-部门")
@RestController
@RequestMapping("/api/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @Operation(summary = "部门列表(平铺)")
    @PreAuthorize("hasAuthority('system:dept:list')")
    @GetMapping("/list")
    public R<List<SysDept>> list(@RequestParam(required = false) String deptName,
                                 @RequestParam(required = false) String status) {
        return R.ok(deptService.listAll(deptName, status));
    }

    @Operation(summary = "部门树")
    @GetMapping("/tree")
    public R<List<SysDept>> tree(@RequestParam(required = false) String deptName,
                                 @RequestParam(required = false) String status) {
        return R.ok(deptService.tree(deptName, status));
    }

    @OperLog(title = "部门管理", businessType = 1)
    @PreAuthorize("hasAuthority('system:dept:add')")
    @PostMapping
    public R<Void> create(@RequestBody SysDept dept) {
        deptService.create(dept);
        return R.ok();
    }

    @OperLog(title = "部门管理", businessType = 2)
    @PreAuthorize("hasAuthority('system:dept:edit')")
    @PutMapping
    public R<Void> update(@RequestBody SysDept dept) {
        deptService.update(dept);
        return R.ok();
    }

    @OperLog(title = "部门管理", businessType = 3)
    @PreAuthorize("hasAuthority('system:dept:remove')")
    @DeleteMapping("/{deptId}")
    public R<Void> remove(@PathVariable Long deptId) {
        deptService.remove(deptId);
        return R.ok();
    }
}
