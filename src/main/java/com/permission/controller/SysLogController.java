package com.permission.controller;

import com.permission.beans.PageQuery;
import com.permission.common.JsonData;
import com.permission.param.SearchLogParam;
import com.permission.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/sys/log")
public class SysLogController {

    @Autowired
    private SysLogService sysLogService;

    /**
     * 进入日志页面
     * @return
     */
    @RequestMapping("/log.page")
    public ModelAndView page() {
        return new ModelAndView("log");
    }

    /**
     * 根据操作日志还原
     * @param id
     * @return
     */
    @RequestMapping("/recover.json")
    @ResponseBody
    public JsonData recover(@RequestParam("id") int id) {
        sysLogService.recover(id);
        return JsonData.success();
    }

    /**
     * 分页查询操作日志
     * @param param
     * @param page
     * @return
     */
    @RequestMapping("/page.json")
    @ResponseBody
    public JsonData searchPage(SearchLogParam param, PageQuery page) {
        return JsonData.success(sysLogService.searchPageList(param, page));
    }
}
