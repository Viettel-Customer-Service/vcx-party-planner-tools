package com.company.birthday.dto.request;

import com.company.birthday.entity.MessageTemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MessageTemplateRequest {

    @NotBlank(message = "Message template name is required")
    private String name;

    @NotNull(message = "Message template type is required")
    private MessageTemplateType type;

    @NotBlank(message = "Message template content is required")
    private String content;

    private Boolean isActive;

    private Boolean isDefault;

    public MessageTemplateRequest() {
    }

    public MessageTemplateRequest(String name, MessageTemplateType type, String content) {
        this.name = name;
        this.type = type;
        this.content = content;
        this.isActive = true;
        this.isDefault = false;
    }

    public MessageTemplateRequest(String name, MessageTemplateType type, String content, Boolean isActive, Boolean isDefault) {
        this.name = name;
        this.type = type;
        this.content = content;
        this.isActive = isActive;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MessageTemplateType getType() {
        return type;
    }

    public void setType(MessageTemplateType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}

