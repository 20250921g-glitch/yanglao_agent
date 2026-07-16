package com.care.module.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.care.module.trade.entity.CommissionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository("operationCommissionMapper")
public interface OperationCommissionMapper extends BaseMapper<CommissionRecord> {
}
