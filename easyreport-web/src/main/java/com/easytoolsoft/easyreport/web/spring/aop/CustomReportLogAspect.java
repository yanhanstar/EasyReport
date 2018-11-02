package com.easytoolsoft.easyreport.web.spring.aop;

import com.easytoolsoft.easyreport.membership.domain.ReportEvent;
import com.easytoolsoft.easyreport.membership.domain.User;
import com.easytoolsoft.easyreport.membership.service.ReportEventService;
import com.easytoolsoft.easyreport.support.aop.ReportLogAspect;
import com.easytoolsoft.easyreport.support.consts.UserAuthConsts;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author Tom Deng
 * @date 2017-03-25
 **/
@Slf4j
@Aspect
@Component
public class CustomReportLogAspect extends ReportLogAspect {
    @Resource
    private ReportEventService reportEventService;

    @Override
    protected void logReportEvent(final ReportEventParameter reportEventParameter) {
        final HttpServletRequest req =
            ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        final User user = (User)req.getAttribute(UserAuthConsts.CURRENT_USER);
        if (user != null) {
            final ReportEvent reportEvent = ReportEvent.builder()
                .source(reportEventParameter.getSource())
                .account(user.getAccount())
                .userId(user.getId())
                .message(reportEventParameter.toString())
                .level(reportEventParameter.getLevel())
                .url(req.getRequestURL().toString())
                .gmtCreated(new Date())
                .build();
            this.reportEventService.add(reportEvent);
        }
    }
}

