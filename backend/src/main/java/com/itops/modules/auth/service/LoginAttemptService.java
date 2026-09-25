package com.itops.modules.auth.service;

import com.itops.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败限流(内存版, 单实例部署足够):
 * 同一 IP + 账号连续失败超过阈值后锁定一段时间, 防止暴力破解
 */
@Slf4j
@Service
public class LoginAttemptService {

    @Value("${itops.security.login-max-fail:5}")
    private int maxFail;

    @Value("${itops.security.login-lock-minutes:10}")
    private long lockMinutes;

    private record Attempt(int count, Instant lockedUntil) {
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    /** 登录前校验: 仍在锁定期内直接拒绝 */
    public void checkLocked(String username, String ip) {
        String key = key(username, ip);
        Attempt a = attempts.get(key);
        if (a == null) {
            return;
        }
        if (a.lockedUntil() != null && a.lockedUntil().isAfter(Instant.now())) {
            long mins = Duration.between(Instant.now(), a.lockedUntil()).toMinutes() + 1;
            log.warn("账号登录已锁定: user={} ip={} 剩余约{}分钟", username, ip, mins);
            throw new BusinessException(429, "连续登录失败次数过多，账号已临时锁定，请 " + mins + " 分钟后再试");
        }
    }

    /** 记录一次失败, 达到阈值则锁定 */
    public void recordFailure(String username, String ip) {
        String k = key(username, ip);
        attempts.compute(k, (key, old) -> {
            int count = (old == null || (old.lockedUntil() != null && old.lockedUntil().isBefore(Instant.now())))
                    ? 1 : old.count() + 1;
            Instant lockedUntil = count >= maxFail
                    ? Instant.now().plus(Duration.ofMinutes(lockMinutes)) : null;
            return new Attempt(count, lockedUntil);
        });
        evictExpired();
    }

    /** 登录成功清除计数 */
    public void recordSuccess(String username, String ip) {
        attempts.remove(key(username, ip));
    }

    private String key(String username, String ip) {
        return (username == null ? "" : username.trim().toLowerCase()) + "|" + (ip == null ? "" : ip);
    }

    /** 惰性清理过期项, 避免 Map 无限增长 */
    private void evictExpired() {
        if (attempts.size() < 1000) {
            return;
        }
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Attempt>> it = attempts.entrySet().iterator();
        while (it.hasNext()) {
            Attempt a = it.next().getValue();
            if (a.lockedUntil() != null && a.lockedUntil().isBefore(now)) {
                it.remove();
            }
        }
    }
}
