package com.itops.modules.ticket.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.common.exception.BusinessException;
import com.itops.modules.ticket.entity.FieldRecord;
import com.itops.modules.ticket.entity.Ticket;
import com.itops.modules.ticket.entity.TicketAttachment;
import com.itops.modules.ticket.entity.TicketCollaborator;
import com.itops.modules.ticket.enums.TicketStatus;
import com.itops.modules.ticket.mapper.FieldRecordMapper;
import com.itops.modules.ticket.mapper.TicketAttachmentMapper;
import com.itops.modules.ticket.mapper.TicketCollaboratorMapper;
import com.itops.security.LoginUser;
import com.itops.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldRecordService extends ServiceImpl<FieldRecordMapper, FieldRecord> {

    private final TicketService ticketService;
    private final TicketAttachmentMapper attachmentMapper;
    private final TicketCollaboratorMapper collaboratorMapper;

    /**
     * 新增现场记录(服务器时间, 仅处理人/协同人/管理员, 工单须处理中或已挂起)
     */
    @Transactional(rollbackFor = Exception.class)
    public FieldRecord add(Long ticketId, FieldRecord body, List<TicketAttachment> images) {
        Ticket ticket = ticketService.getById(ticketId);
        if (ticket == null) {
            throw BusinessException.of("工单不存在");
        }
        LoginUser user = SecurityUtils.requireLoginUser();
        String status = ticket.getStatus();
        if (!TicketStatus.PROCESSING.name().equals(status) && !TicketStatus.SUSPENDED.name().equals(status)) {
            throw BusinessException.of("仅处理中/已挂起的工单可以添加现场记录");
        }
        if (!user.isAdmin()
                && !user.getUserId().equals(ticket.getHandlerId())
                && collaboratorMapper.selectCount(Wrappers.<TicketCollaborator>lambdaQuery()
                .eq(TicketCollaborator::getTicketId, ticketId)
                .eq(TicketCollaborator::getUserId, user.getUserId())) == 0) {
            throw BusinessException.forbidden("仅处理人或协同人可添加现场记录");
        }

        body.setRecordId(null);
        body.setTicketId(ticketId);
        body.setRecordTime(LocalDateTime.now());
        body.setCreateById(user.getUserId());
        body.setCreateBy(user.getRealName());
        save(body);

        if (images != null) {
            for (TicketAttachment a : images) {
                if (a.getFileUrl() == null || a.getFileUrl().isBlank()) {
                    continue;
                }
                a.setAttachmentId(null);
                a.setTicketId(ticketId);
                a.setFieldRecordId(body.getRecordId());
                a.setTimelineId(null);
                attachmentMapper.insert(a);
            }
        }
        return body;
    }

    public List<FieldRecord> listByTicket(Long ticketId) {
        List<FieldRecord> records = list(Wrappers.<FieldRecord>lambdaQuery()
                .eq(FieldRecord::getTicketId, ticketId)
                .orderByDesc(FieldRecord::getRecordId));
        for (FieldRecord record : records) {
            record.setImages(attachmentMapper.selectList(Wrappers.<TicketAttachment>lambdaQuery()
                    .eq(TicketAttachment::getFieldRecordId, record.getRecordId())));
        }
        return records;
    }
}
