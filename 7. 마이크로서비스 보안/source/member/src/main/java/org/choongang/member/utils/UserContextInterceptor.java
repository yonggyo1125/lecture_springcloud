package org.choongang.member.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class UserContextInterceptor implements ClientHttpRequestInterceptor { // ClientHttpRequestInterceptor를 구현한다.
    @Override  // RestTemplate 에서 실제 HTTP 서비스 호출이 발생하기 전에 intercept()를 호출한다.
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        // 발신 서비스를 호출하고자 준비 중인 HTTP 요청 헤더에 UserContext에 저장된 상관관계 ID를 추가한다.
        headers.add(UserContext.CORRELATION_ID, UserContextHolder.getContext().getCorrelationId());
        headers.add(UserContext.AUTH_TOKEN, UserContextHolder.getContext().getAuthToken());

        return execution.execute(request, body);
    }
}
