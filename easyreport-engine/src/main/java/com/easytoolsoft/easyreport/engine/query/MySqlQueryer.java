package com.easytoolsoft.easyreport.engine.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.easytoolsoft.easyreport.engine.data.ReportDataSource;
import com.easytoolsoft.easyreport.engine.data.ReportParameter;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import org.apache.commons.lang3.StringUtils;

/**
 * @author tomdeng
 */
public class MySqlQueryer extends AbstractQueryer implements Queryer {
    public MySqlQueryer(final ReportDataSource dataSource, final ReportParameter parameter) {
        super(dataSource, parameter);
    }

    @Override
    protected String preprocessSqlText(String sqlText) {
        sqlText = StringUtils.stripEnd(sqlText.trim(), ";");
//        18-10-16 yanh sql中需要limit作为参数进行查询，故注释
//        final Pattern pattern = Pattern.compile("limit.*?$", Pattern.CASE_INSENSITIVE);
//        final Matcher matcher = pattern.matcher(sqlText);
//        if (matcher.find()) {
//            sqlText = matcher.replaceFirst("");
//        }
        return sqlText;
    }
}
