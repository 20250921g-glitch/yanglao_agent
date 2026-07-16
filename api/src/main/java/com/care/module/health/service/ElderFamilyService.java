package com.care.module.health.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.health.entity.ElderFamily;
import com.care.module.health.mapper.ElderFamilyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElderFamilyService extends ServiceImpl<ElderFamilyMapper, ElderFamily> {

    public List<ElderFamily> getByElderId(Long elderId) {
        return list(new LambdaQueryWrapper<ElderFamily>().eq(ElderFamily::getElderId, elderId));
    }

    public void add(ElderFamily family) {
        save(family);
    }

    public void updateFamily(ElderFamily family) {
        updateById(family);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
