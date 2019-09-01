package com.permission.controller;

import com.permission.common.ApplicationContextHelper;
import com.permission.common.JsonData;
import com.permission.dao.SysAclModuleMapper;
import com.permission.dto.SysAclModule;
import com.permission.dto.TestVo;
import com.permission.exception.PermissionException;
import com.permission.util.BeanValidator;
import com.permission.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {
    @RequestMapping("/hello.json")
    public JsonData hello() {
        log.info("hello");
        throw new PermissionException("yami exception");
    }

    @RequestMapping("/validateTest.json")
    public JsonData validateTest(TestVo vo) {
        log.info("validate");
        SysAclModuleMapper sysAclModuleMapper = ApplicationContextHelper.getBean(SysAclModuleMapper.class);
        SysAclModule sysAclModule = sysAclModuleMapper.selectByPrimaryKey(1);
        log.info(JsonUtils.obj2String(sysAclModule));
        BeanValidator.check(vo);
        return new JsonData().success("test,validate");
    }

}
