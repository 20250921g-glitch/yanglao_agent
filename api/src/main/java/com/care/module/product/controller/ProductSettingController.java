package com.care.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.care.common.result.Result;
import com.care.module.product.entity.ProductSetting;
import com.care.module.product.mapper.ProductSettingMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product/setting")
public class ProductSettingController {

    @Resource
    private ProductSettingMapper productSettingMapper;

    @GetMapping("/list")
    public Result<Map<String, String>> getSettings() {
        List<ProductSetting> settings = productSettingMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, String> result = settings.stream()
                .collect(Collectors.toMap(ProductSetting::getSettingKey, ProductSetting::getSettingValue));
        return Result.success(result);
    }

    @GetMapping("/detail")
    public Result<List<ProductSetting>> getSettingsWithDetail() {
        return Result.success(productSettingMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @PostMapping("/save")
    public Result<Void> saveSettings(@RequestBody List<ProductSetting> settings) {
        for (ProductSetting setting : settings) {
            LambdaQueryWrapper<ProductSetting> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductSetting::getSettingKey, setting.getSettingKey());
            ProductSetting existing = productSettingMapper.selectOne(wrapper);
            if (existing != null) {
                existing.setSettingValue(setting.getSettingValue());
                productSettingMapper.updateById(existing);
            } else {
                productSettingMapper.insert(setting);
            }
        }
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ProductSetting setting) {
        setting.setId(id);
        productSettingMapper.updateById(setting);
        return Result.success();
    }
}
