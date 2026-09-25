package com.itops.modules.message.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_message")
public class BizMessage {

    @TableId
    private Long messageId;
    private Long receiverId;
    private String messageType;
    private String title;
    private String content;
    private String bizType;
    private Long bizId;
    /** 0未读 1已读 */
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}
