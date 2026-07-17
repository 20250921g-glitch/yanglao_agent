package com.care.module.service.worker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.service.worker.entity.ServiceWorkerTag;
import com.care.module.service.worker.service.ServiceWorkerTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "服务人员标签管理")
@RestController
@RequestMapping("/api/service/worker-tag")
public class ServiceWorkerTagController {

    @Autowired
    private ServiceWorkerTagService tagService;

    @ApiOperation("分页查询标签")
    @GetMapping("/page")
    public Result<PageResult<ServiceWorkerTag>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) String serviceType) {
        IPage<ServiceWorkerTag> page = tagService.getPage(pageNum, pageSize, tagName, serviceType);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("查询所有标签")
    @GetMapping("/list")
    public Result<List<ServiceWorkerTag>> getList(@RequestParam(required = false) String serviceType) {
        return Result.success(tagService.getList(serviceType));
    }

    @ApiOperation("新增标签")
    @PostMapping
    public Result<Void> add(@RequestBody ServiceWorkerTag tag) {
        tagService.add(tag);
        return Result.ok("新增成功");
    }

    @ApiOperation("查询标签详情")
    @GetMapping("/{id}")
    public Result<ServiceWorkerTag> getById(@PathVariable Long id) {
        return Result.success(tagService.getById(id));
    }

    @ApiOperation("修改标签")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ServiceWorkerTag tag) {
        tag.setId(id);
        tagService.updateTag(tag);
        return Result.ok("修改成功");
    }

    @ApiOperation("删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.ok("删除成功");
    }
}
