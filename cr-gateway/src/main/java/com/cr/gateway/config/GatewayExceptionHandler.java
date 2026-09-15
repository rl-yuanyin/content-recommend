package com.cr.gateway.config;

import com.cr.common.result.Result;
import com.cr.common.result.ResultCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Converts unhandled gateway exceptions to the common JSON response format.
 */
@Slf4j
@Order(-2)
@Component
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    /**
     * Handles unhandled gateway exceptions.
     *
     * @param exchange current exchange
     * @param throwable exception
     * @return response completion signal
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        log.error("Unhandled gateway exception for {}", exchange.getRequest().getURI(), throwable);
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(throwable);
        }
        return writeJson(
                exchange.getResponse(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Result.fail(ResultCode.FAIL.getCode(), "系统异常")
        );
    }

    /**
     * Writes a unified JSON response.
     *
     * @param response HTTP response
     * @param status HTTP status
     * @param result response body
     * @return response completion signal
     */
    public Mono<Void> writeJson(
            ServerHttpResponse response,
            HttpStatus status,
            Result<?> result) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException exception) {
            return Mono.error(exception);
        }
    }
}
