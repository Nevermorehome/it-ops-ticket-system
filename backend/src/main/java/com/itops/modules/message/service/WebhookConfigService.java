package com.itops.modules.message.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.modules.message.entity.BizWebhookConfig;
import com.itops.modules.message.mapper.BizWebhookConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebhookConfigService extends ServiceImpl<BizWebhookConfigMapper, BizWebhookConfig> {

    public List<BizWebhookConfig> listAll(String name, String type) {
        return list(Wrappers.<BizWebhookConfig>lambdaQuery()
                .like(name != null && !name.isBlank(), BizWebhookConfig::getName, name)
                .eq(type != null && !type.isBlank(), BizWebhookConfig::getType, type)
                .orderByDesc(BizWebhookConfig::getWebhookId));
    }

    /** 查询启用且订阅指定事件的配置 */
    public List<BizWebhookConfig> listEnabledForEvent(String action) {
        return list(Wrappers.<BizWebhookConfig>lambdaQuery()
                .eq(BizWebhookConfig::getStatus, "0")
                .and(w -> w.eq(BizWebhookConfig::getEvents, "*")
                        .or().like(BizWebhookConfig::getEvents, action)));
    }
}
