package com.easytoolsoft.easyreport.web.util;

import com.easytoolsoft.easyreport.engine.data.*;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;

import com.easytoolsoft.easyreport.engine.query.Queryer;
import com.easytoolsoft.easyreport.engine.util.DateUtils;
import com.easytoolsoft.easyreport.meta.domain.Report;
import com.easytoolsoft.easyreport.meta.domain.options.ReportOptions;
import com.easytoolsoft.easyreport.meta.form.QueryParamFormView;
import com.easytoolsoft.easyreport.meta.form.control.HtmlFormElement;
import com.easytoolsoft.easyreport.meta.service.ReportService;
import com.easytoolsoft.easyreport.meta.service.TableReportService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Tom Deng
 * @date 2017-03-25
 */
@Component
public class ReportUtils {
    private static ReportService reportService;
    private static TableReportService tableReportService;

	public static String DEFAULT_DATE_PATTERN="yyyy-MM-dd HH:mm:ss";//默认日期格式
	public static int DEFAULT_COLOUMN_WIDTH = 17;

    @Autowired
    public ReportUtils(final ReportService reportService, final TableReportService tableReportService) {
        ReportUtils.reportService = reportService;
        ReportUtils.tableReportService = tableReportService;
    }

    public static Report getReportMetaData(final String uid) {
        return reportService.getByUid(uid);
    }

    public static JSONObject getDefaultChartData() {
        return new JSONObject(6) {
            {
                put("dimColumnMap", null);
                put("dimColumns", null);
                put("statColumns", null);
                put("dataRows", null);
                put("msg", "");
            }
        };
    }

    public static ReportDataSource getReportDataSource(final Report report) {
        return reportService.getReportDataSource(report.getDsId());
    }

    public static ReportParameter getReportParameter(final Report report, final Map<?, ?> parameters) {
        return tableReportService.getReportParameter(report, parameters);
    }

    public static void renderByFormMap(final String uid, final ModelAndView modelAndView,
                                       final HttpServletRequest request) {
        final Report report = reportService.getByUid(uid);
        final ReportOptions options = reportService.parseOptions(report.getOptions());
        final Map<String, Object> buildInParams = tableReportService.getBuildInParameters(request.getParameterMap(),
            options.getDataRange());
        final Map<String, HtmlFormElement> formMap = tableReportService.getFormElementMap(report, buildInParams, 1);
        modelAndView.addObject("formMap", formMap);
        modelAndView.addObject("uid", uid);
        modelAndView.addObject("id", report.getId());
        modelAndView.addObject("name", report.getName());
    }

    public static void renderByTemplate(final String uid, final ModelAndView modelAndView,
                                        final QueryParamFormView formView,
                                        final HttpServletRequest request) {
        final Report report = reportService.getByUid(uid);
        final ReportOptions options = reportService.parseOptions(report.getOptions());
        options.setLayout(LayoutType.HORIZONTAL.getValue());
        final List<ReportMetaDataColumn> metaDataColumns = reportService.parseMetaColumns(report.getMetaColumns());
        final Map<String, Object> buildInParams = tableReportService.getBuildInParameters(request.getParameterMap(),
            options.getDataRange());
        final List<HtmlFormElement> dateAndQueryElements = tableReportService.getDateAndQueryParamFormElements(report,
            buildInParams);
        final HtmlFormElement statColumnFormElements = tableReportService.getStatColumnFormElements(metaDataColumns, 0);
        final List<HtmlFormElement> nonStatColumnFormElements = tableReportService.getNonStatColumnFormElements(
            metaDataColumns);
        modelAndView.addObject("uid", uid);
        modelAndView.addObject("id", report.getId());
        modelAndView.addObject("name", report.getName());
        modelAndView.addObject("comment", report.getComment().trim());
        modelAndView.addObject("formHtmlText", formView.getFormHtmlText(dateAndQueryElements));
        modelAndView.addObject("statColumHtmlText", formView.getFormHtmlText(statColumnFormElements));
        modelAndView.addObject("nonStatColumHtmlText", formView.getFormHtmlText(nonStatColumnFormElements));
    }

    public static void generate(final String uid, final JSONObject data, final HttpServletRequest request) {
        generate(uid, data, request.getParameterMap());
    }

    public static void generate(final String uid, final JSONObject data, final Map<?, ?> parameters) {
        generate(uid, data, new HashMap<>(0), parameters);
    }

