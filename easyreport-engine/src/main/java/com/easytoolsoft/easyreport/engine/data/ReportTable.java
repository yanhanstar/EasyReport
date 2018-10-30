package com.easytoolsoft.easyreport.engine.data;

import java.util.List;

/**
 * 报表类
 *
 * @author tomdeng
 */
public class ReportTable {
    private final String htmlText;
    private final String sqlText;
    private final int metaDataRowCount;
    private final int metaDataColumnCount;
    private final List<ReportMetaDataColumn> metaDataColumns;
    private final List<ReportMetaDataRow> metaDataRows;

    public ReportTable(final String htmlText, final String sqlText, final int metaDataRowCount,
                       final int metaDataColumnCount, List<ReportMetaDataColumn> metaDataColumns, List<ReportMetaDataRow> metaDataRows) {
        this.htmlText = htmlText;
        this.sqlText = sqlText;
        this.metaDataRowCount = metaDataRowCount;
        this.metaDataColumnCount = metaDataColumnCount;
        this.metaDataColumns = metaDataColumns;
        this.metaDataRows = metaDataRows;
    }

    public String getHtmlText() {
        return this.htmlText;
    }

    public String getSqlText() {
        return this.sqlText;
    }

    public long getMetaDataRowCount() {
        return this.metaDataRowCount;
    }

    public int getMetaDataColumnCount() {
        return this.metaDataColumnCount;
    }

    public List<ReportMetaDataRow> getMetaDataRows() {
        return this.metaDataRows;
    }

    public List<ReportMetaDataColumn> getMetaDataColumns() {
        return this.metaDataColumns;
    }
}
