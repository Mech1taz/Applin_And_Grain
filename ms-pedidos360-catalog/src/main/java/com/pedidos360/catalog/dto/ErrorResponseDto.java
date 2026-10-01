package com.pedidos360.catalog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record ErrorResponseDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ErrorResponseDto of(int status, String error, String message, String path) {
        return new ErrorResponseDto(Instant.now(), status, error, message, path);
    }
}
