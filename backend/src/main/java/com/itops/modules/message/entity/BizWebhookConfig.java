package com.itops.modules.message.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.itops.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_webhook_config")
public class BizWebhookConfig extends BaseEntity {

    @TableId
    private Long webhookId;
    private String name;
    /** wechat / dingtalk / feishu */
    private String type;
    private String webhookUrl;
    private String secret;
    /** * 或逗号分隔动作名 */
    private String events;
    /** 0启用 1停用 */
    private String status;
    private String remark;
}
