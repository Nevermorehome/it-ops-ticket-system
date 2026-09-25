package com.itops.modules.ticket.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工单动作事件(事务提交后异步通知: 站内信 + Webhook)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketEvent {

    private Long ticketId;
    private String ticketNo;
    private String title;
    /** TicketAction 枚举名 */
    private String action;
    private String actionName;
    private String status;
    private String content;
    private Long operatorId;
    private String operatorName;
    /** 站内信接收人(如被指派人), 不含操作人自身 */
    private List<Long> receiverIds;
    /** 协同人等附加接收人姓名 */
    private List<String> receiverNames;
}
