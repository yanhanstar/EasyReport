package com.easytoolsoft.easyreport.membership.service;

import com.easytoolsoft.easyreport.membership.domain.ReportEvent;
import com.easytoolsoft.easyreport.membership.domain.example.ReportEventExample;
import com.easytoolsoft.easyreport.mybatis.service.CrudService;

/**
 * 系统事件或日志服务类
 *
 * @author Tom Deng
 * @date 2017-03-25
 */
public interface ReportEventService extends CrudService<ReportEvent, ReportEventExample, Integer> {
    /**
     *
     */
    void clear();

    /**
     * @param source
     * @param account
     * @param message
     * @param level
     * @param url
     */
    void add(String source,  String reportId,  String reportName,
             Integer userId,  String userName,  String account,
             String message,  String level,  String url);
}