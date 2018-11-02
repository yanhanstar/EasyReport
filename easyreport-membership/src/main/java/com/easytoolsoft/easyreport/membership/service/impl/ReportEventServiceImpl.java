package com.easytoolsoft.easyreport.membership.service.impl;

import com.easytoolsoft.easyreport.membership.data.ReportEventRepository;
import com.easytoolsoft.easyreport.membership.domain.ReportEvent;
import com.easytoolsoft.easyreport.membership.domain.example.ReportEventExample;
import com.easytoolsoft.easyreport.membership.service.ReportEventService;
import com.easytoolsoft.easyreport.mybatis.service.AbstractCrudService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Tom Deng
 * @date 2017-03-25
 */
@Service("ReportEventService")
public class ReportEventServiceImpl
    extends AbstractCrudService<ReportEventRepository, ReportEvent, ReportEventExample, Integer>
    implements ReportEventService {

    @Override
    protected ReportEventExample getPageExample(final String fieldName, final String keyword) {
        final ReportEventExample example = new ReportEventExample();
        example.createCriteria().andFieldLike(fieldName, keyword);
        return example;
    }

    @Override
    public void clear() {
        this.dao.deleteByExample(null);
    }

    public void add(final String source, final String reportId, final String reportName,
					final Integer userId, final String userName, final String account,
					final String message, final String level, final String url) {
        final ReportEvent event = ReportEvent.builder()
            .source(source)
            .reportId(reportId)
			.reportName(reportName)
            .userId(userId)
			.userName(userName)
			.account(account)
			.level(level)
            .message(message)
            .url(url)
            .gmtCreated(new Date())
            .build();
        this.add(event);
    }
}