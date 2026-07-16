package com.care.module.operation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface ContentStatMapper {

    @Select("SELECT (SELECT COUNT(*) FROM dynamic) total,(SELECT COUNT(*) FROM dynamic WHERE status=1) enabled,(SELECT COUNT(*) FROM dynamic WHERE status!=1) disabled,(SELECT update_time FROM dynamic ORDER BY update_time DESC LIMIT 1) latestTime,(SELECT title FROM dynamic ORDER BY update_time DESC LIMIT 1) latestTitle")
    Map<String, Object> statDynamic();

    @Select("SELECT (SELECT COUNT(*) FROM banner) total,(SELECT COUNT(*) FROM banner WHERE status=1) enabled,(SELECT COUNT(*) FROM banner WHERE status!=1) disabled,(SELECT update_time FROM banner ORDER BY update_time DESC LIMIT 1) latestTime,(SELECT title FROM banner ORDER BY update_time DESC LIMIT 1) latestTitle")
    Map<String, Object> statBanner();

    @Select("SELECT (SELECT COUNT(*) FROM food WHERE deleted=0) total,(SELECT COUNT(*) FROM food WHERE deleted=0 AND status=1) enabled,(SELECT COUNT(*) FROM food WHERE deleted=0 AND status!=1) disabled,(SELECT create_time FROM food WHERE deleted=0 ORDER BY create_time DESC LIMIT 1) latestTime,(SELECT name FROM food WHERE deleted=0 ORDER BY create_time DESC LIMIT 1) latestTitle")
    Map<String, Object> statFood();

    @Select("SELECT (SELECT COUNT(*) FROM health_news) total,(SELECT COUNT(*) FROM health_news WHERE status=1) enabled,(SELECT COUNT(*) FROM health_news WHERE status!=1) disabled,(SELECT update_time FROM health_news ORDER BY update_time DESC LIMIT 1) latestTime,(SELECT title FROM health_news ORDER BY update_time DESC LIMIT 1) latestTitle")
    Map<String, Object> statHealthNews();

    @Select("SELECT (SELECT COUNT(*) FROM institution) total,(SELECT COUNT(*) FROM institution WHERE status=1) enabled,(SELECT COUNT(*) FROM institution WHERE status!=1) disabled,(SELECT update_time FROM institution ORDER BY update_time DESC LIMIT 1) latestTime,(SELECT name FROM institution ORDER BY update_time DESC LIMIT 1) latestTitle")
    Map<String, Object> statInstitution();

    @Select("SELECT (SELECT COUNT(*) FROM recipe) total,(SELECT COUNT(*) FROM recipe WHERE status=1) enabled,(SELECT COUNT(*) FROM recipe WHERE status!=1) disabled,(SELECT update_time FROM recipe ORDER BY update_time DESC LIMIT 1) latestTime,(SELECT name FROM recipe ORDER BY update_time DESC LIMIT 1) latestTitle")
    Map<String, Object> statRecipe();

    @Select("SELECT (SELECT COUNT(*) FROM video) total,(SELECT COUNT(*) FROM video WHERE status=1) enabled,(SELECT COUNT(*) FROM video WHERE status!=1) disabled,(SELECT update_time FROM video ORDER BY update_time DESC LIMIT 1) latestTime,(SELECT title FROM video ORDER BY update_time DESC LIMIT 1) latestTitle")
    Map<String, Object> statVideo();

    @Select("SELECT (SELECT COUNT(*) FROM activity) total,(SELECT COUNT(*) FROM activity WHERE status=1) enabled,(SELECT COUNT(*) FROM activity WHERE status!=1) disabled,(SELECT update_time FROM activity ORDER BY update_time DESC LIMIT 1) latestTime,(SELECT name FROM activity ORDER BY update_time DESC LIMIT 1) latestTitle")
    Map<String, Object> statActivity();
}
