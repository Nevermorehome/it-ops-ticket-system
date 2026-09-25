package com.itops.modules.ticket.enums;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

/**
 * 工单动作及其合法流转定义
 */
@Getter
public enum TicketAction {

    CREATE("创建", null, TicketStatus.PENDING, Set.of()),
    EDIT("编辑", "ticket:edit", null, EnumSet.of(TicketStatus.PENDING, TicketStatus.ASSIGNED)),
    ASSIGN("指派", "ticket:assign", TicketStatus.ASSIGNED, EnumSet.of(TicketStatus.PENDING)),
    TRANSFER("转派", "ticket:transfer", TicketStatus.ASSIGNED,
            EnumSet.of(TicketStatus.ASSIGNED, TicketStatus.PROCESSING, TicketStatus.SUSPENDED)),
    COLLABORATE("设置协同", "ticket:collaborate", null,
            EnumSet.of(TicketStatus.PENDING, TicketStatus.ASSIGNED, TicketStatus.PROCESSING,
                    TicketStatus.SUSPENDED, TicketStatus.RESOLVED)),
    ACCEPT("受理", "ticket:accept", TicketStatus.PROCESSING, EnumSet.of(TicketStatus.ASSIGNED)),
    HANDLE("处理", "ticket:handle", TicketStatus.PROCESSING, EnumSet.of(TicketStatus.PROCESSING)),
    SUSPEND("挂起", "ticket:suspend", TicketStatus.SUSPENDED, EnumSet.of(TicketStatus.PROCESSING)),
    RESUME("恢复", "ticket:suspend", TicketStatus.PROCESSING, EnumSet.of(TicketStatus.SUSPENDED)),
    RESOLVE("解决", "ticket:resolve", TicketStatus.RESOLVED, EnumSet.of(TicketStatus.PROCESSING)),
    CLOSE("关闭", "ticket:close", TicketStatus.CLOSED, EnumSet.of(TicketStatus.RESOLVED)),
    REOPEN("重开", "ticket:reopen", TicketStatus.PROCESSING, EnumSet.of(TicketStatus.CLOSED)),
    CANCEL("取消", "ticket:cancel", TicketStatus.CANCELLED,
            EnumSet.of(TicketStatus.PENDING, TicketStatus.ASSIGNED)),
    MERGE("合并", "ticket:merge", TicketStatus.MERGED,
            EnumSet.of(TicketStatus.PENDING, TicketStatus.ASSIGNED, TicketStatus.PROCESSING,
                    TicketStatus.SUSPENDED, TicketStatus.RESOLVED));

    /** 动作中文名 */
    private final String label;
    /** 所需权限标识(null 表示登录即可) */
    private final String perms;
    /** 流转后的目标状态(null 表示状态不变) */
    private final TicketStatus targetStatus;
    /** 允许发起的源状态集合 */
    private final Set<TicketStatus> allowedFrom;

    TicketAction(String label, String perms, TicketStatus targetStatus, Set<TicketStatus> allowedFrom) {
        this.label = label;
        this.perms = perms;
        this.targetStatus = targetStatus;
        this.allowedFrom = allowedFrom;
    }

    public boolean allowedFrom(TicketStatus status) {
        return allowedFrom.contains(status);
    }
}
