package com.itops.modules.base.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.modules.base.entity.BizLocation;
import com.itops.modules.base.mapper.BizLocationMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class BizLocationService extends ServiceImpl<BizLocationMapper, BizLocation> {

    public List<BizLocation> listAll(String name) {
        return list(Wrappers.<BizLocation>lambdaQuery()
                .like(StringUtils.hasText(name), BizLocation::getLocationName, name)
                .eq(BizLocation::getStatus, "0")
                .orderByDesc(BizLocation::getLocationId));
    }
}
