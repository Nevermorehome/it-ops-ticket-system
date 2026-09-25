package com.itops.modules.ticket.enums;

import lombok.Getter;

/**
 * 工单状态
 */
@Getter
public enum TicketStatus {

    PENDING("待受理"),
    ASSIGNED("已指派"),
    PROCESSING("处理中"),
    SUSPENDED("已挂起"),
    RESOLVED("已解决"),
    CLOSED("已关闭"),
    CANCELLED("已取消"),
    MERGED("已合并");

    private final String label;

    TicketStatus(String label) {
        this.label = label;
    }

    public static String labelOf(String name) {
        try {
            return TicketStatus.valueOf(name).getLabel();
        } catch (Exception e) {
            return name;
        }
    }
}
