package com.itops.modules.ticket.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.common.exception.BusinessException;
import com.itops.modules.system.entity.SysUser;
import com.itops.modules.system.mapper.SysUserMapper;
import com.itops.modules.ticket.dto.TicketActionDTO;
import com.itops.modules.ticket.dto.TicketQueryDTO;
import com.itops.modules.ticket.entity.FieldRecord;
import com.itops.modules.ticket.entity.Ticket;
import com.itops.modules.ticket.entity.TicketAttachment;
import com.itops.modules.ticket.entity.TicketCollaborator;
import com.itops.modules.ticket.entity.TicketTimeline;
import com.itops.modules.ticket.enums.TicketAction;
import com.itops.modules.ticket.enums.TicketStatus;
import com.itops.modules.ticket.event.TicketEvent;
import com.itops.modules.ticket.mapper.FieldRecordMapper;
import com.itops.modules.ticket.mapper.TicketAttachmentMapper;
import com.itops.modules.ticket.mapper.TicketCollaboratorMapper;
import com.itops.modules.ticket.mapper.TicketMapper;
import com.itops.modules.ticket.mapper.TicketTimelineMapper;
import com.itops.security.LoginUser;
import com.itops.security.SecurityUtils;
import com.itops.security.datascope.DataScope;
import com.itops.security.datascope.DataScopeContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 工单核心服务: CRUD + 14 类状态机动作 + 时间线 + 合并
 */
@Service
@RequiredArgsConstructor
public class TicketService extends ServiceImpl<TicketMapper, Ticket> {

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String NO_PREFIX = "WO";

    private final TicketCollaboratorMapper collaboratorMapper;
    private final TicketTimelineMapper timelineMapper;
    private final TicketAttachmentMapper attachmentMapper;
    private final FieldRecordMapper fieldRecordMapper;
    private final SysUserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    // ========================= 查询 =========================

    @DataScope(deptAlias = "t",
            selfSql = " (t.reporter_id = {userId} OR t.handler_id = {userId} OR EXISTS " +
                    "(SELECT 1 FROM biz_ticket_collaborator c WHERE c.ticket_id = t.ticket_id AND c.user_id = {userId})) ")
    public IPage<Ticket> page(TicketQueryDTO query) {
        Page<Ticket> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectTicketPage(page, query, DataScopeContext.get(), SecurityUtils.getUserId());
    }

    public Ticket detail(Long ticketId) {
        Ticket ticket = baseMapper.selectDetailById(ticketId);
        if (ticket == null) {
            throw BusinessException.of("工单不存在");
        }
        ticket.setCollaborators(collaboratorMapper.selectList(
                Wrappers.<TicketCollaborator>lambdaQuery().eq(TicketCollaborator::getTicketId, ticketId)));
        ticket.setAttachments(attachmentMapper.selectList(
                Wrappers.<TicketAttachment>lambdaQuery()
                        .eq(TicketAttachment::getTicketId, ticketId)
                        .isNull(TicketAttachment::getTimelineId)
                        .isNull(TicketAttachment::getFieldRecordId)
                        .orderByAsc(TicketAttachment::getAttachmentId)));

        List<TicketTimeline> timelines = timelineMapper.selectList(
                Wrappers.<TicketTimeline>lambdaQuery()
                        .eq(TicketTimeline::getTicketId, ticketId)
                        .orderByAsc(TicketTimeline::getTimelineId));
        for (TicketTimeline timeline : timelines) {
            timeline.setAttachments(attachmentMapper.selectList(
                    Wrappers.<TicketAttachment>lambdaQuery()
                            .eq(TicketAttachment::getTimelineId, timeline.getTimelineId())));
        }
        ticket.setTimelines(timelines);

        List<FieldRecord> records = fieldRecordMapper.selectList(
                Wrappers.<FieldRecord>lambdaQuery()
                        .eq(FieldRecord::getTicketId, ticketId)
                        .orderByDesc(FieldRecord::getRecordId));
        for (FieldRecord record : records) {
            record.setImages(attachmentMapper.selectList(
                    Wrappers.<TicketAttachment>lambdaQuery()
                            .eq(TicketAttachment::getFieldRecordId, record.getRecordId())));
        }
        ticket.setFieldRecords(records);
        return ticket;
    }

    // ========================= 创建 / 编辑 =========================

