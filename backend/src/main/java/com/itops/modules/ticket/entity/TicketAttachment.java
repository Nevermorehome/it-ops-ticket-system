package com.itops.modules.ticket.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_ticket_attachment")
public class TicketAttachment {

    @TableId
    private Long attachmentId;
    private Long ticketId;
    private Long timelineId;
    private Long fieldRecordId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private String createBy;
    private LocalDateTime createTime;
}
