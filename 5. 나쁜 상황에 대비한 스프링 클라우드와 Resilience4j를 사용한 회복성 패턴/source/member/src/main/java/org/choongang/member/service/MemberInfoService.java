package org.choongang.member.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choongang.member.entity.Member;
import org.choongang.member.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberInfoService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @RateLimiter(name="memberService", fallbackMethod = "fallbackLoadUserByUserName")  // 속도 제한기 패턴을 위한 인스턴스 이름과 폴백 메서드를 설정한다.
    @Retry(name="retryMemberService", fallbackMethod = "fallbackLoadUserByUserName")  // 재시도 패턴을 위해 인스턴스 이름과 폴백 메서드를 설정한다.
    @CircuitBreaker(name="memberService", fallbackMethod = "fallbackLoadUserByUserName")  // Resilience4j 회로 차단기를 사용하여 loadUserByUsername(..) 메서드를 @CircuitBreaker로 래핑한다.
    @Bulkhead(name="bulkheadMemberService", fallbackMethod = "fallbackLoadUserByUserName") // 벌크헤드 패턴을 위한 인스턴스 이름과 폴백 메서드를 설정한다.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        randomlyRunLong();

        Member member = memberRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));

        List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority(member.getAuthority().name()));

        return MemberInfo.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .member(member)
                .authorities(authorities)
                .build();
    }

    public UserDetails fallbackLoadUserByUserName(String username, Throwable t) throws UsernameNotFoundException {
        return MemberInfo.builder().build();
    }

    private void randomlyRunLong() { // 데이터베이스 호출이 오래 실행될 가능성은 3분의 1이다.
        Random rand = new Random();
        int randomNum = rand.nextInt(3) + 1;
        if (randomNum == 3) sleep();
    }

    private void sleep() {
        try {
            Thread.sleep(5000);  // 5000ms(5초)를 일시 정지한 후 TimeoutException 예외를 발생시킨다.
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }
}
