package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.care.common.cache.CacheHelper;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.Dynamic;
import com.care.module.operation.entity.DynamicComment;
import com.care.module.operation.entity.DynamicCount;
import com.care.module.operation.entity.DynamicFavorite;
import com.care.module.operation.entity.DynamicLike;
import com.care.module.operation.service.DynamicCommentService;
import com.care.module.operation.service.DynamicFavoriteService;
import com.care.module.operation.service.DynamicLikeService;
import com.care.module.operation.service.DynamicService;
import com.care.module.user.entity.AppUser;
import com.care.module.user.service.AppUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Api(tags = "邻里圈(用户端)")
@RestController
@RequestMapping("/api/app/dynamic")
public class AppDynamicController {

    @Autowired
    private DynamicService dynamicService;
    @Autowired
    private DynamicLikeService likeService;
    @Autowired
    private DynamicFavoriteService favoriteService;
    @Autowired
    private DynamicCommentService commentService;
    @Autowired
    private AppUserService appUserService;
    @Autowired
    private CacheHelper cacheHelper;

    /**
     * 同一用户同一动态的细粒度锁，保证点赞/收藏的 check-then-act 串行执行，
     * 避免并发请求同时判定“未点赞”而重复插入或 toggle 状态错乱。
     */
    private volatile Map<String, Object> opLocks = new ConcurrentHashMap<>();

    private Object lockFor(Long userId, Long dynId) {
        return opLocks.computeIfAbsent(userId + ":" + dynId, k -> new Object());
    }

    /**
     * opLocks 只增不删，长期运行会极缓慢堆积锁对象。
     * 定时整体替换为新 Map，已持有旧锁对象的线程不受影响（仍持有原引用直至释放）。
     * 真正的并发正确性由 dynamic_like / dynamic_favorite 表唯一索引兜底。
     */
    private ScheduledExecutorService lockCleaner;

