package com.itops.security.datascope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解(标注在 Service 方法上, 由切面生成 SQL 片段写入 {@link DataScopeContext})
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /** 部门字段所在表别名(自定义 SQL 时使用, 如 "t"); 为空则不加别名 */
    String deptAlias() default "";

    /** 部门列名 */
    String deptColumn() default "dept_id";

    /**
     * 数据范围=仅本人 时的自定义条件(不含 AND), 使用 {userId} 占位。
     * 留空时本人范围按部门列无法匹配, 即不可见部门数据(视场景显式提供)。
     */
    String selfSql() default "";
}
