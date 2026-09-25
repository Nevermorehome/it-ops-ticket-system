package com.itops.modules.ticket.dto;

import com.itops.modules.ticket.entity.TicketAttachment;
import lombok.Data;

import java.util.List;

/**
 * 工单动作统一入参(指派/转派/协同/处理/挂起/解决/合并等)
 */
@Data
public class TicketActionDTO {

    private Long ticketId;

    /** 指派/转派目标人 */
    private Long targetUserId;

    /** 协同人ID列表 */
    private List<Long> collaboratorIds;

    /** 处理内容/转派原因/取消原因/挂起原因/重开原因 */
    private String content;

    /** 解决方案 */
    private String solution;

    /** 挂起原因(dict suspend_reason) */
    private String suspendReason;

    /** 合并目标工单ID */
    private Long targetTicketId;

    /** 处理记录/建单附件 */
    private List<TicketAttachment> attachments;

    /** 满意度 1-5(关闭时可选) */
    private Integer satisfaction;
    private String satisfactionComment;
}
