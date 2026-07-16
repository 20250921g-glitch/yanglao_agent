package com.care.module.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.care.module.operation.entity.Recipe;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecipeMapper extends BaseMapper<Recipe> {
}