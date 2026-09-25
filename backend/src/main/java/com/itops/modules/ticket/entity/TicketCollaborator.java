package com.itops.modules.ticket.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_ticket_collaborator")
public class TicketCollaborator {

    @TableId
    private Long id;
    private Long ticketId;
    private Long userId;
    private String userName;
    private String createBy;
    private LocalDateTime createTime;
}
