package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.Food;
import com.care.module.operation.service.FoodService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "运营管理-食物管理")
@RestController
@RequestMapping("/api/operation/food")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @ApiOperation("分页查询食物")
    @GetMapping("/page")
    public Result<PageResult<Food>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {
        IPage<Food> page = foodService.getPage(pageNum, pageSize, name, category);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("新增食物")
    @PostMapping
    public Result<Void> add(@RequestBody Food food) {
        foodService.add(food);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改食物")
    @PutMapping
    public Result<Void> update(@RequestBody Food food) {
        foodService.updateFood(food);
        return Result.ok("修改成功");
    }

    @ApiOperation("删除食物")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        foodService.delete(id);
        return Result.ok("删除成功");
    }
}
