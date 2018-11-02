package com.easytoolsoft.easyreport.membership.data;

import com.easytoolsoft.easyreport.membership.domain.ReportEvent;
import com.easytoolsoft.easyreport.membership.domain.example.ReportEventExample;
import com.easytoolsoft.easyreport.mybatis.data.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * 报表事件或日志(ezrpt_sys_report_event)数据访问类
 *
 * @author Tom Deng
 * @date 2017-03-25
 */
@Repository("ReportEventRepository")
public interface ReportEventRepository extends CrudRepository<ReportEvent, ReportEventExample, Integer> {
}
