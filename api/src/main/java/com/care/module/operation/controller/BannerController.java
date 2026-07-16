package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.cache.CacheHelper;
import com.care.common.cache.CacheTtl;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.Banner;
import com.care.module.operation.service.BannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "运营管理-轮播图管理")
@RestController
@RequestMapping("/api/operation/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;
    @Autowired
    private CacheHelper cacheHelper;

    @ApiOperation("分页查询轮播图")
    @GetMapping("/page")
    public Result<PageResult<Banner>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Integer status) {
        String key = "banner:page:" + pageNum + ":" + pageSize
                + ":" + (position == null ? "all" : position)
                + ":" + (status == null ? "all" : status);
        Object cached = cacheHelper.get(key);
        if (cached instanceof com.care.common.result.PageResult) {
            @SuppressWarnings("unchecked")
            com.care.common.result.PageResult<Banner> pr = (com.care.common.result.PageResult<Banner>) cached;
            return Result.success(pr);
        }
        IPage<Banner> result = bannerService.getPage(pageNum, pageSize, position, status);
        com.care.common.result.PageResult<Banner> pr = PageResult.of(result);
        cacheHelper.put(key, pr, CacheTtl.BANNER_PAGE);
        return Result.success(pr);
    }

    @ApiOperation("根据ID查询轮播图")
    @GetMapping("/{id}")
    public Result<Banner> getById(@PathVariable Long id) {
        return Result.success(bannerService.getById(id));
    }

    @ApiOperation("新增轮播图")
    @PostMapping
    public Result<?> add(@RequestBody Banner banner) {
        bannerService.add(banner);
        return Result.success("添加成功");
    }

    @ApiOperation("更新轮播图")
    @PutMapping
    public Result<?> update(@RequestBody Banner banner) {
        bannerService.updateBanner(banner);
        return Result.success("更新成功");
    }

    @ApiOperation("删除轮播图")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return Result.success("删除成功");
    }
}