    @Transactional(rollbackFor = Exception.class)
    public Ticket create(Ticket ticket, List<TicketAttachment> attachments) {
        LoginUser loginUser = SecurityUtils.requireLoginUser();

        ticket.setTicketNo(generateTicketNo());
        ticket.setStatus(TicketStatus.PENDING.name());
        if (ticket.getPriority() == null) {
            ticket.setPriority(1);
        }
        if (ticket.getSource() == null || ticket.getSource().isBlank()) {
            ticket.setSource("self");
        }
        if (ticket.getReporterId() == null) {
            ticket.setReporterId(loginUser.getUserId());
            ticket.setReporterName(loginUser.getRealName());
        }
        if (ticket.getReporterName() == null || ticket.getReporterName().isBlank()) {
            ticket.setReporterName(loginUser.getRealName());
        }
        if (ticket.getDeptId() == null) {
            ticket.setDeptId(loginUser.getDeptId());
        }
        save(ticket);

        saveAttachments(ticket.getTicketId(), null, null, attachments);
        writeTimeline(ticket, TicketAction.CREATE, TicketStatus.PENDING.name(),
                "创建工单", null, null);
        return ticket;
    }

    @Transactional(rollbackFor = Exception.class)
    public void edit(Ticket body) {
        Ticket ticket = mustGet(body.getTicketId());
        LoginUser user = SecurityUtils.requireLoginUser();
        checkStatus(ticket, TicketAction.EDIT);
        if (!user.isAdmin() && !user.getUserId().equals(ticket.getReporterId())
                && !user.getUserId().equals(ticket.getHandlerId())) {
            throw BusinessException.forbidden("仅报修人或处理人可编辑该工单");
        }
        copyEditableFields(body, ticket);
        updateById(ticket);
        writeTimeline(ticket, TicketAction.EDIT, ticket.getStatus(), "编辑工单信息", null, null);
    }

    // ========================= 状态机动作 =========================

    @Transactional(rollbackFor = Exception.class)
    public void execute(TicketAction action, TicketActionDTO dto) {
        Ticket ticket = mustGet(dto.getTicketId());
        LoginUser user = SecurityUtils.requireLoginUser();

        checkStatus(ticket, action);
        checkParticipant(ticket, action, user);

        TicketStatus from = TicketStatus.valueOf(ticket.getStatus());
        TicketStatus to = action.getTargetStatus() == null ? from : action.getTargetStatus();

        List<Long> receiverIds = new ArrayList<>();
        List<String> receiverNames = new ArrayList<>();
        String content = dto.getContent();
        Ticket mergeTarget = null;

        switch (action) {
            case ASSIGN -> {
                SysUser target = mustGetEnabledUser(dto.getTargetUserId(), "请选择处理人");
                ticket.setHandlerId(target.getUserId());
                ticket.setHandlerName(target.getRealName());
                ticket.setAssignTime(LocalDateTime.now());
                receiverIds.add(target.getUserId());
                receiverNames.add(target.getRealName());
                content = "指派给 " + target.getRealName();
            }
            case TRANSFER -> {
                SysUser target = mustGetEnabledUser(dto.getTargetUserId(), "请选择转派对象");
                ticket.setHandlerId(target.getUserId());
                ticket.setHandlerName(target.getRealName());
                ticket.setAssignTime(LocalDateTime.now());
                if (TicketStatus.PROCESSING == from || TicketStatus.SUSPENDED == from) {
                    to = TicketStatus.ASSIGNED;
                }
                receiverIds.add(target.getUserId());
                receiverNames.add(target.getRealName());
                String reason = dto.getContent() == null ? "" : "，原因：" + dto.getContent();
                content = "转派给 " + target.getRealName() + reason;
            }
            case COLLABORATE -> {
                if (dto.getCollaboratorIds() == null || dto.getCollaboratorIds().isEmpty()) {
                    throw BusinessException.of("请选择协同人");
                }
                replaceCollaborators(ticket.getTicketId(), dto.getCollaboratorIds(),
                        receiverIds, receiverNames);
                content = "设置协同人：" + String.join("、", receiverNames);
            }
            case ACCEPT -> ticket.setAcceptTime(LocalDateTime.now());
            case HANDLE -> {
                if (dto.getContent() == null || dto.getContent().isBlank()) {
                    throw BusinessException.of("请填写处理情况");
                }
                ticket.setHandleSummary(dto.getContent());
            }
            case SUSPEND -> {
                if (dto.getContent() == null && dto.getSuspendReason() == null) {
                    throw BusinessException.of("请填写挂起原因");
                }
                ticket.setSuspendTime(LocalDateTime.now());
                ticket.setSuspendReason(firstNonBlank(dto.getSuspendReason(), dto.getContent()));
                content = "挂起：" + ticket.getSuspendReason();
            }
            case RESUME -> content = "工单恢复处理";
            case RESOLVE -> {
                if (dto.getSolution() == null || dto.getSolution().isBlank()) {
                    throw BusinessException.of("请填写解决方案");
                }
                LocalDateTime now = LocalDateTime.now();
                ticket.setSolution(dto.getSolution());
                ticket.setResolveTime(now);
                ticket.setFinishTime(now);
                LocalDateTime start = ticket.getAcceptTime() != null ? ticket.getAcceptTime() : ticket.getCreateTime();
                ticket.setDurationSeconds(Duration.between(start, now).getSeconds());
                if (ticket.getReporterId() != null && !ticket.getReporterId().equals(user.getUserId())) {
                    receiverIds.add(ticket.getReporterId());
                    receiverNames.add(ticket.getReporterName());
                }
                content = "解决：" + dto.getSolution();
            }
            case CLOSE -> {
                ticket.setCloseTime(LocalDateTime.now());
                if (dto.getSatisfaction() != null) {
                    ticket.setSatisfaction(dto.getSatisfaction());
                    ticket.setSatisfactionComment(dto.getSatisfactionComment());
                }
                if (ticket.getReporterId() != null && !ticket.getReporterId().equals(user.getUserId())) {
                    receiverIds.add(ticket.getReporterId());
                }
                content = "工单关闭";
            }
            case REOPEN -> {
                ticket.setResolveTime(null);
                ticket.setFinishTime(null);
                ticket.setDurationSeconds(0L);
                if (ticket.getReporterId() != null && ticket.getHandlerId() != null
                        && !ticket.getHandlerId().equals(user.getUserId())) {
                    receiverIds.add(ticket.getHandlerId());
                    receiverNames.add(ticket.getHandlerName());
                }
                content = firstNonBlank(dto.getContent(), "工单重新打开");
            }
            case CANCEL -> content = firstNonBlank(dto.getContent(), "工单取消");
            case MERGE -> mergeTarget = doMerge(ticket, dto, receiverIds);
            default -> throw BusinessException.of("不支持的操作");
        }

        if (action == TicketAction.MERGE && mergeTarget != null) {
            content = "合并至工单 " + mergeTarget.getTicketNo();
        }

        // 先记录时间线(此时 ticket.status 仍为操作前状态), 再更新主单状态
        TicketTimeline timeline = writeTimeline(ticket, action, to.name(), content,
                action == TicketAction.ASSIGN || action == TicketAction.TRANSFER ? dto.getTargetUserId() : null,
                receiverNamesStr(receiverNames));

        ticket.setStatus(to.name());
        updateById(ticket);

        // 处理记录附件
        if (action == TicketAction.HANDLE && dto.getAttachments() != null) {
            saveAttachments(ticket.getTicketId(), timeline.getTimelineId(), null, dto.getAttachments());
        }

        publishEvent(ticket, action, user, content, receiverIds, receiverNames);
    }

