package com.itops.modules.system.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解(标注于 Controller 方法)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /** 模块标题 */
    String title();

    /** 业务类型 0其它 1新增 2修改 3删除 4导出 5导入 */
    int businessType() default 0;
}
