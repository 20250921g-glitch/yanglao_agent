package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.Video;
import com.care.module.operation.service.VideoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "视频管理")
@RestController
@RequestMapping("/api/operation/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @ApiOperation("视频列表")
    @GetMapping("/page")
    public Result<PageResult<Video>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        IPage<Video> page = videoService.getPage(pageNum, pageSize, category, status);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("视频列表(不分页)")
    @GetMapping("/list")
    public Result<List<Video>> getAll(@RequestParam(required = false) Integer status) {
        return Result.success(videoService.getAll(status));
    }

    @ApiOperation("视频详情")
    @GetMapping("/{id}")
    public Result<Video> getById(@PathVariable Long id) {
        Video video = videoService.getById(id);
        if (video != null) {
            videoService.fillText(video);
            videoService.incrementView(id);
        }
        return Result.success(video);
    }

    @ApiOperation("新增视频")
    @PostMapping
    public Result<Void> add(@RequestBody Video video) {
        if (video.getStatus() == null) {
            video.setStatus(1);
        }
        if (video.getViewCount() == null) {
            video.setViewCount(0);
        }
        if (video.getLikeCount() == null) {
            video.setLikeCount(0);
        }
        videoService.save(video);
        return Result.ok("新增成功");
    }

    @ApiOperation("更新视频")
    @PutMapping
    public Result<Void> update(@RequestBody Video video) {
        videoService.updateById(video);
        return Result.ok("更新成功");
    }

    @ApiOperation("删除视频")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        videoService.removeById(id);
        return Result.ok("删除成功");
    }

    @ApiOperation("修改状态")
    @PutMapping("/status/{id}")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        Video video = videoService.getById(id);
        if (video == null) {
            return Result.error("记录不存在");
        }
        video.setStatus(status);
        videoService.updateById(video);
        return Result.ok("状态已修改");
    }
}