    // ========================= 合并 =========================

    private Ticket doMerge(Ticket source, TicketActionDTO dto, List<Long> receiverIds) {
        if (dto.getTargetTicketId() == null) {
            throw BusinessException.of("请选择合并目标工单");
        }
        if (dto.getTargetTicketId().equals(source.getTicketId())) {
            throw BusinessException.of("不能合并到自身");
        }
        Ticket target = getById(dto.getTargetTicketId());
        if (target == null) {
            throw BusinessException.of("目标工单不存在");
        }
        if (TicketStatus.MERGED.name().equals(target.getStatus())
                || TicketStatus.CANCELLED.name().equals(target.getStatus())) {
            throw BusinessException.of("目标工单状态不允许被合并");
        }

        source.setMergedTo(target.getTicketId());

        // 被并单协同人并入目标单
        List<TicketCollaborator> sourceCollabs = collaboratorMapper.selectList(
                Wrappers.<TicketCollaborator>lambdaQuery().eq(TicketCollaborator::getTicketId, source.getTicketId()));
        List<TicketCollaborator> targetCollabs = collaboratorMapper.selectList(
                Wrappers.<TicketCollaborator>lambdaQuery().eq(TicketCollaborator::getTicketId, target.getTicketId()));
        Set<Long> existIds = new HashSet<>(targetCollabs.stream().map(TicketCollaborator::getUserId).toList());
        for (TicketCollaborator sc : sourceCollabs) {
            if (existIds.add(sc.getUserId())) {
                TicketCollaborator tc = new TicketCollaborator();
                tc.setTicketId(target.getTicketId());
                tc.setUserId(sc.getUserId());
                tc.setUserName(sc.getUserName());
                collaboratorMapper.insert(tc);
            }
        }

        // 目标单时间线
        TicketTimeline targetLine = new TicketTimeline();
        targetLine.setTicketId(target.getTicketId());
        targetLine.setAction(TicketAction.MERGE.name());
        targetLine.setActionName("被合并入");
        targetLine.setContent("工单 " + source.getTicketNo() + " 已合并到本工单");
        targetLine.setFromStatus(target.getStatus());
        targetLine.setToStatus(target.getStatus());
        timelineMapper.insert(targetLine);

        receiverIds.add(target.getHandlerId());
        return target;
    }

