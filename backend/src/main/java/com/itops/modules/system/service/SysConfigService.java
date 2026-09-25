package com.itops.modules.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.modules.system.entity.SysConfig;
import com.itops.modules.system.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> {

    public List<SysConfig> listAll(String configName, String configKey) {
        return list(Wrappers.<SysConfig>lambdaQuery()
                .like(configName != null && !configName.isBlank(), SysConfig::getConfigName, configName)
                .like(configKey != null && !configKey.isBlank(), SysConfig::getConfigKey, configKey)
                .orderByDesc(SysConfig::getConfigId));
    }

    public String getValue(String key) {
        SysConfig config = getOne(Wrappers.<SysConfig>lambdaQuery().eq(SysConfig::getConfigKey, key), false);
        return config == null ? null : config.getConfigValue();
    }
}
