package com.permission.param;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class DeptParam {
    //部门id
    private Integer id;
    //部门名称
    @NotBlank(message = "部门名称不能为空")
    @Length(max = 15,min = 2,message = "部门名称长度需在2-15个之间")
    private String name;
    //上级部门ID
    private Integer parentId = 0;
    //部门在当前层级下的顺序，由小到大
    @NotNull(message = "展示顺序不能为空")
    private Integer seq;
    //备注
    @Length(max = 150,message = "备注的长度需在150个字以内")
    private String remark;
}