    // ========================= 校验 =========================

    private void checkStatus(Ticket ticket, TicketAction action) {
        TicketStatus current;
        try {
            current = TicketStatus.valueOf(ticket.getStatus());
        } catch (IllegalArgumentException e) {
            throw BusinessException.of("工单状态异常");
        }
        if (!action.allowedFrom(current)) {
            throw BusinessException.of("当前状态[" + current.getLabel() + "]不允许执行[" + action.getLabel() + "]操作");
        }
    }

    /**
     * 当事人/权限二次校验(接口层 @PreAuthorize 已做权限标识校验)
     */
    private void checkParticipant(Ticket ticket, TicketAction action, LoginUser user) {
        if (user.isAdmin()) {
            return;
        }
        boolean isHandler = user.getUserId().equals(ticket.getHandlerId());
        boolean isReporter = user.getUserId().equals(ticket.getReporterId());

        switch (action) {
            case ASSIGN, MERGE -> {
                if (!SecurityUtils.hasPermi(action.getPerms())) {
                    throw BusinessException.forbidden("无权执行[" + action.getLabel() + "]操作");
                }
            }
            case TRANSFER -> {
                if (!isHandler && !SecurityUtils.hasPermi("ticket:assign")) {
                    throw BusinessException.forbidden("仅当前处理人或调度人员可转派");
                }
            }
            case COLLABORATE -> {
                if (!isHandler && !SecurityUtils.hasPermi("ticket:collaborate")) {
                    throw BusinessException.forbidden("仅处理人或调度人员可设置协同");
                }
            }
            case ACCEPT -> {
                if (!isHandler) {
                    throw BusinessException.forbidden("仅被指派人可受理该工单");
                }
            }
            case HANDLE, SUSPEND, RESUME, RESOLVE -> {
                if (!isHandler && !isCollaborator(ticket.getTicketId(), user.getUserId())) {
                    throw BusinessException.forbidden("仅处理人或协同人可执行该操作");
                }
            }
            case CLOSE, REOPEN -> {
                if (!isHandler && !isReporter && !SecurityUtils.hasPermi(action.getPerms())) {
                    throw BusinessException.forbidden("仅报修人、处理人或管理人员可执行该操作");
                }
            }
            case CANCEL -> {
                if (!isReporter && !isHandler) {
                    throw BusinessException.forbidden("仅报修人或处理人可取消该工单");
                }
            }
            default -> {
            }
        }
    }

    private boolean isCollaborator(Long ticketId, Long userId) {
        return collaboratorMapper.selectCount(Wrappers.<TicketCollaborator>lambdaQuery()
                .eq(TicketCollaborator::getTicketId, ticketId)
                .eq(TicketCollaborator::getUserId, userId)) > 0;
    }

    // ========================= 辅助 =========================

    private Ticket mustGet(Long ticketId) {
        Ticket ticket = getById(ticketId);
        if (ticket == null) {
            throw BusinessException.of("工单不存在");
        }
        return ticket;
    }

    private SysUser mustGetEnabledUser(Long userId, String message) {
        if (userId == null) {
            throw BusinessException.of(message);
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null || !"0".equals(user.getStatus())) {
            throw BusinessException.of("所选用户不存在或已停用");
        }
        return user;
    }

    private void replaceCollaborators(Long ticketId, List<Long> newIds,
                                      List<Long> addedIds, List<String> addedNames) {
        List<TicketCollaborator> exists = collaboratorMapper.selectList(
                Wrappers.<TicketCollaborator>lambdaQuery().eq(TicketCollaborator::getTicketId, ticketId));
        Set<Long> existSet = new HashSet<>(exists.stream().map(TicketCollaborator::getUserId).toList());
        Set<Long> newSet = new HashSet<>(newIds);

        // 删除被移除的
        exists.stream().filter(c -> !newSet.contains(c.getUserId()))
                .forEach(c -> collaboratorMapper.deleteById(c.getId()));
        // 新增
        for (Long uid : newSet) {
            if (!existSet.contains(uid)) {
                SysUser user = userMapper.selectById(uid);
                if (user == null) {
                    continue;
                }
                TicketCollaborator c = new TicketCollaborator();
                c.setTicketId(ticketId);
                c.setUserId(uid);
                c.setUserName(user.getRealName());
                collaboratorMapper.insert(c);
                addedIds.add(uid);
                addedNames.add(user.getRealName());
            }
        }
    }

