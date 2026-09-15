package com.cr.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.cr.common.constant.CommonConstants;
import com.cr.common.result.Result;
import com.cr.common.result.ResultCode;
import com.cr.common.util.JwtUtil;
import com.cr.gateway.config.AuthProperties;
import com.cr.gateway.config.GatewayExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JWT authentication filter for gateway routes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {

    private static final int FILTER_ORDER = -100;

    private static final String USER_ID_HEADER = "userId";

    private static final String PUBLIC_IMAGE_PATTERN = "/api/image/*";

    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    private final AuthProperties authProperties;

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    private final GatewayExceptionHandler exceptionHandler;

    /**
     * Authenticates requests before they are routed downstream.
     *
     * @param exchange current exchange
     * @param chain gateway filter chain
     * @return response completion signal
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (HttpMethod.OPTIONS.equals(request.getMethod())
                || isWhitelisted(request.getURI().getPath(), request.getMethod())) {
            return chain.filter(exchange);
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = extractToken(authorization);
        Long userId = resolveUserId(token);
        if (userId == null) {
            return unauthorized(exchange);
        }

        String cacheKey = CommonConstants.REDIS_TOKEN_PREFIX + userId;
        return reactiveStringRedisTemplate.opsForValue()
                .get(cacheKey)
                .map(token::equals)
                .defaultIfEmpty(false)
                .flatMap(valid -> {
                    if (!valid) {
                        log.warn("Invalid or logged-out token for user {}", userId);
                        return unauthorized(exchange);
                    }

                    ServerHttpRequest authenticatedRequest = request.mutate()
                            .headers(headers -> headers.set(
                                    USER_ID_HEADER,
                                    String.valueOf(userId)
                            ))
                            .build();
                    return chain.filter(exchange.mutate()
                            .request(authenticatedRequest)
                            .build());
                });
    }

    /**
     * Returns the filter order.
     *
     * @return filter order
     */
    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }

    /**
     * Checks whether a request path is publicly accessible.
     *
     * @param path request path
     * @param method HTTP method
     * @return true when authentication can be skipped
     */
    private boolean isWhitelisted(String path, HttpMethod method) {
        for (String pattern : authProperties.getWhitelist()) {
            if (StrUtil.isBlank(pattern) || !PATH_MATCHER.match(pattern, path)) {
                continue;
            }
            if (PUBLIC_IMAGE_PATTERN.equals(pattern)) {
                return HttpMethod.GET.equals(method);
            }
            return true;
        }
        return false;
    }

    /**
     * Extracts a raw JWT from the Authorization header.
     *
     * @param authorization authorization header value
     * @return raw JWT or null
     */
    private String extractToken(String authorization) {
        if (StrUtil.isBlank(authorization)) {
            return null;
        }

        String token = authorization.trim();
        if (token.regionMatches(
                true,
                0,
                CommonConstants.TOKEN_PREFIX,
                0,
                CommonConstants.TOKEN_PREFIX.length())) {
            token = token.substring(CommonConstants.TOKEN_PREFIX.length()).trim();
        }
        return StrUtil.isBlank(token) ? null : token;
    }

    /**
     * Validates a JWT and extracts its user identifier.
     *
     * @param token raw JWT
     * @return user identifier or null
     */
    private Long resolveUserId(String token) {
        if (token == null || !JwtUtil.validateToken(token)) {
            return null;
        }
        try {
            return JwtUtil.parseToken(token);
        } catch (RuntimeException exception) {
            log.warn("Failed to parse JWT", exception);
            return null;
        }
    }

    /**
     * Writes a unified unauthorized response.
     *
     * @param exchange current exchange
     * @return response completion signal
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        return exceptionHandler.writeJson(
                exchange.getResponse(),
                HttpStatus.UNAUTHORIZED,
                Result.fail(
                        ResultCode.UNAUTHORIZED.getCode(),
                        ResultCode.UNAUTHORIZED.getMsg()
                )
        );
    }
}
