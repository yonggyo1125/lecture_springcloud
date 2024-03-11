package org.choongang.member.utils;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserContextFilter implements Filter { // @Component와 jakarta.servlet.Filter 인터페이스 구현으로 스프링에서 선택한 필터를 등록한다.

    private static final Logger logger = LoggerFactory.getLogger(UserContextFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        // 헤더에 상관관계 ID를 정의하고 UserContext에 값을 설정한다.
        UserContextHolder.getContext()
                .setCorrelationId(request.getHeader(UserContext.CORRELATION_ID));
        UserContextHolder.getContext()
                .setUserId(request.getHeader(UserContext.USER_ID));
        UserContextHolder.getContext()
                .setAuthToken(request.getHeader(UserContext.AUTH_TOKEN));
        UserContextHolder.getContext()
                .setOrganizationId(request.getHeader(UserContext.ORGANIZATION_ID));

        logger.debug("UserContextFilter Correlation id: {}", UserContextHolder.getContext().getCorrelationId());

        filterChain.doFilter(request, servletResponse);
    }
}