    private void copyEditableFields(Ticket body, Ticket target) {
        if (body.getTitle() != null) target.setTitle(body.getTitle());
        if (body.getCategoryId() != null) target.setCategoryId(body.getCategoryId());
        if (body.getDescription() != null) target.setDescription(body.getDescription());
        if (body.getReporterName() != null) target.setReporterName(body.getReporterName());
        if (body.getPhone() != null) target.setPhone(body.getPhone());
        if (body.getSource() != null) target.setSource(body.getSource());
        if (body.getPriority() != null) target.setPriority(body.getPriority());
        if (body.getLocationText() != null) target.setLocationText(body.getLocationText());
        if (body.getLongitude() != null) target.setLongitude(body.getLongitude());
        if (body.getLatitude() != null) target.setLatitude(body.getLatitude());
        if (body.getAssetId() != null) target.setAssetId(body.getAssetId());
    }

    private void saveAttachments(Long ticketId, Long timelineId, Long recordId, List<TicketAttachment> attachments) {
        if (attachments == null) {
            return;
        }
        for (TicketAttachment a : attachments) {
            if (a.getFileUrl() == null || a.getFileUrl().isBlank()) {
                continue;
            }
            a.setAttachmentId(null);
            a.setTicketId(ticketId);
            a.setTimelineId(timelineId);
            a.setFieldRecordId(recordId);
            attachmentMapper.insert(a);
        }
    }

    private TicketTimeline writeTimeline(Ticket ticket, TicketAction action, String toStatus,
                                         String content, Long targetUserId, String targetUserName) {
        LoginUser user = SecurityUtils.getLoginUser();
        TicketTimeline timeline = new TicketTimeline();
        timeline.setTicketId(ticket.getTicketId());
        timeline.setAction(action.name());
        timeline.setActionName(action.getLabel());
        timeline.setContent(content);
        timeline.setFromStatus(ticket.getStatus());
        timeline.setToStatus(toStatus);
        timeline.setTargetUserId(targetUserId);
        timeline.setTargetUserName(targetUserName);
        if (user != null) {
            timeline.setOperateId(user.getUserId());
            timeline.setOperateName(user.getRealName());
        }
        timelineMapper.insert(timeline);
        return timeline;
    }

    private void publishEvent(Ticket ticket, TicketAction action, LoginUser user,
                              String content, List<Long> receiverIds, List<String> receiverNames) {
        List<Long> ids = receiverIds.stream().filter(id -> id != null && !id.equals(user.getUserId())).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        eventPublisher.publishEvent(TicketEvent.builder()
                .ticketId(ticket.getTicketId())
                .ticketNo(ticket.getTicketNo())
                .title(ticket.getTitle())
                .action(action.name())
                .actionName(action.getLabel())
                .status(ticket.getStatus())
                .content(content)
                .operatorId(user.getUserId())
                .operatorName(user.getRealName())
                .receiverIds(ids)
                .receiverNames(receiverNames)
                .build());
    }

    /**
     * 工单编号: WO + yyyyMMdd + 3 位序列(同日递增, 唯一索引兜底)
     */
    private synchronized String generateTicketNo() {
        String prefix = NO_PREFIX + LocalDateTime.now().format(NO_DATE);
        for (int attempt = 0; attempt < 5; attempt++) {
            Ticket last = getOne(Wrappers.<Ticket>lambdaQuery()
                    .likeRight(Ticket::getTicketNo, prefix)
                    .orderByDesc(Ticket::getTicketNo)
                    .last("LIMIT 1"));
            int seq = 1;
            if (last != null) {
                seq = Integer.parseInt(last.getTicketNo().substring(prefix.length())) + 1;
            }
            String no = prefix + String.format("%03d", seq);
            long exists = count(Wrappers.<Ticket>lambdaQuery().eq(Ticket::getTicketNo, no));
            if (exists == 0) {
                return no;
            }
        }
        throw BusinessException.of("工单编号生成失败，请重试");
    }

    private String firstNonBlank(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }

    private String receiverNamesStr(List<String> names) {
        return names == null || names.isEmpty() ? null : String.join("、", names);
    }
}
