package com.care.module.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.product.entity.Product;
import com.care.module.product.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @ApiOperation("分页查询商品")
    @GetMapping("/page")
    public Result<PageResult<Product>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String serviceType) {
        IPage<Product> page = productService.getPage(pageNum, pageSize, name, categoryId, serviceType);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("新增商品")
    @PostMapping
    public Result<Void> add(@RequestBody Product product) {
        productService.add(product);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改商品")
    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        productService.updateProduct(product);
        return Result.ok("修改成功");
    }

    @ApiOperation("删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok("删除成功");
    }

    @ApiOperation("获取商品详情")
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }
}
