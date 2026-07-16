package com.care.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.common.cache.CacheHelper;
import com.care.module.product.entity.Product;
import com.care.module.product.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    @Autowired
    private CacheHelper cacheHelper;

    public IPage<Product> getPage(Integer pageNum, Integer pageSize, String name, Long categoryId, String serviceType) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Product::getName, name);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(serviceType)) {
            wrapper.eq(Product::getServiceType, serviceType);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return page(page, wrapper);
    }

    public void add(Product product) {
        product.setUpdateBy(currentOperator());
        save(product);
        evictCache(product.getId());
    }

    public void updateProduct(Product product) {
        product.setUpdateBy(currentOperator());
        updateById(product);
        evictCache(product.getId());
    }

    public void delete(Long id) {
        removeById(id);
        evictCache(id);
    }

    /** 商品写后失效：详情缓存 + 列表缓存（列表 key 带参数，按前缀清理） */
    private void evictCache(Long id) {
        if (id == null) {
            return;
        }
        cacheHelper.evict("product:" + id);
        cacheHelper.evictByPrefix("product:list:");
    }

    private String currentOperator() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
                return authentication.getName();
            }
        } catch (Exception ignored) {}
        return "admin";
    }

    public void updateStock(Long id, Integer quantity) {
        Product product = getById(id);
        if (product == null) throw new RuntimeException("商品不存在");
        product.setStock(product.getStock() + quantity);
        if (product.getStock() < 0) throw new RuntimeException("库存不足");
        updateById(product);
        evictCache(id);
    }

    /**
     * 原子扣减库存，返回是否成功（true=扣减成功，false=库存不足/并发失败）。
     * 用带 stock>=qty 条件的单条 UPDATE，避免读-改-写造成的超卖。
     * 扣减后失效商品缓存（详情/列表含 stock 字段），避免读到陈旧库存。
     */
    public boolean deductStock(Long id, int quantity) {
        boolean ok = getBaseMapper().deductStock(id, quantity) == 1;
        if (ok) {
            evictCache((Long) id);
        }
        return ok;
    }

    /** 原子回补库存（取消订单时使用）。回补后同样失效缓存。 */
    public void restoreStock(Long id, int quantity) {
        getBaseMapper().restoreStock(id, quantity);
        evictCache((Long) id);
    }
}
