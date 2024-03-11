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


        return null;
    }
}
