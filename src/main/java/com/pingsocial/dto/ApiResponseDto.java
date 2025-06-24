package com.pingsocial.dto;

import java.time.LocalDateTime;

/**
 * DTO genérico para respostas da API.
 */
public class ApiResponseDto {

    private String message;
    private boolean success;
    private Object data;
    private LocalDateTime timestamp;

    public ApiResponseDto() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponseDto(String message, boolean success) {
        this.message = message;
        this.success = success;
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponseDto(Object data, String message, boolean success) {
        this.data = data;
        this.message = message;
        this.success = success;
        this.timestamp = LocalDateTime.now();
    }

    public static ApiResponseDto success(Object data, String message) {
        return new ApiResponseDto(data, message, true);
    }

    public static ApiResponseDto success(String message) {
        return new ApiResponseDto(message, true);
    }

    public static ApiResponseDto error(String message) {
        return new ApiResponseDto(message, false);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}