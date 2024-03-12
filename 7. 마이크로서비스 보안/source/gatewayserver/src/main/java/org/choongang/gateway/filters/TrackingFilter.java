package org.choongang.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(1)
@Component
public class TrackingFilter implements GlobalFilter { // 글로벌 필터는 GlobalFilter 인터페이스를 구현하고 filter() 메서드를 재정의해야 한다.
    private static final Logger logger = LoggerFactory.getLogger(TrackingFilter.class);

    @Autowired
    private FilterUtils filterUtils;  // 여러 필터에 걸쳐 공통으로 사용되는 함수는 FilterUtils 클래스에 캡슐화된다.

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {  // 요청이 필터를 통과할 때마다 실행되는 코드이다.

        HttpHeaders requestHeaders = exchange.getRequest().getHeaders(); // filter() 메서드의 매개변수로 전달된 ServerWebExchange 객체를 사용하여 요청에서 ServerWebExchange 객체 Http 헤더를 추출한다.
        if (isCorrelationIdPresent(requestHeaders)) {
            logger.debug("tmx-correlation-id found in tracking filter: {}", filterUtils.getCorrelationId(requestHeaders));
        } else {
            String correlationID = generateCorrelationId();
            exchange = filterUtils.setCorrelationId(exchange, correlationID);
            logger.debug("tmx-correlation-id generated in tracking filter: {}", correlationID);
        }


        return chain.filter(exchange);
    }

    private boolean isCorrelationIdPresent(HttpHeaders requestHeaders) { // 요청 헤더에 상관 ID가 있는지 확인하는 헬퍼 메서드
        if (filterUtils.getCorrelationId(requestHeaders) != null) {
            return true;
        } else {
            return false;
        }
    }

    private String generateCorrelationId() { // tmx-correlation-id가 있는지 확인하는 헬퍼 메서드이며, 상관관계 ID를 UUID 값으로 생성한다.
        return java.util.UUID.randomUUID().toString();
    }
}
