package com.edu.enrollment.exception;

import com.edu.enrollment.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * - BusinessException: 业务逻辑错误，返回 HTTP 200 + ResultVO.error(code, message)
 * - RuntimeException: 系统运行时异常，返回 HTTP 500 + ResultVO.error(message)
 * - MethodArgumentNotValidException: 参数校验失败，返回 HTTP 400 + ResultVO.error(message)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常 - 返回 HTTP 200，让前端通过 code 字段判断结果
     */
    @ExceptionHandler(BusinessException.class)
    public ResultVO<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResultVO.error(e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultVO<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", message);
        return ResultVO.error(400, message);
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultVO<?> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ResultVO.error("服务器内部错误，请稍后重试");
    }
}
