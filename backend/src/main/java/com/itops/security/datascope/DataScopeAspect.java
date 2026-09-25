package com.itops.security.datascope;

import com.itops.modules.system.service.SysDeptService;
import com.itops.security.LoginUser;
import com.itops.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 数据权限切面:
 * 按当前用户各角色 data_scope 生成 SQL 片段
 * 1 全部 -> 无片段; 2 自定义部门; 3 本部门; 4 本部门及以下; 5 仅本人(selfSql)
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DataScopeAspect {

    private final SysDeptService deptService;

    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint pjp, DataScope dataScope) throws Throwable {
        try {
            DataScopeContext.set(buildFragment(dataScope));
            return pjp.proceed();
        } finally {
            DataScopeContext.clear();
        }
    }

    private String buildFragment(DataScope ds) {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null || user.isAdmin() || user.getRoleScopes() == null || user.getRoleScopes().isEmpty()) {
            return "";
        }
        String col = (ds.deptAlias() == null || ds.deptAlias().isBlank())
                ? ds.deptColumn() : ds.deptAlias() + "." + ds.deptColumn();

        Set<Long> deptIds = new LinkedHashSet<>();
        boolean selfScope = false;

        for (LoginUser.RoleScope scope : user.getRoleScopes()) {
            String type = scope.getDataScope();
            switch (type) {
                case "1" -> {
                    return "";
                }
                case "2" -> {
                    if (scope.getDeptIds() != null) {
                        deptIds.addAll(scope.getDeptIds());
                    }
                }
                case "3" -> {
                    if (user.getDeptId() != null) {
                        deptIds.add(user.getDeptId());
                    }
                }
                case "4" -> {
                    if (user.getDeptId() != null) {
                        deptIds.addAll(deptService.selfAndChildrenIds(user.getDeptId()));
                    }
                }
                case "5" -> selfScope = true;
                default -> log.debug("未知数据范围类型: {}", type);
            }
        }

        String deptPart = null;
        if (!deptIds.isEmpty()) {
            deptPart = col + " IN (" + deptIds.stream().map(String::valueOf)
                    .reduce((a, b) -> a + "," + b).orElse("") + ")";
        }
        String selfPart = null;
        if (selfScope && ds.selfSql() != null && !ds.selfSql().isBlank()) {
            selfPart = ds.selfSql().replace("{userId}", String.valueOf(user.getUserId()));
        }

        if (deptPart != null && selfPart != null) {
            return " AND (" + deptPart + " OR " + selfPart + ")";
        }
        if (deptPart != null) {
            return " AND " + deptPart;
        }
        if (selfPart != null) {
            return " AND " + selfPart;
        }
        // 无部门且无本人条件 -> 查不到任何数据
        return " AND 1 = 0";
    }
}
