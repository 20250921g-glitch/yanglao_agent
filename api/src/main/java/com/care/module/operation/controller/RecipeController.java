package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.Recipe;
import com.care.module.operation.service.RecipeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "食谱管理")
@RestController
@RequestMapping("/api/operation/recipe")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @ApiOperation("食谱列表")
    @GetMapping("/page")
    public Result<PageResult<Recipe>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        IPage<Recipe> page = recipeService.getPage(pageNum, pageSize, category, status);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("食谱列表(不分页)")
    @GetMapping("/list")
    public Result<List<Recipe>> getAll(@RequestParam(required = false) Integer status) {
        return Result.success(recipeService.getAll(status));
    }

    @ApiOperation("食谱详情")
    @GetMapping("/{id}")
    public Result<Recipe> getById(@PathVariable Long id) {
        Recipe recipe = recipeService.getById(id);
        if (recipe != null) {
            recipeService.fillText(recipe);
        }
        return Result.success(recipe);
    }

    @ApiOperation("新增食谱")
    @PostMapping
    public Result<Void> add(@RequestBody Recipe recipe) {
        if (recipe.getStatus() == null) {
            recipe.setStatus(1);
        }
        recipeService.save(recipe);
        return Result.ok("新增成功");
    }

    @ApiOperation("更新食谱")
    @PutMapping
    public Result<Void> update(@RequestBody Recipe recipe) {
        recipeService.updateById(recipe);
        return Result.ok("更新成功");
    }

    @ApiOperation("删除食谱")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        recipeService.removeById(id);
        return Result.ok("删除成功");
    }

    @ApiOperation("修改状态")
    @PutMapping("/status/{id}")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        Recipe recipe = recipeService.getById(id);
        if (recipe == null) {
            return Result.error("记录不存在");
        }
        recipe.setStatus(status);
        recipeService.updateById(recipe);
        return Result.ok("状态已修改");
    }
}