package com.care.common.cache;

import java.util.Arrays;
import java.util.List;

/**
 * 缓存 TTL 集中配置 + 监控分组定义。
 * 所有业务的过期时间统一在此维护，调优时只需修改本文件并重新打包，
 * 避免 TTL 硬编码散落在各 Service / Controller 中难以追踪。
 */
public final class CacheTtl {

    // ============ P0 鉴权 ============
    /** 用户角色缓存（user:roles:*） */
    public static final long ROLE_AUTH = 30L;

    // ============ P1 商品 / 服务 ============
    /** 商品详情（product:{id}） */
    public static final long PRODUCT_DETAIL = 5L;
    /** 商品列表（product:list:*） */
    public static final long PRODUCT_LIST = 5L;
    /** 服务详情（service:project:{id}） */
    public static final long SERVICE_DETAIL = 5L;
    /** 服务列表（service:project:list*） */
    public static final long SERVICE_LIST = 5L;

    // ============ P1 轮播 ============
    /** 轮播图分页（banner:page:*） */
    public static final long BANNER_PAGE = 10L;

    // ============ P1 菜单 / 角色 / 分类 ============
    /** 菜单树（sys:menu:tree） */
    public static final long MENU_TREE = 30L;
    /** 角色列表（sys:role:all） */
    public static final long ROLE_ALL = 30L;
    /** 商品分类（product:category:list:*） */
    public static final long CATEGORY_LIST = 30L;

    // ============ P1 邻里圈计数 ============
    /** 邻里圈实时计数（dynamic:count:{id}） */
    public static final long NEIGHBOR_COUNT = 1L;

    // ============ P1 AI 健康建议 ============
    /** AI 健康建议（ai:advice:{scope}:{userId}），避免短期内重复调用 DeepSeek 浪费额度 */
    public static final long AI_ADVICE = 30L;

    // ============ P2 报表 / 看板 ============
    /** 数据看板（data:getDataDashboard） */
    public static final long DATA_DASHBOARD = 5L;
    /** 时序类聚合（data:getXxxAnalysis 等） */
    public static final long DATA_TIMESERIES = 10L;
    /** 分布类聚合 */
    public static final long DATA_DISTRIBUTION = 30L;
    /** 占位类（export / tracking / dict / action-log） */
    public static final long DATA_PLACEHOLDER = 60L;

    /** 监控遍历用的分组：name=展示名，pattern=redis keys 模式，designTtlMinutes=设计 TTL（分钟）。 */
    public static class Group {
        public final String name;
        public final String pattern;
        public final long designTtlMinutes;

        public Group(String name, String pattern, long designTtlMinutes) {
            this.name = name;
            this.pattern = pattern;
            this.designTtlMinutes = designTtlMinutes;
        }
    }

    /** 缓存分组清单，供监控接口遍历。 */
    public static final List<Group> GROUPS = Arrays.asList(
            new Group("鉴权角色", "user:roles:*", ROLE_AUTH),
            new Group("商品缓存", "product:*", PRODUCT_DETAIL),
            new Group("服务缓存", "service:project:*", SERVICE_DETAIL),
            new Group("轮播缓存", "banner:page:*", BANNER_PAGE),
            new Group("菜单树", "sys:menu:tree", MENU_TREE),
            new Group("角色列表", "sys:role:all", ROLE_ALL),
            new Group("商品分类", "product:category:list:*", CATEGORY_LIST),
            new Group("邻里圈计数", "dynamic:count:*", NEIGHBOR_COUNT),
            new Group("AI健康建议", "ai:advice:*", AI_ADVICE),
            new Group("报表看板(多TTL)", "data:*", -1L)
    );

    private CacheTtl() {
    }
}
