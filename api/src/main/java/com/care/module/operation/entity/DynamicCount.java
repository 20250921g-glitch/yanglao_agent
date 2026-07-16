package com.care.module.operation.entity;

import java.io.Serializable;

/**
 * 邻里圈动态聚合计数（以关系表为准，实时统计后缓存）。
 * - likeCount：dynamic_like 行数
 * - collectCount：dynamic_favorite 行数
 * - shareCount：dynamic.share_count（分享无关系表，直接取自动态行）
 * - commentCount：dynamic_comment 行数
 */
public class DynamicCount implements Serializable {

    private int likeCount;
    private int collectCount;
    private int shareCount;
    private int commentCount;

    public DynamicCount() {
    }

    public DynamicCount(int likeCount, int collectCount, int shareCount, int commentCount) {
        this.likeCount = likeCount;
        this.collectCount = collectCount;
        this.shareCount = shareCount;
        this.commentCount = commentCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(int collectCount) {
        this.collectCount = collectCount;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}
