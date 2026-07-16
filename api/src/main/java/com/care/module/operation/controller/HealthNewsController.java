package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.HealthNews;
import com.care.module.operation.service.HealthNewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "运营管理-健康资讯")
@RestController
@RequestMapping("/api/operation/health-news")
public class HealthNewsController {

    @Autowired
    private HealthNewsService newsService;

    @ApiOperation("分页查询健康资讯")
    @GetMapping("/page")
    public Result<PageResult<HealthNews>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        IPage<HealthNews> result = newsService.getPage(pageNum, pageSize, title, category, status);
        return Result.success(PageResult.of(result));
    }

    @ApiOperation("根据ID查询资讯")
    @GetMapping("/{id}")
    public Result<HealthNews> getById(@PathVariable Long id) {
        HealthNews news = newsService.getById(id);
        if (news != null && news.getViewCount() != null) {
            news.setViewCount(news.getViewCount() + 1);
            newsService.updateById(news);
        }
        return Result.success(news);
    }

    @ApiOperation("新增健康资讯")
    @PostMapping
    public Result<?> add(@RequestBody HealthNews news) {
        newsService.add(news);
        return Result.success("添加成功");
    }

    @ApiOperation("更新健康资讯")
    @PutMapping
    public Result<?> update(@RequestBody HealthNews news) {
        newsService.updateNews(news);
        return Result.success("更新成功");
    }

    @ApiOperation("删除健康资讯")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        newsService.delete(id);
        return Result.success("删除成功");
    }
}
