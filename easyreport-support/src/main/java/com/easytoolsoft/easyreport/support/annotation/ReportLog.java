package com.easytoolsoft.easyreport.support.annotation;

import java.lang.annotation.*;

/**
 * 报表操作日志注解
 *
 * @author yanh
 * @date 2018-11-01
 **/
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReportLog {
    /**
     * 操作日志名称
     *
     * @return
     */
    String name() default "";

    /**
     * 操作日常说明
     *
     * @return
     */
    String desc() default "";
}
