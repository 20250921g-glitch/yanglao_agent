package com.care.module.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.health.entity.Disease;
import com.care.module.health.service.DiseaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "疾病管理")
@RestController
@RequestMapping("/api/health/disease")
public class DiseaseController {

    @Autowired
    private DiseaseService diseaseService;

    @ApiOperation("疾病列表")
    @GetMapping("/page")
    public Result<PageResult<Disease>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        IPage<Disease> page = diseaseService.getPage(pageNum, pageSize, category, status);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("疾病列表(不分页)")
    @GetMapping("/list")
    public Result<List<Disease>> getAll(@RequestParam(required = false) Integer status) {
        return Result.success(diseaseService.getAll(status));
    }

    @ApiOperation("疾病详情")
    @GetMapping("/{id}")
    public Result<Disease> getById(@PathVariable Long id) {
        Disease disease = diseaseService.getById(id);
        if (disease != null) {
            diseaseService.fillText(disease);
        }
        return Result.success(disease);
    }

    @ApiOperation("新增疾病")
    @PostMapping
    public Result<Void> add(@RequestBody Disease disease) {
        if (disease.getStatus() == null) {
            disease.setStatus(1);
        }
        diseaseService.save(disease);
        return Result.ok("新增成功");
    }

    @ApiOperation("更新疾病")
    @PutMapping
    public Result<Void> update(@RequestBody Disease disease) {
        diseaseService.updateById(disease);
        return Result.ok("更新成功");
    }

    @ApiOperation("删除疾病")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        diseaseService.removeById(id);
        return Result.ok("删除成功");
    }

    @ApiOperation("修改状态")
    @PutMapping("/status/{id}")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        Disease disease = diseaseService.getById(id);
        if (disease == null) {
            return Result.error("记录不存在");
        }
        disease.setStatus(status);
        diseaseService.updateById(disease);
        return Result.ok("状态已修改");
    }
}