package com.itops.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.modules.system.entity.SysLoginLog;
import com.itops.modules.system.entity.SysOperationLog;
import com.itops.modules.system.mapper.SysLoginLogMapper;
import com.itops.modules.system.mapper.SysOperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysLogService {

    private final SysLoginLogMapper loginLogMapper;
    private final SysOperationLogMapper operationLogMapper;

    @Async
    public void saveLoginLog(SysLoginLog log) {
        loginLogMapper.insert(log);
    }

    @Async
    public void saveOperationLog(SysOperationLog log) {
        operationLogMapper.insert(log);
    }

    public IPage<SysLoginLog> loginLogPage(long pageNum, long pageSize, String username, String status) {
        return loginLogMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<SysLoginLog>lambdaQuery()
                        .like(StringUtils.hasText(username), SysLoginLog::getUsername, username)
                        .eq(StringUtils.hasText(status), SysLoginLog::getStatus, status)
                        .orderByDesc(SysLoginLog::getInfoId));
    }

    public IPage<SysOperationLog> operationLogPage(long pageNum, long pageSize, String title, String operName) {
        return operationLogMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<SysOperationLog>lambdaQuery()
                        .like(StringUtils.hasText(title), SysOperationLog::getTitle, title)
                        .like(StringUtils.hasText(operName), SysOperationLog::getOperName, operName)
                        .orderByDesc(SysOperationLog::getOperId));
    }

    public void clearLoginLog() {
        loginLogMapper.delete(Wrappers.lambdaQuery());
    }

    public void clearOperationLog() {
        operationLogMapper.delete(Wrappers.lambdaQuery());
    }
}
