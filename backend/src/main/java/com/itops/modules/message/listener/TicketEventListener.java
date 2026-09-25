package com.itops.modules.message.listener;

import com.itops.modules.message.service.MessageService;
import com.itops.modules.message.service.WebhookDispatcher;
import com.itops.modules.ticket.event.TicketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 工单事件 -> 站内信 + 群机器人 Webhook
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketEventListener {

    private final MessageService messageService;
    private final WebhookDispatcher webhookDispatcher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketAction(TicketEvent event) {
        // 1. 站内信
        if (event.getReceiverIds() != null) {
            String title = "工单" + event.getActionName() + "通知";
            String content = "您的工单[" + event.getTicketNo() + "]" + event.getActionName()
                    + "，标题：" + event.getTitle();
            for (Long receiverId : event.getReceiverIds()) {
                try {
                    messageService.push(receiverId, title, content, event.getAction(), event.getTicketId());
                } catch (Exception e) {
                    log.warn("站内信写入失败: {}", e.getMessage());
                }
            }
        }
        // 2. 群机器人
        try {
            webhookDispatcher.dispatch(event);
        } catch (Exception e) {
            log.warn("Webhook 分发异常: {}", e.getMessage());
        }
    }
}
