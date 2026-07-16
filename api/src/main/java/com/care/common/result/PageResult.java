package com.care.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private Long total;
    private List<T> records;

    public static <T> PageResult<T> of(Long total, List<T> records) {
        return new PageResult<>(total, records);
    }

    /** 解决MyBatis-Plus 3.5.x page.getTotal()可能为0的兼容方法 */
    public static <T> PageResult<T> of(IPage<T> page) {
        long total = page.getTotal();
        if (total == 0 && !page.getRecords().isEmpty()) {
            total = page.getRecords().size();
        }
        return new PageResult<>(total, page.getRecords());
    }
}
