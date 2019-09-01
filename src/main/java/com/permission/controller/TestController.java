package com.permission.controller;

import com.permission.common.JsonData;
import com.permission.dto.TestVo;
import com.permission.exception.PermissionException;
import com.permission.util.BeanValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class TestController {
    @RequestMapping("/hello.json")
    public JsonData hello() {
        log.info("hello");
        throw new PermissionException("yami exception");
    }

    @RequestMapping("/validateTest")
    public JsonData validateTest(TestVo vo) {
        log.info("validate");
        try {
            Map<String, String> map = BeanValidator.validateObject(vo);
            if (map != null && map.entrySet().size() > 0) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    log.info("{}->{}", entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {

        }
        return new JsonData().success("test,validate");
    }
}
