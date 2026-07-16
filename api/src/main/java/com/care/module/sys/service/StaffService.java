package com.care.module.sys.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.sys.entity.Staff;
import com.care.module.sys.mapper.StaffMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StaffService extends ServiceImpl<StaffMapper, Staff> {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public IPage<Staff> getPage(Integer pageNum, Integer pageSize, String name, String dept, Integer status) {
        Page<Staff> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) wrapper.like(Staff::getName, name);
        if (StringUtils.hasText(dept)) wrapper.eq(Staff::getDept, dept);
        if (status != null) wrapper.eq(Staff::getStatus, status);
        wrapper.orderByDesc(Staff::getCreateTime);
        return page(page, wrapper);
    }

    public void add(Staff staff) {
        if (StringUtils.hasText(staff.getPassword())) {
            staff.setPassword(encoder.encode(staff.getPassword()));
        }
        save(staff);
    }

    public void updateStaff(Staff staff) {
        if (staff.getPassword() != null && !staff.getPassword().isEmpty() && !staff.getPassword().startsWith("$2")) {
            staff.setPassword(encoder.encode(staff.getPassword()));
        }
        updateById(staff);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
