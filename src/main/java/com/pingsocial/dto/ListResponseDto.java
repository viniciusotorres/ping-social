package com.pingsocial.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO genérico para respostas da API que contêm listas de objetos.
 *
 * @param <T> Tipo dos objetos na lista
 */
public class ListResponseDto<T> {

    private List<T> items;
    private int count;
    private boolean success;
    private String message;
    private LocalDateTime timestamp;

    public ListResponseDto() {
        this.timestamp = LocalDateTime.now();
    }

    public ListResponseDto(List<T> items, String message, boolean success) {
        this.items = items;
        this.count = items != null ? items.size() : 0;
        this.message = message;
        this.success = success;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Cria uma resposta de sucesso com uma lista de itens.
     *
     * @param items Lista de itens
     * @param message Mensagem de sucesso
     * @param <T> Tipo dos itens na lista
     * @return DTO ListResponseDto
     */
    public static <T> ListResponseDto<T> success(List<T> items, String message) {
        return new ListResponseDto<>(items, message, true);
    }

    /**
     * Cria uma resposta de erro.
     *
     * @param message Mensagem de erro
     * @param <T> Tipo dos itens na lista
     * @return DTO ListResponseDto
     */
    public static <T> ListResponseDto<T> error(String message) {
        return new ListResponseDto<>(null, message, false);
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
        this.count = items != null ? items.size() : 0;
    }

    public int getCount() {
        return count;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}