package com.care.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.user.entity.Conversation;
import com.care.module.user.entity.ConversationMessage;
import com.care.module.user.mapper.ConversationMapper;
import com.care.module.user.mapper.ConversationMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationService extends ServiceImpl<ConversationMapper, Conversation> {

    @Autowired
    private ConversationMessageMapper conversationMessageMapper;

    public IPage<Conversation> getPage(Integer pageNum, Integer pageSize, String keyword) {
        Page<Conversation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Conversation::getUserName, keyword)
                    .or().like(Conversation::getPhone, keyword)
                    .or().like(Conversation::getLastMessage, keyword));
        }
        wrapper.orderByDesc(Conversation::getUpdateTime);
        return page(page, wrapper);
    }

    public List<ConversationMessage> getMessages(Long conversationId) {
        LambdaQueryWrapper<ConversationMessage> w = new LambdaQueryWrapper<>();
        w.eq(ConversationMessage::getConversationId, conversationId).orderByAsc(ConversationMessage::getId);
        return conversationMessageMapper.selectList(w);
    }

    /**
     * 客服(后台)向会话发送一条消息：写入 conversation_message(senderType=2)，
     * 并同步更新会话的 last_message / msg_count / update_time。
     */
    public void sendMessage(Long conversationId, String content) {
        ConversationMessage msg = new ConversationMessage();
        msg.setConversationId(conversationId);
        msg.setSenderType(2);
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        conversationMessageMapper.insert(msg);

        Conversation conv = baseMapper.selectById(conversationId);
        if (conv != null) {
            Integer cnt = conv.getMsgCount() == null ? 0 : conv.getMsgCount();
            conv.setLastMessage(content);
            conv.setMsgCount(cnt + 1);
            conv.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(conv);
        }
    }
}
