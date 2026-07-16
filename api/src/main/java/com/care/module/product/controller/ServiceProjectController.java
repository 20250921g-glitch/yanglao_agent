package com.care.module.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.product.entity.ServiceProject;
import com.care.module.product.service.ServiceProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "商品管理-服务项目管理")
@RestController
@RequestMapping("/api/product/service-project")
public class ServiceProjectController {

    @Autowired
    private ServiceProjectService serviceProjectService;

    @ApiOperation("分页查询服务项目")
    @GetMapping("/page")
    public Result<PageResult<ServiceProject>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<ServiceProject> page = serviceProjectService.getPage(pageNum, pageSize, keyword, status);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("新增项目")
    @PostMapping
    public Result<Void> add(@RequestBody ServiceProject p) {
        serviceProjectService.saveProject(p);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改项目")
    @PutMapping
    public Result<Void> update(@RequestBody ServiceProject p) {
        serviceProjectService.saveProject(p);
        return Result.ok("修改成功");
    }

    @ApiOperation("上架/下架")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        serviceProjectService.updateStatus(id, status);
        return Result.ok("状态已更新");
    }

    @ApiOperation("删除项目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        serviceProjectService.removeProject(id);
        return Result.ok("删除成功");
    }
}
