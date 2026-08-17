package com.example.apistarterkit.global.response;

import com.example.apistarterkit.global.exception.ErrorCode;
import java.util.List;

public record ApiResponse<T>(boolean success, T data, ErrorPayload error) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> empty() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, ErrorPayload.of(errorCode));
    }

    public static ApiResponse<Void> error(ErrorCode errorCode, List<FieldErrorDetail> fieldErrors) {
        return new ApiResponse<>(false, null, ErrorPayload.of(errorCode, fieldErrors));
    }

    public record ErrorPayload(String code, String message, List<FieldErrorDetail> fieldErrors) {

        public static ErrorPayload of(ErrorCode errorCode) {
            return new ErrorPayload(errorCode.getCode(), errorCode.getMessage(), null);
        }

        public static ErrorPayload of(ErrorCode errorCode, List<FieldErrorDetail> fieldErrors) {
            return new ErrorPayload(errorCode.getCode(), errorCode.getMessage(), fieldErrors);
        }
    }

    public record FieldErrorDetail(String field, String reason) {
    }
}
