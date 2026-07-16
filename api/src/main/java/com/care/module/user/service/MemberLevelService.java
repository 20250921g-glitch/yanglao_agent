package com.care.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.user.entity.MemberLevel;
import com.care.module.user.mapper.MemberLevelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberLevelService extends ServiceImpl<MemberLevelMapper, MemberLevel> {

    public IPage<MemberLevel> getPage(Integer pageNum, Integer pageSize, String levelName) {
        Page<MemberLevel> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        if (levelName != null && !levelName.isEmpty()) {
            wrapper.like(MemberLevel::getLevelName, levelName);
        }
        wrapper.orderByAsc(MemberLevel::getSort);
        return page(page, wrapper);
    }

    public List<MemberLevel> getAllList() {
        return list(new LambdaQueryWrapper<MemberLevel>().orderByAsc(MemberLevel::getSort));
    }

    public void add(MemberLevel level) {
        save(level);
    }

    public void updateLevel(MemberLevel level) {
        updateById(level);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
