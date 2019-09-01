package com.permission.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.permission.dao.SysDeptMapper;
import com.permission.dto.DeptLevelDto;
import com.permission.dto.SysDept;
import com.permission.util.LeverUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class SysTreeService {
    @Autowired
    private SysDeptMapper sysDeptMapper;

    public List<DeptLevelDto> deptTree() {
        List<SysDept> allDept = sysDeptMapper.getAllDept();
        List<DeptLevelDto> deptLevelDtos = new ArrayList<>();
        for (SysDept sysDept : allDept) {
            DeptLevelDto dto = DeptLevelDto.adapt(sysDept);
            deptLevelDtos.add(dto);
        }
        return deptListTotree(deptLevelDtos);
    }

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
                deptLevelDto.setDeptLevelDtos(tempDeptList);
                // 进入到下一层处理
                transformDeptTree(tempDeptList, nextLever, levelDeptMap);
            }
        }
    }
    // 排序方法
    public Comparator<DeptLevelDto> deptLevelDtoComparator = new Comparator<DeptLevelDto>() {
        @Override
        public int compare(DeptLevelDto o1, DeptLevelDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };
}
