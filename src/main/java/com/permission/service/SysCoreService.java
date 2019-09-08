package com.permission.service;

import com.google.common.collect.Lists;
import com.permission.common.RequestHolder;
import com.permission.dao.SysAclMapper;
import com.permission.dao.SysRoleAclMapper;
import com.permission.dao.SysRoleUserMapper;
import com.permission.model.SysAcl;
import com.permission.model.SysUser;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户权限及角色权限查询service
 */
@Service
public class SysCoreService {

    @Autowired
    private SysAclMapper sysAclMapper;
    @Autowired
    private SysRoleUserMapper sysRoleUserMapper;
    @Autowired
    private SysRoleAclMapper sysRoleAclMapper;

    // 获取当前用户的权限
    public List<SysAcl> getCurrentUserAclList() {
        int userId = RequestHolder.getCurrentUser().getId();
        return getUserAclList(userId);
    }

    // 获取用户的权限
    public List<SysAcl> getUserAclList(int userId) {
        //如果该用户是超级管理员，就获取所有的权限
        if (isSuperAdmin()) {
            return sysAclMapper.getAll();
        }
        // 如果不是，就先获取用户的角色
        List<Integer> userRoleIdList = sysRoleUserMapper.getRoleIdListByUserId(userId);
        // 如果当前用户没有分配角色，则返回空
        if (CollectionUtils.isEmpty(userRoleIdList)) {
            return new ArrayList<>();
        }
        // 通过角色获取权限
        List<Integer> userAclIdList = sysRoleAclMapper.getAclIdListByRoleIdList(userRoleIdList);
        if (CollectionUtils.isEmpty(userAclIdList)) {
            return Lists.newArrayList();
        }
        return sysAclMapper.getByIdList(userAclIdList);
    }

    // 获取角色的权限
    public List<SysAcl> getRoleAclList(int roleId) {
        List<Integer> aclIdList = sysRoleAclMapper.getAclIdListByRoleIdList(Lists.<Integer>newArrayList(roleId));
        if (CollectionUtils.isEmpty(aclIdList)) {
            return Lists.newArrayList();
        }
        return sysAclMapper.getByIdList(aclIdList);
    }

    // 是否是超级管理员
    public boolean isSuperAdmin() {
        // 这里是我自己定义了一个假的超级管理员规则，实际中要根据项目进行修改
        // 可以是配置文件获取，可以指定某个用户，也可以指定某个角色
        SysUser sysUser = RequestHolder.getCurrentUser();
        if (sysUser.getMail().contains("admin")) {
            return true;
        }
        return false;
    }

}