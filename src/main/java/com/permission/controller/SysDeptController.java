package com.permission.controller;

import com.permission.common.JsonData;
import com.permission.dto.DeptLevelDto;
import com.permission.param.DeptParam;
import com.permission.service.SysDeptService;
import com.permission.service.SysTreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/sys/dept")
@Slf4j
public class SysDeptController {
    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private SysTreeService sysTreeService;

    @RequestMapping("/save.json")
    @ResponseBody
    public JsonData saveDept(@RequestBody DeptParam deptParam) {
        sysDeptService.saveDept(deptParam);
        return JsonData.success();
    }

    @RequestMapping("/tree.json")
    @ResponseBody
    public JsonData tree() {
        List<DeptLevelDto> deptLevelDtos = sysTreeService.deptTree();
        return JsonData.success(deptLevelDtos);
    }

    @RequestMapping("/updateDept.json")
    @ResponseBody
    public JsonData updateDept(@RequestBody DeptParam deptParam) {
        sysDeptService.update(deptParam);
        return JsonData.success();
    }
}
