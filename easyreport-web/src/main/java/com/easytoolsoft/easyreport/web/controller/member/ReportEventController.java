package com.easytoolsoft.easyreport.web.controller.member;

import com.easytoolsoft.easyreport.membership.domain.ReportEvent;
import com.easytoolsoft.easyreport.membership.domain.example.ReportEventExample;
import com.easytoolsoft.easyreport.membership.service.ReportEventService;
import com.easytoolsoft.easyreport.mybatis.pager.PageInfo;
import com.easytoolsoft.easyreport.support.annotation.OpLog;
import com.easytoolsoft.easyreport.support.model.ResponseResult;
import com.easytoolsoft.easyreport.web.controller.common.BaseController;
import com.easytoolsoft.easyreport.web.model.DataGridPager;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yanh
 * @date 2018-11-01
 */
@RestController
@RequestMapping(value = "/rest/member/reportevent")
public class ReportEventController
    extends BaseController<ReportEventService, ReportEvent, ReportEventExample, Integer> {
    @GetMapping(value = "/list")
    @OpLog(name = "分页获取报表日志列表")
    @RequiresPermissions("membership.reportevent:view")
    public Map<String, Object> list(final DataGridPager pager, final String fieldName, final String keyword) {
        final PageInfo pageInfo = pager.toPageInfo();
        final List<ReportEvent> list = this.service.getByPage(pageInfo, fieldName, "%" + keyword + "%");
        final Map<String, Object> modelMap = new HashMap<>(2);
        modelMap.put("total", pageInfo.getTotals());
        modelMap.put("rows", list);
        return modelMap;
    }

    @PostMapping(value = "/remove")
    @OpLog(name = "删除报表日志")
    @RequiresPermissions("membership.reportevent:remove")
    public ResponseResult remove(final Integer id) {
        this.service.removeById(id);
        return ResponseResult.success("");
    }

    @GetMapping(value = "/clear")
    @OpLog(name = "清除报表日志")
    @RequiresPermissions("membership.reportevent:clear")
    public ResponseResult clear() {
        this.service.clear();
        return ResponseResult.success("");
    }
}