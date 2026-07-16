package com.care.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.care.module.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 原子扣减库存：仅当剩余库存足够时才扣减，返回受影响行数（1=成功，0=库存不足/并发失败）。
     * 用单条带条件的 UPDATE 保证并发安全，避免读-改-写导致的超卖。
     */
    @Update("UPDATE product SET stock = stock - #{qty} WHERE id = #{id} AND stock >= #{qty}")
    int deductStock(@Param("id") Long id, @Param("qty") int qty);

    /** 原子回补库存（取消订单时使用）。 */
    @Update("UPDATE product SET stock = stock + #{qty} WHERE id = #{id}")
    int restoreStock(@Param("id") Long id, @Param("qty") int qty);
}