    @PostConstruct
    public void initLockCleaner() {
        lockCleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "opLock-cleaner");
            t.setDaemon(true);
            return t;
        });
        lockCleaner.scheduleAtFixedRate(() -> {
            opLocks = new ConcurrentHashMap<>();
        }, 30, 30, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void destroyLockCleaner() {
        if (lockCleaner != null) {
            lockCleaner.shutdownNow();
        }
    }

    private Long uid(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private Map<String, Object> toMap(Dynamic d) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", d.getId());
        m.put("userId", d.getUserId());
        m.put("userName", d.getUserName());
        m.put("userPhone", d.getUserPhone());
        m.put("title", d.getTitle());
        m.put("content", d.getContent());
        m.put("images", d.getImages());
        m.put("topic", d.getTopic());
        m.put("status", d.getStatus());
        m.put("viewCount", d.getViewCount() == null ? 0 : d.getViewCount());
        m.put("likeCount", d.getLikeCount() == null ? 0 : d.getLikeCount());
        m.put("collectCount", d.getCollectCount() == null ? 0 : d.getCollectCount());
        m.put("shareCount", d.getShareCount() == null ? 0 : d.getShareCount());
        m.put("commentCount", d.getCommentCount() == null ? 0 : d.getCommentCount());
        m.put("createTime", d.getCreateTime());
        return m;
    }

    @ApiOperation("邻里圈动态列表(已通过)")
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        Long userId = uid(request);
        Page<Dynamic> page = new Page<>(pageNum, pageSize);
        page.setSearchCount(false);
        LambdaQueryWrapper<Dynamic> w = new LambdaQueryWrapper<>();
        w.eq(Dynamic::getStatus, 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            w.and(q -> q.like(Dynamic::getContent, keyword).or().like(Dynamic::getTopic, keyword));
        }
        w.orderByDesc(Dynamic::getCreateTime);
        Page<Dynamic> res = dynamicService.page(page, w);
        long total = buildListCount(keyword);
        List<Long> dynamicIds = new ArrayList<>();
        for (Dynamic d : res.getRecords()) {
            dynamicIds.add(d.getId());
        }
        Map<Long, DynamicCount> countsMap = dynamicService.getCountsBatch(dynamicIds);
        Set<Long> likedSet = likeService.likedIds(userId, dynamicIds);
        Set<Long> favSet = favoriteService.favoritedIds(userId, dynamicIds);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Dynamic d : res.getRecords()) {
            Map<String, Object> m = toMap(d);
            DynamicCount c = countsMap.get(d.getId());
            if (c != null) {
                m.put("likeCount", c.getLikeCount());
                m.put("collectCount", c.getCollectCount());
                m.put("shareCount", c.getShareCount());
                m.put("commentCount", c.getCommentCount());
            }
            m.put("liked", likedSet.contains(d.getId()));
            m.put("favorited", favSet.contains(d.getId()));
            records.add(m);
        }
        return Result.success(new PageResult<>(total, records));
    }

    private long buildListCount(String keyword) {
        LambdaQueryWrapper<Dynamic> cw = new LambdaQueryWrapper<>();
        cw.eq(Dynamic::getStatus, 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            cw.and(q -> q.like(Dynamic::getContent, keyword).or().like(Dynamic::getTopic, keyword));
        }
        return dynamicService.count(cw);
    }

    @ApiOperation("动态详情(含评论)")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id, HttpServletRequest request) {
        Dynamic d = dynamicService.getById(id);
        if (d == null || d.getStatus() != 1) {
            return Result.error("动态不存在或已下架");
        }
        d.setViewCount((d.getViewCount() == null ? 0 : d.getViewCount()) + 1);
        dynamicService.updateById(d);
        Long userId = uid(request);
        Map<String, Object> m = toMap(d);
        DynamicCount c = dynamicService.getCounts(id);
        m.put("likeCount", c.getLikeCount());
        m.put("collectCount", c.getCollectCount());
        m.put("shareCount", c.getShareCount());
        m.put("commentCount", c.getCommentCount());
        m.put("liked", likeService.liked(id, userId));
        m.put("favorited", favoriteService.favorited(id, userId));
        List<DynamicComment> comments = commentService.lambdaQuery()
                .eq(DynamicComment::getDynamicId, id)
                .orderByAsc(DynamicComment::getCreateTime)
                .list();
        m.put("comments", comments);
        return Result.success(m);
    }

    @ApiOperation("发布动态(待审核)")
    @PostMapping("/publish")
    public Result<Void> publish(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = uid(request);
        AppUser u = appUserService.getById(userId);
        if (u == null) {
            return Result.error("用户不存在");
        }
        String content = (String) body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("内容不能为空");
        }
        Dynamic d = new Dynamic();
        d.setUserId(userId);
        d.setUserName(u.getUsername());
        d.setUserPhone(u.getPhone());
        d.setContent(content);
        d.setTopic((String) body.get("topic"));
        d.setTitle((String) body.get("title"));
        d.setImages((String) body.get("images"));
        d.setStatus(0);
        d.setViewCount(0);
        d.setLikeCount(0);
        d.setCollectCount(0);
        d.setShareCount(0);
        d.setCommentCount(0);
        dynamicService.save(d);
        return Result.ok("发布成功，等待管理员审核");
    }

    @ApiOperation("点赞/取消点赞")
    @PostMapping("/like")
    public Result<Map<String, Object>> like(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = uid(request);
        Long dynId = Long.valueOf(String.valueOf(body.get("dynamicId")));
        Dynamic d = dynamicService.getById(dynId);
        if (d == null) {
            return Result.error("动态不存在");
        }
        synchronized (lockFor(userId, dynId)) {
            boolean nowLiked;
            if (likeService.liked(dynId, userId)) {
                likeService.lambdaUpdate()
                        .eq(DynamicLike::getDynamicId, dynId)
                        .eq(DynamicLike::getUserId, userId)
                        .remove();
                nowLiked = false;
            } else {
                DynamicLike l = new DynamicLike();
                l.setDynamicId(dynId);
                l.setUserId(userId);
                try {
                    likeService.save(l);
                    nowLiked = true;
                } catch (DuplicateKeyException e) {
                    // 并发场景下已被其他请求插入，幂等视为已点赞
                    nowLiked = true;
                }
            }
            dynamicService.evictCounts(dynId);
            int likeCount = (int) likeService.countByDynamicId(dynId);
            Map<String, Object> m = new HashMap<>();
            m.put("liked", nowLiked);
            m.put("likeCount", likeCount);
            return Result.success(m);
        }
    }

    @ApiOperation("收藏/取消收藏")
    @PostMapping("/favorite")
    public Result<Map<String, Object>> favorite(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = uid(request);
        Long dynId = Long.valueOf(String.valueOf(body.get("dynamicId")));
        Dynamic d = dynamicService.getById(dynId);
        if (d == null) {
            return Result.error("动态不存在");
        }
        synchronized (lockFor(userId, dynId)) {
            boolean nowFav;
            if (favoriteService.favorited(dynId, userId)) {
                favoriteService.lambdaUpdate()
                        .eq(DynamicFavorite::getDynamicId, dynId)
                        .eq(DynamicFavorite::getUserId, userId)
                        .remove();
                nowFav = false;
            } else {
                DynamicFavorite f = new DynamicFavorite();
                f.setDynamicId(dynId);
                f.setUserId(userId);
                try {
                    favoriteService.save(f);
                    nowFav = true;
                } catch (DuplicateKeyException e) {
                    // 并发场景下已被其他请求插入，幂等视为已收藏
                    nowFav = true;
                }
            }
            dynamicService.evictCounts(dynId);
            int collectCount = (int) favoriteService.countByDynamicId(dynId);
            Map<String, Object> m = new HashMap<>();
            m.put("favorited", nowFav);
            m.put("collectCount", collectCount);
            return Result.success(m);
        }
    }

    @ApiOperation("发表评论")
    @PostMapping("/comment")
    public Result<Map<String, Object>> comment(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = uid(request);
        Long dynId = Long.valueOf(String.valueOf(body.get("dynamicId")));
        String content = (String) body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }
        Dynamic d = dynamicService.getById(dynId);
        if (d == null) {
            return Result.error("动态不存在");
        }
        AppUser u = appUserService.getById(userId);
        DynamicComment c = new DynamicComment();
        c.setDynamicId(dynId);
        c.setUserId(userId);
        c.setUserName(u == null ? null : u.getUsername());
        c.setContent(content);
        commentService.save(c);
        dynamicService.evictCounts(dynId);
        int commentCount = (int) commentService.countByDynamicId(dynId);
        Map<String, Object> m = new HashMap<>();
        m.put("commentCount", commentCount);
        return Result.success(m);
    }

    @ApiOperation("我的发布")
    @GetMapping("/my")
    public Result<PageResult<Map<String, Object>>> my(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = uid(request);
        Page<Dynamic> page = new Page<>(pageNum, pageSize);
        page.setSearchCount(false);
        LambdaQueryWrapper<Dynamic> w = new LambdaQueryWrapper<>();
        w.eq(Dynamic::getUserId, userId);
        w.orderByDesc(Dynamic::getCreateTime);
        Page<Dynamic> res = dynamicService.page(page, w);
        long total = dynamicService.count(w);
        List<Long> dynamicIds = new ArrayList<>();
        for (Dynamic d : res.getRecords()) {
            dynamicIds.add(d.getId());
        }
        Map<Long, DynamicCount> countsMap = dynamicService.getCountsBatch(dynamicIds);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Dynamic d : res.getRecords()) {
            Map<String, Object> m = toMap(d);
            DynamicCount c = countsMap.get(d.getId());
            if (c != null) {
                m.put("likeCount", c.getLikeCount());
                m.put("collectCount", c.getCollectCount());
                m.put("shareCount", c.getShareCount());
                m.put("commentCount", c.getCommentCount());
            }
            records.add(m);
        }
        return Result.success(new PageResult<>(total, records));
    }

    @ApiOperation("分享(+1)")
    @PostMapping("/share/{id}")
    public Result<Map<String, Object>> share(@PathVariable Long id) {
        Dynamic d = dynamicService.getById(id);
        if (d == null) {
            return Result.error("动态不存在");
        }
        d.setShareCount((d.getShareCount() == null ? 0 : d.getShareCount()) + 1);
        dynamicService.updateById(d);
        dynamicService.evictCounts(id);
        Map<String, Object> m = new HashMap<>();
        m.put("shareCount", d.getShareCount());
        return Result.success(m);
    }
}
