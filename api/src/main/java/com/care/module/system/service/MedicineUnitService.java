package com.care.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.system.entity.MedicineUnit;
import com.care.module.system.mapper.MedicineUnitMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MedicineUnitService extends ServiceImpl<MedicineUnitMapper, MedicineUnit> {

    public IPage<MedicineUnit> getPage(Integer pageNum, Integer pageSize, String keyword) {
        Page<MedicineUnit> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MedicineUnit> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.like(MedicineUnit::getName, keyword);
        }
        w.orderByDesc(MedicineUnit::getCreateTime);
        return page(page, w);
    }

    public void saveUnit(MedicineUnit u) {
        if (u.getId() == null) save(u); else updateById(u);
    }

    public void updateStatus(Long id, Integer status) {
        MedicineUnit u = getById(id);
        if (u == null) throw new RuntimeException("单位不存在");
        u.setStatus(status);
        updateById(u);
    }
}
