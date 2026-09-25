package com.itops.security.datascope;

/**
 * 数据权限 SQL 片段线程上下文
 */
public final class DataScopeContext {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private DataScopeContext() {
    }

    public static void set(String sql) {
        HOLDER.set(sql);
    }

    /** 获取当前数据权限 SQL 片段(无任何限制时返回空串) */
    public static String get() {
        String sql = HOLDER.get();
        return sql == null ? "" : sql;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
