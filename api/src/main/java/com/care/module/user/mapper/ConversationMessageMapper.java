package com.care.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.care.module.user.entity.ConversationMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMessageMapper extends BaseMapper<ConversationMessage> {
}
