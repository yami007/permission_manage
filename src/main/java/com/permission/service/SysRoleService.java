package com.permission.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.permission.common.RequestHolder;
import com.permission.dao.SysRoleAclMapper;
import com.permission.dao.SysRoleMapper;
import com.permission.dao.SysRoleUserMapper;
import com.permission.dao.SysUserMapper;
import com.permission.exception.ParamException;
import com.permission.model.SysRole;
import com.permission.model.SysUser;
import com.permission.param.RoleParam;
import com.permission.util.BeanValidator;
import com.permission.util.IpUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysRoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysRoleUserMapper sysRoleUserMapper;
    @Autowired
    private SysRoleAclMapper sysRoleAclMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 保存新增角色
     * @param param
     */
    public void save(RoleParam param) {
        BeanValidator.check(param);
        if (checkExist(param.getName(), param.getId())) {
            throw new ParamException("角色名称已经存在");
        }
        SysRole role = SysRole.builder().name(param.getName()).status(param.getStatus()).type(param.getType())
                .remark(param.getRemark()).build();
        role.setOperator(RequestHolder.getCurrentUser().getUsername());
        role.setOperatorIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        role.setOperatorTime(new Date());
        sysRoleMapper.insertSelective(role);
    }

    /**
     * 更新角色
     * @param param
     */
    public void update(RoleParam param) {
        BeanValidator.check(param);
        if (checkExist(param.getName(), param.getId())) {
            throw new ParamException("角色名称已经存在");
        }
        SysRole before = sysRoleMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(before, "待更新的角色不存在");

        SysRole after = SysRole.builder().id(param.getId()).name(param.getName()).status(param.getStatus()).type(param.getType())
                .remark(param.getRemark()).build();
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperatorIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperatorTime(new Date());
        sysRoleMapper.updateByPrimaryKeySelective(after);
    }

    /**
     * 获取所有角色列表
     * @return
     */
    public List<SysRole> getAll() {
        return sysRoleMapper.getAll();
    }

    /**
     * 校验是否有相同名字的角色
     * @param name
     * @param id
     * @return
     */
    private boolean checkExist(String name, Integer id) {
        return sysRoleMapper.countByName(name, id) > 0;
    }

    /**
     * 通过用户id获取用户的角色
     * @param userId
     * @return
     */
    public List<SysRole> getRoleListByUserId(int userId) {
        List<Integer> roleIdList = sysRoleUserMapper.getRoleIdListByUserId(userId);
        if (CollectionUtils.isEmpty(roleIdList)) {
            return new ArrayList<>();
        }
        return sysRoleMapper.getByIdList(roleIdList);
    }

    /**
     * 通过权限id获取拥有该权限的角色
     * @param aclId
     * @return
     */
    public List<SysRole> getRoleListByAclId(int aclId) {
        List<Integer> roleIdList = sysRoleAclMapper.getRoleIdListByAclId(aclId);
        if (CollectionUtils.isEmpty(roleIdList)) {
            return Lists.newArrayList();
        }
        return sysRoleMapper.getByIdList(roleIdList);
    }

    /**
     * 通过角色list获取角色下的所有用户
     * @param roleList
     * @return
     */
    public List<SysUser> getUserListByRoleList(List<SysRole> roleList) {
        if (CollectionUtils.isEmpty(roleList)) {
            return new ArrayList<>();
        }
        List<Integer> roleIdList = roleList.stream().map(role -> role.getId()).collect(Collectors.toList());
        List<Integer> userIdList = sysRoleUserMapper.getUserIdListByRoleIdList(roleIdList);
        if (CollectionUtils.isEmpty(userIdList)) {
            return new ArrayList<>();
        }
        return sysUserMapper.getByIdList(userIdList);
    }

}
