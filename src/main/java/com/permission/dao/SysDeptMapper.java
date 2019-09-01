package com.permission.dao;

import com.permission.dto.SysDept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysDeptMapper {
    int deleteByPrimaryKey(@Param("id")Integer id);

    int insert(SysDept record);

    int insertSelective(SysDept record);

    SysDept selectByPrimaryKey(@Param("id")Integer id);

    int updateByPrimaryKeySelective(SysDept record);

    int updateByPrimaryKey(SysDept record);

    List<SysDept> getAllDept();
    // 获取所有的子部门，包括子部门的子部门
    List<SysDept> getChildDeptListByLevel(@Param("lever") String lever);
    //批量更新level
    void updataLevel(SysDept sysDept);

    Integer countByNmaeAndParentId(@Param("parentId") Integer parentId, @Param("name")String name, @Param("id") Integer deptId);
}