package org.choongang.member.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardDiscoveryClient {

    private final DiscoveryClient discoveryClient;  // Discovery Client를 클래스에 주입한다.
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public List<Board> getBoards() {

        ResponseEntity<String> exchange = restTemplate.exchange(  // 서비스 호출을 위해 표준 스프링 RestTemplate 클래스를 사용한다.
                "http://board-service/api/v1/board", // 로드 밸런서 지원 RestTemplate를 사용할 때 유레카 서비스 ID로 대상 URL을 생성한다.
                HttpMethod.GET,
                null,
                String.class);

        String json = exchange.getBody();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {}
        return null;
    }
}
