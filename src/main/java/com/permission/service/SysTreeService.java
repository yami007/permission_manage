package com.permission.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.permission.dao.SysAclMapper;
import com.permission.dao.SysAclModuleMapper;
import com.permission.dao.SysDeptMapper;
import com.permission.dto.AclDto;
import com.permission.dto.AclModuleLevelDto;
import com.permission.dto.DeptLevelDto;
import com.permission.model.SysAcl;
import com.permission.model.SysAclModule;
import com.permission.model.SysDept;
import com.permission.util.LeverUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 递归树业务类
 */
@Service
public class SysTreeService {
    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private SysAclModuleMapper sysAclModuleMapper;
    @Autowired
    private SysCoreService sysCoreService;
    @Autowired
    private SysAclMapper sysAclMapper;

    // ------------角色权限树，包含所有的权限模块及权限树，用户已经应有及角色已经拥有对字段赋值----------------
    public List<AclModuleLevelDto> roleTree(int roleId) {
        // 1、获取当前用户已分配的权限点
        List<SysAcl> currentUserAclList = sysCoreService.getCurrentUserAclList();
        // 2、获取所选角色当前分配的权限点
        List<SysAcl> roleAclList = sysCoreService.getRoleAclList(roleId);
        // 3、将当前用户的权限id及角色的权限点id取出来
        Set<Integer> userAclIdSet = currentUserAclList.stream().map(sysAcl -> sysAcl.getId()).collect(Collectors.toSet());
        Set<Integer> roleAclIdSet = roleAclList.stream().map(sysAcl -> sysAcl.getId()).collect(Collectors.toSet());
        // 4、获取所有权限点
        List<SysAcl> aclList = sysAclMapper.getAll();
        Set<SysAcl> aclSet = new HashSet<>(aclList);
        // 5、将权限点赋值并转换成AclDto
        List<AclDto> aclDtoList = new ArrayList<>();
        for (SysAcl sysAcl : aclSet) {
            AclDto aclDto = AclDto.adapt(sysAcl);
            // 判断当前用户是否有这个权限
            if (userAclIdSet.contains(sysAcl.getId())) {
                aclDto.setHasAcl(true);
            }
            // 判断当前角色是否有这个权限
            if (roleAclIdSet.contains(sysAcl.getId())) {
                aclDto.setChecked(true);
            }
            aclDtoList.add(aclDto);
        }
        // 6、转换成权限模块树
        return aclListToTree(aclDtoList);
    }

    // 将权限点绑定到权限模块树上
    public List<AclModuleLevelDto> aclListToTree(List<AclDto> aclDtoList) {
        if (CollectionUtils.isEmpty(aclDtoList)) {
            return new ArrayList<>();
        }
        // 查出所有权限模块树
        List<AclModuleLevelDto> aclModuleTree = aclModuleTree();
        // 将权限点转成权限点map
        Multimap<Integer, AclDto> aclDtoMultimap = ArrayListMultimap.create();
        for (AclDto aclDto : aclDtoList) {
            if (aclDto.getStatus() == 1) {
                aclDtoMultimap.put(aclDto.getAclModuleId(), aclDto);
            }
        }
        // 将权限点绑定到权限模块树上
        bindAclsWithMoudke(aclModuleTree, aclDtoMultimap);
        return aclModuleTree;
    }

    // 将权限点绑定到权限模块树上
    public void bindAclsWithMoudke(List<AclModuleLevelDto> aclModuleTree, Multimap<Integer, AclDto> aclDtoMultimap) {
        if (CollectionUtils.isEmpty(aclModuleTree)) {
            return;
        }
        // 将权限点放置到对应的权限模块下
        for (AclModuleLevelDto aclModuleLevelDto : aclModuleTree) {
            List<AclDto> aclDtos = (List<AclDto>) aclDtoMultimap.get(aclModuleLevelDto.getId());
            if (CollectionUtils.isNotEmpty(aclDtos)) {
                Collections.sort(aclDtos, aclSeqComparator);
                aclModuleLevelDto.setAclList(aclDtos);
            }
            bindAclsWithMoudke(aclModuleLevelDto.getAclModuleList(), aclDtoMultimap);
        }
    }

    // ------------部门树处理----------------
    // 查询出所有的部门
    public List<DeptLevelDto> deptTree() {
        List<SysDept> allDept = sysDeptMapper.getAllDept();
        List<DeptLevelDto> deptLevelDtos = new ArrayList<>();
        for (SysDept sysDept : allDept) {
            DeptLevelDto dto = DeptLevelDto.adapt(sysDept);
            deptLevelDtos.add(dto);
        }
        return deptListTotree(deptLevelDtos);
    }

