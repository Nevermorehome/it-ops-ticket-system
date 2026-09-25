package com.itops.modules.system.controller;

import com.itops.common.api.R;
import com.itops.modules.system.annotation.OperLog;
import com.itops.modules.system.entity.SysDictData;
import com.itops.modules.system.entity.SysDictType;
import com.itops.modules.system.service.SysDictService;
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

@Tag(name = "系统管理-字典")
@RestController
@RequestMapping("/api/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService dictService;

    // -------- 类型 --------

    @Operation(summary = "字典类型列表")
    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/type/list")
    public R<List<SysDictType>> typeList(@RequestParam(required = false) String dictName,
                                         @RequestParam(required = false) String dictType) {
        return R.ok(dictService.typeList(dictName, dictType));
    }

    @OperLog(title = "字典类型", businessType = 1)
    @PreAuthorize("hasAuthority('system:dict:add')")
    @PostMapping("/type")
    public R<Void> createType(@RequestBody SysDictType type) {
        dictService.createType(type);
        return R.ok();
    }

    @OperLog(title = "字典类型", businessType = 2)
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @PutMapping("/type")
    public R<Void> updateType(@RequestBody SysDictType type) {
        dictService.updateType(type);
        return R.ok();
    }

    @OperLog(title = "字典类型", businessType = 3)
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @DeleteMapping("/type/{dictId}")
    public R<Void> removeType(@PathVariable Long dictId) {
        dictService.removeType(dictId);
        return R.ok();
    }

    // -------- 数据 --------

    @Operation(summary = "字典数据列表")
    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/data/list")
    public R<List<SysDictData>> dataList(@RequestParam(required = false) String dictType,
                                         @RequestParam(required = false) String dictLabel,
                                         @RequestParam(required = false) String status) {
        return R.ok(dictService.dataList(dictType, dictLabel, status));
    }

    @Operation(summary = "按类型编码获取字典项(前端通用)")
    @GetMapping("/data/type/{dictType}")
    public R<List<SysDictData>> dataByType(@PathVariable String dictType) {
        return R.ok(dictService.dataList(dictType, null, "0"));
    }

    @OperLog(title = "字典数据", businessType = 1)
    @PreAuthorize("hasAuthority('system:dict:add')")
    @PostMapping("/data")
    public R<Void> createData(@RequestBody SysDictData data) {
        dictService.createData(data);
        return R.ok();
    }

    @OperLog(title = "字典数据", businessType = 2)
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @PutMapping("/data")
    public R<Void> updateData(@RequestBody SysDictData data) {
        dictService.updateData(data);
        return R.ok();
    }

    @OperLog(title = "字典数据", businessType = 3)
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @DeleteMapping("/data/{dictCode}")
    public R<Void> removeData(@PathVariable Long dictCode) {
        dictService.removeData(dictCode);
        return R.ok();
    }
}
