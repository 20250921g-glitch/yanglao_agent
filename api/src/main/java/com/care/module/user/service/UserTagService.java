package com.care.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.user.entity.UserTag;
import com.care.module.user.mapper.UserTagMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTagService extends ServiceImpl<UserTagMapper, UserTag> {

    public IPage<UserTag> getPage(Integer pageNum, Integer pageSize, String tagName, String tagType) {
        Page<UserTag> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserTag> wrapper = new LambdaQueryWrapper<>();
        if (tagName != null && !tagName.isEmpty()) {
            wrapper.like(UserTag::getTagName, tagName);
        }
        if (tagType != null && !tagType.isEmpty()) {
            wrapper.eq(UserTag::getTagType, tagType);
        }
        wrapper.orderByDesc(UserTag::getId);
        return page(page, wrapper);
    }

    public List<UserTag> getAllList() {
        return list();
    }

    public void add(UserTag tag) {
        save(tag);
    }

    public void updateTag(UserTag tag) {
        updateById(tag);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
