package com.care.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.care.common.result.Result;
import com.care.common.result.PageResult;
import com.care.module.product.entity.ProductParam;
import com.care.module.product.mapper.ProductParamMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/product/param")
public class ProductParamController {

    @Resource
    private ProductParamMapper productParamMapper;

    @GetMapping("/page")
    public Result<PageResult<ProductParam>> getPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                                    @RequestParam(required = false) String serviceType,
                                                    @RequestParam(required = false) String name) {
        Page<ProductParam> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductParam> wrapper = new LambdaQueryWrapper<>();
        if (serviceType != null && !serviceType.isEmpty()) {
            wrapper.eq(ProductParam::getServiceType, serviceType);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(ProductParam::getName, name);
        }
        wrapper.orderByDesc(ProductParam::getCreateTime);
        IPage<ProductParam> result = productParamMapper.selectPage(page, wrapper);
        return Result.success(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @GetMapping("/list")
    public Result<List<ProductParam>> getList(@RequestParam(required = false) String serviceType) {
        LambdaQueryWrapper<ProductParam> wrapper = new LambdaQueryWrapper<>();
        if (serviceType != null && !serviceType.isEmpty()) {
            wrapper.eq(ProductParam::getServiceType, serviceType);
        }
        wrapper.eq(ProductParam::getStatus, 1);
        return Result.success(productParamMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public Result<ProductParam> getById(@PathVariable Long id) {
        return Result.success(productParamMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody ProductParam param) {
        productParamMapper.insert(param);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ProductParam param) {
        param.setId(id);
        productParamMapper.updateById(param);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productParamMapper.deleteById(id);
        return Result.success();
    }
}
