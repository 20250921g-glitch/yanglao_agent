package com.care.module.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.user.entity.Coupon;
import com.care.module.user.service.CouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "用户管理-优惠券")
@RestController
@RequestMapping("/api/user/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @ApiOperation("分页查询优惠券")
    @GetMapping("/page")
    public Result<PageResult<Coupon>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        IPage<Coupon> page = couponService.getPage(pageNum, pageSize, name, type, status);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("新增优惠券")
    @PostMapping
    public Result<Void> add(@RequestBody Coupon coupon) {
        couponService.add(coupon);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改优惠券")
    @PutMapping
    public Result<Void> update(@RequestBody Coupon coupon) {
        couponService.updateCoupon(coupon);
        return Result.ok("修改成功");
    }

    @ApiOperation("上架/下架优惠券")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        couponService.updateStatus(id, status);
        return Result.ok(status == 1 ? "上架成功" : "下架成功");
    }

    @ApiOperation("删除优惠券")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return Result.ok("删除成功");
    }
}
