package com.itops.modules.message.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itops.common.api.R;
import com.itops.modules.message.entity.BizMessage;
import com.itops.modules.message.entity.BizWebhookConfig;
import com.itops.modules.message.service.MessageService;
import com.itops.modules.message.service.WebhookConfigService;
import com.itops.modules.system.annotation.OperLog;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "消息通知")
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final WebhookConfigService webhookConfigService;

    // ---------------- 站内信 ----------------

    @GetMapping("/page")
    public R<IPage<BizMessage>> page(@RequestParam(defaultValue = "1") long pageNum,
                                     @RequestParam(defaultValue = "10") long pageSize,
                                     @RequestParam(required = false) Integer isRead) {
        return R.ok(messageService.pageMine(pageNum, pageSize, isRead));
    }

    @GetMapping("/unread-count")
    public R<Long> unreadCount() {
        return R.ok(messageService.unreadCount());
    }

    @PutMapping("/read/{messageId}")
    public R<Void> read(@PathVariable Long messageId) {
        messageService.markRead(messageId);
        return R.ok();
    }

    @PutMapping("/read-all")
    public R<Void> readAll() {
        messageService.markAllRead();
        return R.ok();
    }

    @DeleteMapping("/{messageId}")
    public R<Void> removeMessage(@PathVariable Long messageId) {
        messageService.removeById(messageId);
        return R.ok();
    }

    // ---------------- Webhook 配置 ----------------

    @PreAuthorize("hasAuthority('message:webhook:list')")
    @GetMapping("/webhook/list")
    public R<List<BizWebhookConfig>> list(@RequestParam(required = false) String name,
                                          @RequestParam(required = false) String type) {
        return R.ok(webhookConfigService.listAll(name, type));
    }

    @OperLog(title = "Webhook配置", businessType = 1)
    @PreAuthorize("hasAuthority('message:webhook:list')")
    @PostMapping("/webhook")
    public R<Void> create(@RequestBody BizWebhookConfig config) {
        webhookConfigService.save(config);
        return R.ok();
    }

    @OperLog(title = "Webhook配置", businessType = 2)
    @PreAuthorize("hasAuthority('message:webhook:list')")
    @PutMapping("/webhook")
    public R<Void> update(@RequestBody BizWebhookConfig config) {
        webhookConfigService.updateById(config);
        return R.ok();
    }

    @OperLog(title = "Webhook配置", businessType = 3)
    @PreAuthorize("hasAuthority('message:webhook:list')")
    @DeleteMapping("/webhook/{webhookId}")
    public R<Void> remove(@PathVariable Long webhookId) {
        webhookConfigService.removeById(webhookId);
        return R.ok();
    }
}
