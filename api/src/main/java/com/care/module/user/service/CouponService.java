package com.care.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.user.entity.Coupon;
import com.care.module.user.mapper.CouponMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CouponService extends ServiceImpl<CouponMapper, Coupon> {

    public IPage<Coupon> getPage(Integer pageNum, Integer pageSize, String name, Integer type, Integer status) {
        Page<Coupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Coupon::getName, name);
        }
        if (type != null) {
            wrapper.eq(Coupon::getType, type);
        }
        if (status != null) {
            wrapper.eq(Coupon::getStatus, status);
        }
        wrapper.orderByDesc(Coupon::getCreateTime);
        return page(page, wrapper);
    }

    @Transactional
    public void add(Coupon coupon) {
        if (coupon.getTotalCount() != null) {
            coupon.setRemainCount(coupon.getTotalCount());
        }
        save(coupon);
    }

    public void updateCoupon(Coupon coupon) {
        updateById(coupon);
    }

    public void updateStatus(Long id, Integer status) {
        Coupon coupon = new Coupon();
        coupon.setId(id);
        coupon.setStatus(status);
        updateById(coupon);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
