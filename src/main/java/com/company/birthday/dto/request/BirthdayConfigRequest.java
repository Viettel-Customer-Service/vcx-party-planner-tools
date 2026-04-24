package com.company.birthday.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BirthdayConfigRequest {

    @NotNull(message = "Trang thai bat/tat khong duoc de trong")
    private Boolean enabled;

    @NotNull(message = "Gio khong duoc de trong")
    @Min(value = 0, message = "Gio phai trong khoang 0-23")
    @Max(value = 23, message = "Gio phai trong khoang 0-23")
    private Integer hour;

    @NotNull(message = "Phut khong duoc de trong")
    @Min(value = 0, message = "Phut phai trong khoang 0-59")
    @Max(value = 59, message = "Phut phai trong khoang 0-59")
    private Integer minute;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }
}

