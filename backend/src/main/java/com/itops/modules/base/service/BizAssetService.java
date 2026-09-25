package com.itops.modules.base.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.modules.base.entity.BizAsset;
import com.itops.modules.base.mapper.BizAssetMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BizAssetService extends ServiceImpl<BizAssetMapper, BizAsset> {

    public IPage<BizAsset> page(long pageNum, long pageSize, String keyword,
                                String category, String status, Long deptId) {
        return page(new Page<>(pageNum, pageSize), Wrappers.<BizAsset>lambdaQuery()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(BizAsset::getAssetNo, keyword)
                        .or().like(BizAsset::getAssetName, keyword)
                        .or().like(BizAsset::getBrand, keyword)
                        .or().like(BizAsset::getModel, keyword))
                .eq(StringUtils.hasText(category), BizAsset::getCategory, category)
                .eq(StringUtils.hasText(status), BizAsset::getStatus, status)
                .eq(deptId != null, BizAsset::getDeptId, deptId)
                .orderByDesc(BizAsset::getAssetId));
    }
}
