package com.permission.param;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class TestVo {
    @NotBlank   //  如果是String类型的 @NotBlank
    private String msg;

    @NotNull(message = "ID不能为空")  // 对象不能为空 @NotNull
    @Max(value = 10,message = "ID最大不能超过10")
    @Min(value = 2,message = "ID最小不能低于2")
    private String id;

    @NotEmpty // 集合不能为空 @NotEmpty
    private List<String> list;
}