    public static void generate(final String uid, final JSONObject data, final Map<String, Object> attachParams,
                                final Map<?, ?> parameters) {
        if (StringUtils.isBlank(uid)) {
            data.put("htmlTable", "uid参数为空导致数据不能加载！");
            return;
        }
        final ReportTable reportTable = generate(uid, attachParams, parameters);
        data.put("htmlTable", reportTable.getHtmlText());
        data.put("metaDataRowCount", reportTable.getMetaDataRowCount());
        data.put("metaDataColumnCount", reportTable.getMetaDataColumnCount());
    }

    public static void generate(final Queryer queryer, final ReportParameter reportParameter, final JSONObject data) {
        final ReportTable reportTable = tableReportService.getReportTable(queryer, reportParameter);
        data.put("htmlTable", reportTable.getHtmlText());
        data.put("metaDataRowCount", reportTable.getMetaDataRowCount());
    }

    public static void generate(final ReportMetaDataSet metaDataSet, final ReportParameter reportParameter,
                                final JSONObject data) {
        final ReportTable reportTable = tableReportService.getReportTable(metaDataSet, reportParameter);
        data.put("htmlTable", reportTable.getHtmlText());
        data.put("metaDataRowCount", reportTable.getMetaDataRowCount());
    }

    public static ReportTable generate(final String uid, final Map<?, ?> parameters) {
        return generate(uid, new HashMap<>(0), parameters);
    }

    public static ReportTable generate(final String uid, final Map<String, Object> attachParams,
                                       final Map<?, ?> parameters) {
        final Report report = reportService.getByUid(uid);
        final ReportOptions options = reportService.parseOptions(report.getOptions());
        final Map<String, Object> formParams = tableReportService.getFormParameters(parameters, options.getDataRange());
        if (MapUtils.isNotEmpty(attachParams)) {
            for (final Entry<String, Object> es : attachParams.entrySet()) {
                formParams.put(es.getKey(), es.getValue());
            }
        }
        return tableReportService.getReportTable(report, formParams);
    }