    // 将deptLevelList转换成树
    public List<DeptLevelDto> deptListTotree(List<DeptLevelDto> deptLevelList) {
        if (CollectionUtils.isEmpty(deptLevelList)) {
            return new ArrayList<>();
        }
        Multimap<String, DeptLevelDto> levelDeptMap = ArrayListMultimap.create();
        List<DeptLevelDto> rootList = new ArrayList<>();

        for (DeptLevelDto dto : deptLevelList) {
            levelDeptMap.put(dto.getLever(), dto);
            if (LeverUtil.ROOT.equals(dto.getLever())) {
                rootList.add(dto);
            }
        }
        // 按照seq从小到大排序
        Collections.sort(rootList, new Comparator<DeptLevelDto>() {
            @Override
            public int compare(DeptLevelDto o1, DeptLevelDto o2) {
                return o1.getSeq() - o2.getSeq();
            }
        });
        // 递归生成树
        transformDeptTree(rootList, LeverUtil.ROOT, levelDeptMap);
        return rootList;
    }

    // 递归方法
    public void transformDeptTree(List<DeptLevelDto> deptLevelList, String lverl, Multimap<String, DeptLevelDto> levelDeptMap) {
        for (int i = 0; i < deptLevelList.size(); i++) {
            // 遍历该层的每个元素
            DeptLevelDto deptLevelDto = deptLevelList.get(i);
            // 处理当前层级的数据
            String nextLever = LeverUtil.calculateLever(lverl, deptLevelDto.getId());
            // 处理下一层
            List<DeptLevelDto> tempDeptList = (List<DeptLevelDto>) levelDeptMap.get(nextLever);
            if (CollectionUtils.isNotEmpty(tempDeptList)) {
                // 排序
                Collections.sort(tempDeptList, deptLevelDtoComparator);
                // 设置下一层部门
                deptLevelDto.setDeptList(tempDeptList);
                // 进入到下一层处理
                transformDeptTree(tempDeptList, nextLever, levelDeptMap);
            }
        }
    }
    // ------------权限模块树处理----------------

    // 查询出所有的权限模块
    public List<AclModuleLevelDto> aclModuleTree() {
        List<SysAclModule> aclModuleList = sysAclModuleMapper.getAllAclModule();
        if (CollectionUtils.isEmpty(aclModuleList)) {
            return new ArrayList<>();
        } else {
            List<AclModuleLevelDto> aclModuleLevelDtos = new ArrayList<>();
            for (SysAclModule sysAclModule : aclModuleList) {
                AclModuleLevelDto dto = AclModuleLevelDto.adapt(sysAclModule);
                aclModuleLevelDtos.add(dto);
            }
            List<AclModuleLevelDto> aclModuleLevelDtoList = aclModuleListToTree(aclModuleLevelDtos);
            return aclModuleLevelDtoList;
        }
    }

    // 将dtoList转换成树
    public List<AclModuleLevelDto> aclModuleListToTree(List<AclModuleLevelDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return new ArrayList<>();
        } else {
            List<AclModuleLevelDto> aclModuleLevelDtoList = new ArrayList<>();
            Multimap<String, AclModuleLevelDto> levelDeptMap = ArrayListMultimap.create();
            for (AclModuleLevelDto dto : dtoList) {
                levelDeptMap.put(dto.getLever(), dto);
                if (LeverUtil.ROOT.equals(dto.getLever())) {
                    aclModuleLevelDtoList.add(dto);
                }
            }
            aclModuleLevelDtoList.sort(aclModuleSeqComparator);
            transformAclModuleTree(aclModuleLevelDtoList, LeverUtil.ROOT, levelDeptMap);
            return aclModuleLevelDtoList;
        }
    }

    // 递归转换
    public void transformAclModuleTree(List<AclModuleLevelDto> dtoList, String level, Multimap<String, AclModuleLevelDto> levelAclModuleMap) {
        if (CollectionUtils.isNotEmpty(dtoList)) {
            for (AclModuleLevelDto aclModuleLevelDto : dtoList) {
                Integer parentId = aclModuleLevelDto.getId();
                // 通过父类的等级及id获取子类的等级
                String childLever = LeverUtil.calculateLever(level, parentId);
                // 通过子的等级，从map中获取子集合
                List<AclModuleLevelDto> childAclModuleLevelDtos = (List<AclModuleLevelDto>) levelAclModuleMap.get(childLever);
                // 如果包含子集合，就set进父权限中，并排序，再递归转换
                if (CollectionUtils.isNotEmpty(childAclModuleLevelDtos)) {
                    aclModuleLevelDto.setAclModuleList(childAclModuleLevelDtos);
                    childAclModuleLevelDtos.sort(aclModuleSeqComparator);
                    transformAclModuleTree(childAclModuleLevelDtos, childLever, levelAclModuleMap);
                }
            }
        }
    }

    // 部门排序方法
    public Comparator<DeptLevelDto> deptLevelDtoComparator = new Comparator<DeptLevelDto>() {
        @Override
        public int compare(DeptLevelDto o1, DeptLevelDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };
    // 权限模块排序方法
    public Comparator<AclModuleLevelDto> aclModuleSeqComparator = new Comparator<AclModuleLevelDto>() {
        public int compare(AclModuleLevelDto o1, AclModuleLevelDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };
    // 权限点排序方法
    public Comparator<AclDto> aclSeqComparator = new Comparator<AclDto>() {
        public int compare(AclDto o1, AclDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };

}
