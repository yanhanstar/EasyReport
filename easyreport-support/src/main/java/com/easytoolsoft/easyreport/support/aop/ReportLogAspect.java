package com.easytoolsoft.easyreport.support.aop;

import com.easytoolsoft.easyreport.support.annotation.ReportLog;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author Tom Deng
 * @date 2017-03-25
 * 仅限报表操作日志使用
 * 保存信息：
 *  查询报表/导出报表
 *  报表名称
 *  查询参数
 *  用户名称
 **/
@Slf4j
public class ReportLogAspect {
    @Pointcut("@annotation(com.easytoolsoft.easyreport.support.annotation.ReportLog)")
    public void pointcut() {
    }

    @After("pointcut()")
    public void doAfter(final JoinPoint joinPoint) {
        try {
            this.logReportEvent(joinPoint, "INFO", "");
        } catch (final Exception e) {
            log.error("异常信息:{}", e);
        }
    }

    @AfterThrowing(pointcut = "pointcut()", throwing = "e")
    public void doAfterThrowing(final JoinPoint joinPoint, final Throwable e) {
        try {
            this.logReportEvent(joinPoint, "ERROR", ExceptionUtils.getStackTrace(e));
        } catch (final Exception ex) {
            log.error("异常信息:{}", ex.getMessage());
        }
    }

    protected void logReportEvent(final JoinPoint joinPoint, final String level, final String message) {
        try {
            final ReportEventParameter reportEventParameter = this.getReportEventParameter(joinPoint, level, message);
            this.logReportEvent(reportEventParameter);
        } catch (final Exception e) {
            log.error("记录系统事件出错", e);
        }
    }

    protected void logReportEvent(final ReportEventParameter reportEventParameter) {
        final HttpServletRequest req =
            ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        reportEventParameter.setUrl(req.getRequestURL().toString());
        log.info("ReportLog:{}", reportEventParameter.toString());
    }

    protected ReportEventParameter getReportEventParameter(final JoinPoint joinPoint, final String level, final String message)
        throws Exception {
        final ReportEventParameter reportEventParameter = new ReportEventParameter();
        final String targetName = joinPoint.getTarget().getClass().getName();
        final String methodName = joinPoint.getSignature().getName();
        final Object[] arguments = joinPoint.getArgs();
        final Class targetClass = Class.forName(targetName);
        final Method[] methods = targetClass.getMethods();

        reportEventParameter.setSource(targetName + ":" + methodName);
        reportEventParameter.setLevel(level);
        reportEventParameter.setMessage(message);
        for (final Method method : methods) {
            if (method.getName().equals(methodName)) {
                final Class[] methodParameterTypes = method.getParameterTypes();
                if (methodParameterTypes.length == arguments.length) {
                    reportEventParameter.setName(method.getAnnotation(ReportLog.class).name());
                    reportEventParameter.setDesc(method.getAnnotation(ReportLog.class).desc());
                    reportEventParameter.setArguments(StringUtils.join(arguments, ","));
                    break;
                }
            }
        }
        return reportEventParameter;
    }

    /**
     * 事件参数类
     */
    @Data
    public static class ReportEventParameter {
        /**
         * 事件来源
         */
        private String source;
        /**
         * 事件级别
         */
        private String level;
        /**
         * 事件名称
         */
        private String name;
        /**
         * 事件说明
         */
        private String desc;
        /**
         * 事件调用方法参数
         */
        private String arguments;
        /**
         * 事件信息
         */
        private String message;
        /**
         * 事件请求url
         */
        private String url;
    }
}

