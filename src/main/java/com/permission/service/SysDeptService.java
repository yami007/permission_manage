package com.permission.service;

import com.google.common.base.Preconditions;
import com.permission.common.RequestHolder;
import com.permission.dao.SysDeptMapper;
import com.permission.dao.SysUserMapper;
import com.permission.dto.SysDept;
import com.permission.exception.ParamException;
import com.permission.param.DeptParam;
import com.permission.util.BeanValidator;
import com.permission.util.IpUtil;
import com.permission.util.LeverUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 部门业务类
 *
 * @author YAMI
 */
@Service
public class SysDeptService {
    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 保存部门
     *
     * @param param
     */
    public void saveDept(DeptParam param) {
        BeanValidator.check(param);
        if (checkExist(param.getParentId(), param.getName(), param.getId())) {
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        SysDept sysDept = SysDept.builder().name(param.getName()).parentId(param.getParentId())
                .seq(param.getSeq()).remark(param.getRemark()).build();
        sysDept.setLever(LeverUtil.calculateLever(getLever(param.getParentId()), param.getParentId()));
        sysDept.setOperator(RequestHolder.getCurrentUser().getUsername());
        sysDept.setOperatorIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        sysDept.setOperatorTime(new Date());
        sysDeptMapper.insertSelective(sysDept);
    }

    /**
     * 更新部门
     *
     * @param param
     */
    public void update(DeptParam param) {
        BeanValidator.check(param);
        if (checkExist(param.getParentId(), param.getName(), param.getId())) {
            throw new ParamException("同一层级下存在相同名称的部门");
        }
        SysDept beforeSysDept = sysDeptMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(beforeSysDept, "待更新的部门不存在");

        SysDept afterSysDept = SysDept.builder().id(param.getId()).name(param.getName()).parentId(param.getParentId())
                .seq(param.getSeq()).remark(param.getRemark()).build();
        afterSysDept.setLever(LeverUtil.calculateLever(getLever(param.getParentId()), param.getParentId()));
        afterSysDept.setOperator(RequestHolder.getCurrentUser().getUsername());
        afterSysDept.setOperatorIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        afterSysDept.setOperatorTime(new Date());
        // 更新本部门及其子部门
        updateWithChild(beforeSysDept, afterSysDept);
    }

    /**
     * 更新本部门及其子部门
     *
     * @param befor
     * @param after
     */
    @Transactional
    public void updateWithChild(SysDept befor, SysDept after) {
        String beforLever = befor.getLever();
        String afterLever = after.getLever();
        if (!afterLever.equals(beforLever)) {
            // 获取所有的子部门，包括子部门的子部门
            List<SysDept> childDepts = new ArrayList<>();
            getChildDeptListByLevel(beforLever, befor.getId(),childDepts);
            if (CollectionUtils.isNotEmpty(childDepts)) {
                for (SysDept childDept : childDepts) {
                    String lever = childDept.getLever();
                    if (lever.indexOf(beforLever) == 0) {
                        lever = afterLever + lever.substring(beforLever.length());
                        childDept.setLever(lever);
                        sysDeptMapper.updataLevel(childDept);
                    }
                }
            }

        }

        sysDeptMapper.updateByPrimaryKey(after);
    }

    // 判断同一层级下是否存在相同名称的部门
    private boolean checkExist(Integer parentId, String deptNmae, Integer deptId) {
        int count = sysDeptMapper.countByNmaeAndParentId(parentId, deptNmae, deptId);
        if (count > 0) {
            return true;
        }
        return false;
    }

    // 通过id获取等级
    private String getLever(Integer parentId) {
        SysDept sysDept = sysDeptMapper.selectByPrimaryKey(parentId);
        if (sysDept == null) {
            return null;
        } else {
            return sysDept.getLever();
        }

    }

    public void delete(int deptId) {
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        Preconditions.checkNotNull(dept, "待删除的部门不存在，无法删除");
        if (sysDeptMapper.countByParentId(dept.getId()) > 0) {
            throw new ParamException("当前部门下面有子部门，无法删除");
        }
        if (sysUserMapper.countByDeptId(dept.getId()) > 0) {
            throw new ParamException("当前部门下面有用户，无法删除");
        }
        sysDeptMapper.deleteByPrimaryKey(deptId);
    }

    /**
     * 根据等级和id查询子部门
     *
     * @param lever
     * @param id
     * @return
     */
    public void getChildDeptListByLevel(String lever, Integer id,List<SysDept> sysDeptList) {
        String childLever = lever + "." + id;

        List<SysDept> childDeptListByLevel = sysDeptMapper.getChildDeptListByLevel(childLever);
        if (CollectionUtils.isNotEmpty(childDeptListByLevel)) {
            sysDeptList.addAll(childDeptListByLevel);
            for (SysDept sysDept : childDeptListByLevel) {
                getChildDeptListByLevel(sysDept.getLever(), sysDept.getId(),sysDeptList);
            }
        }
    }

}
