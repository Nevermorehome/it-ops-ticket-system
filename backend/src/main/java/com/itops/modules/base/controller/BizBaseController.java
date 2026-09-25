package com.itops.modules.base.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itops.common.api.R;
import com.itops.modules.system.annotation.OperLog;
import com.itops.modules.base.entity.BizAsset;
import com.itops.modules.base.entity.BizCategory;
import com.itops.modules.base.entity.BizLocation;
import com.itops.modules.base.service.BizAssetService;
import com.itops.modules.base.service.BizCategoryService;
import com.itops.modules.base.service.BizLocationService;
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

/**
 * 基础数据: 工单分类 / 常用地点 / 资产设备
 */
@Tag(name = "基础数据")
@RestController
@RequestMapping("/api/base")
@RequiredArgsConstructor
public class BizBaseController {

    private final BizCategoryService categoryService;
    private final BizLocationService locationService;
    private final BizAssetService assetService;

    // ---------------- 分类 ----------------

    @GetMapping("/category/list")
    public R<List<BizCategory>> categoryList(@RequestParam(required = false) String name,
                                             @RequestParam(required = false) String status) {
        return R.ok(categoryService.listAll(name, status));
    }

    @GetMapping("/category/tree")
    public R<List<BizCategory>> categoryTree(@RequestParam(required = false) String name,
                                             @RequestParam(required = false) String status) {
        return R.ok(categoryService.tree(name, status));
    }

    @OperLog(title = "工单分类", businessType = 1)
    @PreAuthorize("hasAuthority('ticket:category:list')")
    @PostMapping("/category")
    public R<Void> createCategory(@RequestBody BizCategory category) {
        categoryService.create(category);
        return R.ok();
    }

    @OperLog(title = "工单分类", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:category:list')")
    @PutMapping("/category")
    public R<Void> updateCategory(@RequestBody BizCategory category) {
        categoryService.update(category);
        return R.ok();
    }

    @OperLog(title = "工单分类", businessType = 3)
    @PreAuthorize("hasAuthority('ticket:category:list')")
    @DeleteMapping("/category/{id}")
    public R<Void> removeCategory(@PathVariable Long id) {
        categoryService.remove(id);
        return R.ok();
    }

    // ---------------- 地点 ----------------

    @GetMapping("/location/list")
    public R<List<BizLocation>> locationList(@RequestParam(required = false) String name) {
        return R.ok(locationService.listAll(name));
    }

    @OperLog(title = "常用地点", businessType = 1)
    @PreAuthorize("hasAuthority('base:location:add')")
    @PostMapping("/location")
    public R<Void> createLocation(@RequestBody BizLocation location) {
        locationService.save(location);
        return R.ok();
    }

    @OperLog(title = "常用地点", businessType = 2)
    @PreAuthorize("hasAuthority('base:location:edit')")
    @PutMapping("/location")
    public R<Void> updateLocation(@RequestBody BizLocation location) {
        locationService.updateById(location);
        return R.ok();
    }

    @OperLog(title = "常用地点", businessType = 3)
    @PreAuthorize("hasAuthority('base:location:remove')")
    @DeleteMapping("/location/{id}")
    public R<Void> removeLocation(@PathVariable Long id) {
        locationService.removeById(id);
        return R.ok();
    }

    // ---------------- 资产 ----------------

    @GetMapping("/asset/page")
    public R<IPage<BizAsset>> assetPage(@RequestParam(defaultValue = "1") long pageNum,
                                        @RequestParam(defaultValue = "10") long pageSize,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) Long deptId) {
        return R.ok(assetService.page(pageNum, pageSize, keyword, category, status, deptId));
    }

    @OperLog(title = "资产设备", businessType = 1)
    @PreAuthorize("hasAuthority('base:asset:add')")
    @PostMapping("/asset")
    public R<Void> createAsset(@RequestBody BizAsset asset) {
        assetService.save(asset);
        return R.ok();
    }

    @OperLog(title = "资产设备", businessType = 2)
    @PreAuthorize("hasAuthority('base:asset:edit')")
    @PutMapping("/asset")
    public R<Void> updateAsset(@RequestBody BizAsset asset) {
        assetService.updateById(asset);
        return R.ok();
    }

    @OperLog(title = "资产设备", businessType = 3)
    @PreAuthorize("hasAuthority('base:asset:remove')")
    @DeleteMapping("/asset/{id}")
    public R<Void> removeAsset(@PathVariable Long id) {
        assetService.removeById(id);
        return R.ok();
    }
}
