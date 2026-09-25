package com.itops.modules.message.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.itops.modules.message.entity.BizWebhookConfig;
import com.itops.modules.system.service.SysConfigService;
import com.itops.modules.ticket.enums.TicketStatus;
import com.itops.modules.ticket.event.TicketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业微信 / 钉钉 / 飞书 群机器人推送
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDispatcher {

    private final WebhookConfigService configService;
    private final SysConfigService sysConfigService;

    public void dispatch(TicketEvent event) {
        List<BizWebhookConfig> configs;
        try {
            configs = configService.listEnabledForEvent(event.getAction());
        } catch (Exception e) {
            log.warn("查询 webhook 配置失败: {}", e.getMessage());
            return;
        }
        for (BizWebhookConfig config : configs) {
            try {
                send(config, event);
            } catch (Exception e) {
                // 推送失败不影响主流程
                log.warn("Webhook[{}]推送失败: {}", config.getName(), e.getMessage());
            }
        }
    }

    private void send(BizWebhookConfig config, TicketEvent event) {
        String payload = switch (config.getType()) {
            case "wechat" -> buildWechatPayload(event);
            case "dingtalk" -> buildDingTalkPayload(event);
            case "feishu" -> buildFeishuPayload(event);
            default -> null;
        };
        if (payload == null) {
            return;
        }
        String url = config.getWebhookUrl();
        if ("dingtalk".equals(config.getType()) && StrUtil.isNotBlank(config.getSecret())) {
            url = signDingTalk(url, config.getSecret());
        }
        String body = HttpRequest.post(url)
                .timeout(5000)
                .header("Content-Type", "application/json;charset=utf-8")
                .body(payload)
                .execute()
                .body();
        log.info("Webhook[{}]响应: {}", config.getName(), body);
    }

    /** 企业微信 markdown 消息 */
    private String buildWechatPayload(TicketEvent e) {
        String md = "### IT运维工单通知\n"
                + "> 标题：<font color=\"info\">" + e.getTitle() + "</font>\n"
                + "> 编号：" + e.getTicketNo() + "\n"
                + "> 动作：" + e.getActionName() + "（" + TicketStatus.labelOf(e.getStatus()) + "）\n"
                + "> 操作人：" + e.getOperatorName() + "\n"
                + "> 时间：" + now() + "\n"
                + "> [点击查看工单](" + ticketLink(e.getTicketId()) + ")";
        Map<String, Object> markdown = new HashMap<>();
        markdown.put("content", md);
        Map<String, Object> payload = new HashMap<>();
        payload.put("msgtype", "markdown");
        payload.put("markdown", markdown);
        return JSONUtil.toJsonStr(payload);
    }

    /** 钉钉 markdown 消息 */
    private String buildDingTalkPayload(TicketEvent e) {
        String text = "### IT运维工单通知\n"
                + "> 标题：<font color=\"#1E90FF\">" + e.getTitle() + "</font>\n\n"
                + "> 编号：" + e.getTicketNo() + "\n\n"
                + "> 动作：" + e.getActionName() + "（" + TicketStatus.labelOf(e.getStatus()) + "）\n\n"
                + "> 操作人：" + e.getOperatorName() + "\n\n"
                + "> 时间：" + now() + "\n\n"
                + "[点击查看工单](" + ticketLink(e.getTicketId()) + ")";
        Map<String, Object> markdown = new HashMap<>();
        markdown.put("title", "IT运维工单通知");
        markdown.put("text", text);
        Map<String, Object> payload = new HashMap<>();
        payload.put("msgtype", "markdown");
        payload.put("markdown", markdown);
        return JSONUtil.toJsonStr(payload);
    }

    /** 飞书文本消息(自定义机器人) */
    private String buildFeishuPayload(TicketEvent e) {
        String text = "【IT运维工单通知】\n"
                + "标题：" + e.getTitle() + "\n"
                + "编号：" + e.getTicketNo() + "\n"
                + "动作：" + e.getActionName() + "（" + TicketStatus.labelOf(e.getStatus()) + "）\n"
                + "操作人：" + e.getOperatorName() + "\n"
                + "时间：" + now() + "\n"
                + "工单链接：" + ticketLink(e.getTicketId());
        Map<String, Object> content = new HashMap<>();
        content.put("text", text);
        Map<String, Object> payload = new HashMap<>();
        payload.put("msg_type", "text");
        payload.put("content", content);
        return JSONUtil.toJsonStr(payload);
    }

    /** 钉钉加签: base64(HmacSHA256(timestamp + "\\n" + secret)) */
    private String signDingTalk(String url, String secret) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signSource = timestamp + "\n" + secret;
        HMac mac = new HMac(HmacAlgorithm.HmacSHA256, secret.getBytes(StandardCharsets.UTF_8));
        String sign = URLUtil.encode(Base64.encode(mac.digest(signSource.getBytes(StandardCharsets.UTF_8))));
        char joiner = url.contains("?") ? '&' : '?';
        return url + joiner + "timestamp=" + timestamp + "&sign=" + sign;
    }

    private String ticketLink(Long ticketId) {
        String base = sysConfigService.getValue("sys.h5.url");
        if (StrUtil.isBlank(base)) {
            base = "http://localhost:8088/h5";
        }
        return base + "/#/pages/ticket/detail?id=" + ticketId;
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