    public static void exportToExcel(final String uid, final String name, String htmlText,
                                     final HttpServletRequest request,
                                     final HttpServletResponse response) {
        try (OutputStream out = response.getOutputStream()) {
            String fileName = name + "_" + DateUtils.getNow("yyyyMMddHHmmss");
            fileName = new String(fileName.getBytes(), "ISO8859-1") + ".xls";
            if ("large".equals(htmlText)) {
                final Report report = reportService.getByUid(uid);
                final ReportOptions options = reportService.parseOptions(report.getOptions());
                final Map<String, Object> formParameters = tableReportService.getFormParameters(
                    request.getParameterMap(),
                    options.getDataRange());
                final ReportTable reportTable = tableReportService.getReportTable(report, formParameters);
                htmlText = reportTable.getHtmlText();
            }
            response.reset();
            response.setHeader("Content-Disposition", String.format("attachment; filename=%s", fileName));
            response.setContentType("application/vnd.ms-excel; charset=utf-8");
            //response.addCookie(new Cookie("fileDownload", "true"));
            response.setHeader("Set-Cookie", "fileDownload=true; path=/");
            //out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}); // 生成带bom的utf8文件
            out.write(htmlText.getBytes());
            out.flush();
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void exportToExcelNew(final String uid, final String title,
										final HttpServletRequest request, final HttpServletResponse resp){
		try {
			//查询数据List
			final Report report = reportService.getByUid(uid);
			final ReportOptions options = reportService.parseOptions(report.getOptions());
			final Map<String, Object> formParameters = tableReportService.getFormParameters(
					request.getParameterMap(),
					options.getDataRange());
			final ReportTable reportTable = tableReportService.getReportTable(report, formParameters);
			List<ReportMetaDataColumn> metaDataColumns = reportTable.getMetaDataColumns();
			List<ReportMetaDataRow> metaDataRows = reportTable.getMetaDataRows();

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			exportExcelX(title,metaDataColumns,metaDataRows,null,0,os);
			byte[] content = os.toByteArray();
			InputStream is = new ByteArrayInputStream(content);

			// 设置response参数，可以打开下载页面
			String fileName = title + "_" + DateUtils.getNow("yyyyMMddHHmmss") + ".xlsx";
			resp.reset();
			resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");//设置类型
			resp.setHeader("Content-disposition","attachment;filename="
					+new String(fileName.getBytes("gb2312"),"ISO8859-1"));    //设置文件头编码格式
			resp.setHeader("Set-Cookie", "fileDownload=true; path=/");
			resp.setDateHeader("Expires", 0);//设置日期头
			resp.setContentLength(content.length);

			ServletOutputStream outputStream = resp.getOutputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			BufferedOutputStream bos = new BufferedOutputStream(outputStream);
			byte[] buff = new byte[8192];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);

			}
			bis.close();
			bos.close();
			outputStream.flush();
			outputStream.close();
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
    }

	/**
	 * 导出Excel 2007 OOXML (.xlsx)格式
	 * @param title 标题行
	 * @param metaDataColumns 表格列头
	 * @param metaDataRows 数据集
	 * @param datePattern 日期格式，传null值则默认 年月日
	 * @param colWidth 列宽 默认 至少17个字节
	 * @param out 输出流
	 */
	public static void exportExcelX(String title,List<ReportMetaDataColumn> metaDataColumns,List<ReportMetaDataRow> metaDataRows,
									String datePattern,int colWidth, OutputStream out) {
		if(datePattern==null) datePattern = DEFAULT_DATE_PATTERN;
		// 声明一个工作薄
		SXSSFWorkbook workbook = new SXSSFWorkbook(1000);//缓存
		workbook.setCompressTempFiles(true);
		//表头样式
		CellStyle titleStyle = workbook.createCellStyle();
		titleStyle.setBorderBottom(BorderStyle.THIN);
		titleStyle.setBorderLeft(BorderStyle.THIN);
		titleStyle.setBorderRight(BorderStyle.THIN);
		titleStyle.setBorderTop(BorderStyle.THIN);
		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		Font titleFont = workbook.createFont();
		titleFont.setFontHeightInPoints((short) 20);
		titleFont.setBold(true);
		titleStyle.setFont(titleFont);
		// 列头样式
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIME.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		Font headerFont = workbook.createFont();
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setBold(true);
		headerStyle.setFont(headerFont);
		// 单元格样式
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		//建立新的sheet对象（excel的表单）
		//生成一个(带标题)表格
		SXSSFSheet sheet = workbook.createSheet();
		//设置列宽
		int minBytes = colWidth<DEFAULT_COLOUMN_WIDTH?DEFAULT_COLOUMN_WIDTH:colWidth;//至少字节数
		int[] arrColWidth = new int[metaDataColumns.size()];
		// 产生表格标题行,以及设置列宽
		String[] properties = new String[metaDataColumns.size()];
		String[] headers = new String[metaDataColumns.size()];
		int ii = 0;
		for (ReportMetaDataColumn column : metaDataColumns) {
			properties[ii] = column.getName();
			headers[ii] = column.getText();

			int bytes = column.getName().getBytes().length;
			arrColWidth[ii] =  bytes < minBytes ? minBytes : bytes;
			sheet.setColumnWidth(ii,arrColWidth[ii]*256);
			ii++;
		}
		// 遍历集合数据，产生数据行
		int rowIndex = 0;
		for (ReportMetaDataRow obj : metaDataRows) {
			if(rowIndex == 65535 || rowIndex == 0){
				if ( rowIndex != 0 ) sheet = workbook.createSheet();//如果数据超过了，则在第二页显示

				SXSSFRow titleRow = sheet.createRow(0);//表头 rowIndex=0
				titleRow.createCell(0).setCellValue(title);
				titleRow.getCell(0).setCellStyle(titleStyle);
				sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, metaDataColumns.size() - 1));

				SXSSFRow headerRow = sheet.createRow(1); //列头 rowIndex =1
				for(int i=0;i<headers.length;i++)
				{
					headerRow.createCell(i).setCellValue(headers[i]);
					headerRow.getCell(i).setCellStyle(headerStyle);

				}
				rowIndex = 2;//数据内容从 rowIndex=2开始
			}
			JSONObject jcell = (JSONObject) JSONObject.toJSON(obj.getCells());
			SXSSFRow dataRow = sheet.createRow(rowIndex);
			for (int i = 0; i < properties.length; i++)
			{
				SXSSFCell newCell = dataRow.createCell(i);

				JSONObject jo = (JSONObject) JSONObject.toJSON(jcell.get(properties[i]));
				Object o =  jo.get("value");
				String cellValue = "";
				if(o==null) cellValue = "";
				else if(o instanceof Date) cellValue = new SimpleDateFormat(datePattern).format(o);
				else if(o instanceof Float || o instanceof Double)
					cellValue= new BigDecimal(o.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
				else cellValue = o.toString();

				newCell.setCellValue(cellValue);
				newCell.setCellStyle(cellStyle);
			}
			rowIndex++;
		}
		// 自动调整宽度
		/*for (int i = 0; i < headers.length; i++) {
			sheet.autoSizeColumn(i);
		}*/
		try {
			workbook.write(out);
			workbook.close();
			workbook.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
