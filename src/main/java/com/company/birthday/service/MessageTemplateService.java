package com.company.birthday.service;

import com.company.birthday.dto.request.MessageTemplateRequest;
import com.company.birthday.dto.response.MessageTemplateResponse;
import com.company.birthday.entity.MessageTemplateType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageTemplateService {

    Page<MessageTemplateResponse> getMessages(Pageable pageable);

    MessageTemplateResponse getMessage(Integer id);

    List<MessageTemplateResponse> getAllActiveMessages();

    List<MessageTemplateResponse> getMessagesByType(MessageTemplateType type);

    MessageTemplateResponse getDefaultMessageByType(MessageTemplateType type);

    MessageTemplateResponse getMessageByName(String name);

    MessageTemplateResponse createMessage(MessageTemplateRequest request);

    MessageTemplateResponse updateMessage(Integer id, MessageTemplateRequest request);

    void deleteMessage(Integer id);

    void toggleActive(Integer id);

    void setAsDefault(Integer id);
}

