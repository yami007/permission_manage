package com.permission.service;

import com.google.common.base.Preconditions;
import com.permission.common.RequestHolder;
import com.permission.dao.SysAclMapper;
import com.permission.dao.SysAclModuleMapper;
import com.permission.model.SysAclModule;
import com.permission.exception.ParamException;
import com.permission.param.AclModuleParam;
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
 * 权限模块业务类
 *
 * @author YAMI
 */
@Service
public class SysAclModuleService {

    @Autowired
    private SysAclModuleMapper sysAclModuleMapper;
    @Autowired
    private SysAclMapper sysAclMapper;

    /**
     * 保存
     *
     * @param param
     */
    public void save(AclModuleParam param) {
        BeanValidator.check(param);
        if (checkExist(param.getParentId(), param.getName(), param.getId())) {
            throw new ParamException("同一层级下存在相同名称的权限模块");
        }
        SysAclModule aclModule = SysAclModule.builder().name(param.getName()).parentId(param.getParentId()).seq(param.getSeq())
                .status(param.getStatus()).remark(param.getRemark()).build();
        aclModule.setLever(LeverUtil.calculateLever(getLevel(param.getParentId()), param.getParentId()));
        aclModule.setOperator(RequestHolder.getCurrentUser().getUsername());
        aclModule.setOperatorIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        aclModule.setOperatorTime(new Date());
        sysAclModuleMapper.insertSelective(aclModule);
    }

    public void update(AclModuleParam param) {
        BeanValidator.check(param);
        if (checkExist(param.getParentId(), param.getName(), param.getId())) {
            throw new ParamException("同一层级下存在相同名称的权限模块");
        }
        SysAclModule before = sysAclModuleMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(before, "待更新的权限模块不存在");

        SysAclModule after = SysAclModule.builder().id(param.getId()).name(param.getName()).parentId(param.getParentId()).seq(param.getSeq())
                .status(param.getStatus()).remark(param.getRemark()).build();
        after.setLever(LeverUtil.calculateLever(getLevel(param.getParentId()), param.getParentId()));
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperatorIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperatorTime(new Date());

        updateWithChild(before, after);

    }

    @Transactional
    public void updateWithChild(SysAclModule before, SysAclModule after) {
        String newLevelPrefix = after.getLever();
        String oldLevelPrefix = before.getLever();
        if (!after.getLever().equals(before.getLever())) {
            List<SysAclModule> aclModuleList = new ArrayList<>();
            getChildAclModuleListByLevel(oldLevelPrefix, before.getId(), aclModuleList);
            if (CollectionUtils.isNotEmpty(aclModuleList)) {
                for (SysAclModule aclModule : aclModuleList) {
                    String lever = aclModule.getLever();
                    if (lever.indexOf(oldLevelPrefix) == 0) {
                        lever = newLevelPrefix + lever.substring(oldLevelPrefix.length());
                        aclModule.setLever(lever);
                        sysAclModuleMapper.updateLevel(aclModule);
                    }
                }

            }
        }
        sysAclModuleMapper.updateByPrimaryKeySelective(after);
    }

    private boolean checkExist(Integer parentId, String aclModuleName, Integer deptId) {
        return sysAclModuleMapper.countByNameAndParentId(parentId, aclModuleName, deptId) > 0;
    }

    private String getLevel(Integer aclModuleId) {
        SysAclModule aclModule = sysAclModuleMapper.selectByPrimaryKey(aclModuleId);
        if (aclModule == null) {
            return null;
        }
        return aclModule.getLever();
    }

    public void delete(int aclModuleId) {
        SysAclModule aclModule = sysAclModuleMapper.selectByPrimaryKey(aclModuleId);
        Preconditions.checkNotNull(aclModule, "待删除的权限模块不存在，无法删除");
        if (sysAclModuleMapper.countByParentId(aclModule.getId()) > 0) {
            throw new ParamException("当前模块下面有子模块，无法删除");
        }
        if (sysAclMapper.countByAclModuleId(aclModule.getId()) > 0) {
            throw new ParamException("当前模块下面有权限点，无法删除");
        }
        sysAclModuleMapper.deleteByPrimaryKey(aclModuleId);
    }

    /**
     * 根据等级和id查询子权限模块
     *
     * @param lever
     * @param id
     * @return
     */
    public void getChildAclModuleListByLevel(String lever, Integer id, List<SysAclModule> aclModuleList) {
        String childLever = lever + "." + id;
        List<SysAclModule> sysAclModuleList = sysAclModuleMapper.getChildAclModuleListByLevel(childLever);
        if (CollectionUtils.isNotEmpty(sysAclModuleList)) {
            aclModuleList.addAll(sysAclModuleList);
            for (SysAclModule sysAclModule : sysAclModuleList) {
                getChildAclModuleListByLevel(sysAclModule.getLever(), sysAclModule.getId(), aclModuleList);
            }
        }
    }

}
