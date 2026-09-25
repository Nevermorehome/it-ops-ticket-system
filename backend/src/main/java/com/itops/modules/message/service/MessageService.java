package com.itops.modules.message.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.common.exception.BusinessException;
import com.itops.modules.message.entity.BizMessage;
import com.itops.modules.message.mapper.BizMessageMapper;
import com.itops.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MessageService extends ServiceImpl<BizMessageMapper, BizMessage> {

    /** 生成站内信 */
    public void push(Long receiverId, String title, String content, String bizType, Long bizId) {
        if (receiverId == null) {
            return;
        }
        BizMessage message = new BizMessage();
        message.setReceiverId(receiverId);
        message.setMessageType("TICKET");
        message.setTitle(title);
        message.setContent(content);
        message.setBizType(bizType);
        message.setBizId(bizId);
        message.setIsRead(0);
        save(message);
    }

    public IPage<BizMessage> pageMine(long pageNum, long pageSize, Integer isRead) {
        Long userId = SecurityUtils.getUserId();
        return page(new Page<>(pageNum, pageSize), Wrappers.<BizMessage>lambdaQuery()
                .eq(BizMessage::getReceiverId, userId)
                .eq(isRead != null, BizMessage::getIsRead, isRead)
                .orderByDesc(BizMessage::getMessageId));
    }

    public long unreadCount() {
        return count(Wrappers.<BizMessage>lambdaQuery()
                .eq(BizMessage::getReceiverId, SecurityUtils.getUserId())
                .eq(BizMessage::getIsRead, 0));
    }

    public void markRead(Long messageId) {
        BizMessage message = getById(messageId);
        if (message == null) {
            throw BusinessException.of("消息不存在");
        }
        if (!message.getReceiverId().equals(SecurityUtils.getUserId())) {
            throw BusinessException.forbidden("只能操作本人的消息");
        }
        if (message.getIsRead() == 0) {
            message.setIsRead(1);
            message.setReadTime(LocalDateTime.now());
            updateById(message);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void markAllRead() {
        BizMessage update = new BizMessage();
        update.setIsRead(1);
        update.setReadTime(LocalDateTime.now());
        update(update, Wrappers.<BizMessage>lambdaUpdate()
                .eq(BizMessage::getReceiverId, SecurityUtils.getUserId())
                .eq(BizMessage::getIsRead, 0));
    }
}
