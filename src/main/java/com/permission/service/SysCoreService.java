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
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * 判断用户是否有这个url的权限
     *
     * @param url
     * @return
     */
    public Boolean hasUrlAcl(String url) {
        // 如果是超级管理员，则放行
        if (isSuperAdmin()) {
            return true;
        }
        List<SysAcl> aclList = sysAclMapper.getByUrl(url);
        // 如果这个url在权限表中没有配置，说明这个url对每个用户都可以访问
        if (CollectionUtils.isEmpty(aclList)) {
            return true;
        }
        // 获取当前登录用户的权限点
        List<SysAcl> currentUserAclList = getCurrentUserAclList();
        Set<Integer> sysAclIdSet = currentUserAclList.stream().map(sysAcl -> sysAcl.getId()).collect(Collectors.toSet());
        // 判断用户是否有某个权限点的访问权限
        // 规则：只要有一个权限点有权限，那么我们就认为有访问权限

        boolean hasValidAcl = false;
        for (SysAcl sysAcl : aclList) {
            if (sysAcl.getStatus() != 1) {
                continue;
            }
            hasValidAcl = true;
            if(sysAclIdSet.contains(sysAcl.getId())){
                return true;
            }
        }
        // 是否有有效的权限点，如果一个都没有，则说明可以访问
        if(!hasValidAcl){
            return true;
        }
        return false;
    }
}
