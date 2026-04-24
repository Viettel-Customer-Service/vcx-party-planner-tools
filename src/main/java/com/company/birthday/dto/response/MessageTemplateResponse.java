package com.company.birthday.dto.response;

import com.company.birthday.entity.MessageTemplateType;
import java.time.LocalDateTime;

public class MessageTemplateResponse {

    private Integer id;
    private String name;
    private MessageTemplateType type;
    private String content;
    private Boolean isActive;
    private Boolean isDefault;
    private LocalDateTime createdAt;

    public MessageTemplateResponse() {
    }

    public MessageTemplateResponse(Integer id, String name, MessageTemplateType type, String content,
                                  Boolean isActive, Boolean isDefault, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.content = content;
        this.isActive = isActive;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "MessageTemplateResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", isActive=" + isActive +
                ", isDefault=" + isDefault +
                ", createdAt=" + createdAt +
                '}';
    }
}

