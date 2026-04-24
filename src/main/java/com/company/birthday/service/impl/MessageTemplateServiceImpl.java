package com.company.birthday.service.impl;

import com.company.birthday.dto.request.MessageTemplateRequest;
import com.company.birthday.dto.response.MessageTemplateResponse;
import com.company.birthday.entity.MessageTemplate;
import com.company.birthday.entity.MessageTemplateType;
import com.company.birthday.repository.MessageTemplateRepository;
import com.company.birthday.service.MessageTemplateService;
import com.company.birthday.service.exception.DuplicateFieldException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageTemplateServiceImpl implements MessageTemplateService {

    private final MessageTemplateRepository messageTemplateRepository;

    public MessageTemplateServiceImpl(MessageTemplateRepository messageTemplateRepository) {
        this.messageTemplateRepository = messageTemplateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageTemplateResponse> getMessages(Pageable pageable) {
        return messageTemplateRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageTemplateResponse getMessage(Integer id) {
        return messageTemplateRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Message template not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageTemplateResponse> getAllActiveMessages() {
        return messageTemplateRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageTemplateResponse> getMessagesByType(MessageTemplateType type) {
        return messageTemplateRepository.findByType(type).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MessageTemplateResponse getDefaultMessageByType(MessageTemplateType type) {
        return messageTemplateRepository.findByTypeAndIsDefaultTrue(type)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Default message template not found for type: " + type));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageTemplateResponse getMessageByName(String name) {
        return messageTemplateRepository.findByName(name)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Message template not found with name: " + name));
    }

    @Override
    public MessageTemplateResponse createMessage(MessageTemplateRequest request) {
        MessageTemplate messageTemplate = new MessageTemplate();
        applyRequest(messageTemplate, request, null);

        MessageTemplate saved = messageTemplateRepository.save(messageTemplate);
        return toResponse(saved);
    }

    @Override
    public MessageTemplateResponse updateMessage(Integer id, MessageTemplateRequest request) {
        MessageTemplate messageTemplate = messageTemplateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message template not found with id: " + id));

        applyRequest(messageTemplate, request, id);

        MessageTemplate updated = messageTemplateRepository.save(messageTemplate);
        return toResponse(updated);
    }

    @Override
    public void deleteMessage(Integer id) {
        MessageTemplate messageTemplate = messageTemplateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message template not found with id: " + id));
        messageTemplateRepository.delete(messageTemplate);
    }

    @Override
    public void toggleActive(Integer id) {
        MessageTemplate messageTemplate = messageTemplateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message template not found with id: " + id));
        messageTemplate.setIsActive(!messageTemplate.getIsActive());
        messageTemplateRepository.save(messageTemplate);
    }

    @Override
    public void setAsDefault(Integer id) {
        MessageTemplate messageTemplate = messageTemplateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message template not found with id: " + id));

        // Remove default from other templates of same type
        messageTemplateRepository.findByTypeAndIsDefaultTrue(messageTemplate.getType())
                .ifPresent(current -> {
                    if (!current.getId().equals(messageTemplate.getId())) {
                        current.setIsDefault(false);
                        messageTemplateRepository.save(current);
                    }
                });

        // Set this as default
        messageTemplate.setIsDefault(true);
        messageTemplateRepository.save(messageTemplate);
    }

    private void applyRequest(MessageTemplate messageTemplate, MessageTemplateRequest request, Integer excludedId) {
        String name = request.getName() == null ? null : request.getName().trim();
        String content = request.getContent() == null ? null : request.getContent().trim();

        validateUniqueName(name, excludedId);

        messageTemplate.setName(name);
        messageTemplate.setType(request.getType());
        messageTemplate.setContent(content);
        messageTemplate.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        messageTemplate.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);

        if (Boolean.TRUE.equals(messageTemplate.getIsDefault())) {
            messageTemplateRepository.findByTypeAndIsDefaultTrue(messageTemplate.getType())
                    .ifPresent(current -> {
                        if (current.getId() == null || !current.getId().equals(messageTemplate.getId())) {
                            current.setIsDefault(false);
                            messageTemplateRepository.save(current);
                        }
                    });
        }
    }

    private void validateUniqueName(String name, Integer excludedId) {
        if (name == null || name.isBlank()) {
            return;
        }

        boolean duplicated = excludedId == null
                ? messageTemplateRepository.existsByNameIgnoreCase(name)
                : messageTemplateRepository.existsByNameIgnoreCaseAndIdNot(name, excludedId);

        if (duplicated) {
            throw new DuplicateFieldException("name", "Ten loi chuc da ton tai.");
        }
    }

    private MessageTemplateResponse toResponse(MessageTemplate messageTemplate) {
        return new MessageTemplateResponse(
                messageTemplate.getId(),
                messageTemplate.getName(),
                messageTemplate.getType(),
                messageTemplate.getContent(),
                messageTemplate.getIsActive(),
                messageTemplate.getIsDefault(),
                messageTemplate.getCreatedAt()
        );
    }
}

