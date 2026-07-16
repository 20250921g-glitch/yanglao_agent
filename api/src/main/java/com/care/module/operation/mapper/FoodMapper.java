package com.care.module.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.care.module.operation.entity.Food;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FoodMapper extends BaseMapper<Food> {
}
