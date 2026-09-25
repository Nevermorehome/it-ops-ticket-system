package com.itops.modules.ticket.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("biz_ticket_timeline")
public class TicketTimeline {

    @TableId
    private Long timelineId;
    private Long ticketId;
    /** TicketAction 枚举名 */
    private String action;
    private String actionName;
    private String content;
    private String fromStatus;
    private String toStatus;
    private Long targetUserId;
    private String targetUserName;
    private Long operateId;
    private String operateName;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<TicketAttachment> attachments;
}
