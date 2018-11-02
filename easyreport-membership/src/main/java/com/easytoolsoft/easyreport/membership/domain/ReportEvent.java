package com.easytoolsoft.easyreport.membership.domain;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户操作报表事件或日志(ezrpt_sys_report_event表)持久化类
 *
 * @author yanh
 * @date 2018-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportEvent implements Serializable {
    /**
     * 日志标识
     */
    private Integer id;
    /**
     * 日志来源
     */
    private String source;
    /**
     * 报表ID
     */
    private String reportId;
    /**
     * 报表名称
     */
    private String reportName;
    /**
     * 操作用户id
     */
    private Integer userId;
    /**
     * 操作用户id
     */
    private String userName;
    /**
     * 操作用户账号
     */
    private String account;
    /**
     * 日志级别
     */
    private String level;
    /**
     * 日志信息
     */
    private String message;
    /**
     * url
     */
    private String url;
    /**
     * 日志发生的时间
     */
    private Date gmtCreated;
}
