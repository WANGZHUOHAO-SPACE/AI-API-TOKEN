package com.keybridge.common.exception;

import com.keybridge.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getCode());
        return ResponseEntity.status(status == null ? HttpStatus.BAD_REQUEST : status)
                .body(ApiResponse.error(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBindException(Exception exception) {
        String message;
        if (exception instanceof MethodArgumentNotValidException validException) {
            message = validException.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .orElse("请求参数错误");
        } else {
            BindException bindException = (BindException) exception;
            message = bindException.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .orElse("请求参数错误");
        }
        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException exception) {
        return ApiResponse.error(400, exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException exception) {
        return ApiResponse.error(403, "无权执行此操作");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnknownException(Exception exception) {
        log.error("Unhandled server exception", exception);
        return ApiResponse.error(500, "服务器内部错误");
    }
}
