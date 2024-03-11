# 나쁜 상황에 대비한 스프링 클라우드와 Resilience4j를 사용한 회복성 패턴 

- 모든 시스템, 특히 분산 시스템은 실패를 겪는다. 이러한 실패에 대응하는 애플리케이션을 구축하는 방법은 모든 소프트웨어 개발자의 업무에서 중요한 부분이다. 하지만 <code>회복성(resilience)</code>을 갖춘 시스템을 구축할 때 소프트웨어 엔지니어 대부분은 인프라스트럭처나 중요 서비스의 한 부분이 완전히 실패한 경우만 고려한다. 그들은 핵심 서버의 클러스터링, 서비스의 로드 밸런싱, 인프라스트럭처를 여러 곳에 분리하는 것 등의 기술을 사용하여 애플리케이션 각 계층에 중복성(redundancy)을 만드는 데 집중한다.

- 이러한 접근 방식에서는 시스템 구성 요소가 완전히(대개 엄청나게) 손상될 것을 고려하지만, 회복력 있는 시스템 구축에 대한 작은 한 부분의 문제만 해결할 뿐이다. 서비스가 망가지면 쉽게 감지할 수 있고 애플리케이션은 이를 우회할 수 있다. 하지만 서비스가 느려진다면 성능 저하를 감지하고 우회하는 일은 다음 이유로 매우 어렵다.
    - **서비스 성능 저하는 간헐적으로 시작되어 확산될 수 있다**: 서비스 저하도 작은 곳에서 갑자기 발생할 수 있다. 순식간에 애플리케이션 컨테이너의 스레드 풀이 완전히 소진되고 붕괴되기 전까지, 실패의 첫 징후는 소규모 사용자가 문제에 대해 불평하는 정도로 나타날 수 있다.
    - **원격 서비스 호출은 대개 동기식이며 장기간 수행되는 호출을 중단하지 않는다**: 일반적으로 애플리케이션 개발자는 작업을 수행하려고 서비스를 호출하고 결과를 기다린다. 호출자에게는 서비스 호출이 행(hanging)되는 것을 방지하는 타임아웃 개념이 없다.
    - **대개 원격 자원의 부분적인 저하가 아닌 완전한 실패를 처리하도록 애플리케이션을 설계한다**: 서비스가 완전히 실패하지 않는 한 애플리케이션은 계속해서 불량한 서비스를 호출하고 빠르게 실패하지 못하는 경우가 많다. 이때 호출하는 애플리케이션이나 서비스는 정상적으로 성능이 저하될 수도 있지만, 자원 고갈로 고장 날 가능성이 더 높다. 자원 고갈(resource exhaustion)이란 스레드 풀이나 데이터베이스 커넥션 같은 제한된 자원이 초과 사용되어 호출 클라이언트가 자원이 다시 가용해질 때까지 대기해야 하는 상황이다.
- 성능이 나쁜 원격 서비스가 야기하는 문제를 간과할 수 없는 것은 이를 탐지하기 어려울 뿐만 아니라 전체 애플리케이션 생태계에 파급되는 연쇄 효과를 유발할 수 있기 때문이다. 보호 장치가 없다면 불량한 서비스 하나가 빠르게 여러 애플리케이션을 다운시킬 수 있다. 클라우드 기반이면서 마이크로서비스 기반 애플리케이션이 이러한 유형의 장애에 특히 취약한 이유는 사용자 트랜잭션을 완료하는 데 연관된 다양한 인프라스트럭처와 함께 다수의 세분화된 서비스로 구성되기 때문이다.
- 회복성 패턴은 마이크로서비스 아키텍처에서 가장 중요한 요소 중 하나다.


## 클라이언트 측 회복성이란?

> 클라이언트 측 회복성 소프트웨어 패턴들은 에러나 성능 저하로 원격 자원이 실패할 때 원격 자원의 클라이언트가 고장 나지 않게 보호하는 데 중점을 둔다. 이들 패턴을 사용하면 클라이언트가 빨리 실패하고 데이터베이스 커넥션과 스레드 풀 같은 소중한 자원을 소비하는 것을 방지할 수 있다. 또한 제대로 성능이 낮은 원격 서비스 문제가 소비자에게 ‘상향(upstream)’으로 확산되는 것을 막는다. 

![image1](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/5.%20%EB%82%98%EC%81%9C%20%EC%83%81%ED%99%A9%EC%97%90%20%EB%8C%80%EB%B9%84%ED%95%9C%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%EC%99%80%20Resilience4j%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%9C%20%ED%9A%8C%EB%B3%B5%EC%84%B1%20%ED%8C%A8%ED%84%B4/images/1.png)
> 네 가지 클라이언트 회복성 패턴은 서비스 소비자와 서비스 사이에서 보호대 역할을 한다.

### 클라이언트 측 로드 밸런싱

- 앞서 서비스 디스커버리를 설명하면서 클라이언트 측 로드 밸런싱을 소개했다. 클라이언트 측 로드 밸런싱은 클라이언트가 서비스 디스커버리 에이전트(넷플릭스의 유레카와 같은)에서 서비스의 모든 인스턴스를 검색한 후 해당 서비스 인스턴스의 물리적 위치를 캐싱하는 작업을 포함한다.
- 서비스 소비자가 서비스 인스턴스를 호출해야 할 때 클라이언트 측 로드 밸런싱은 관리 중인 서비스 위치 풀에서 위치를 반환한다. 클라이언트 측 로드 밸런서는 서비스 클라이언트와 서비스 소비자 사이에 위치하기 때문에 서비스 인스턴스가 에러를 발생하거나 정상적으로 동작하지 않는지 탐지할 수 있다. 클라이언트 측 로드 밸런서가 문제를 탐지하면 가용 서비스 풀에서 문제된 서비스 인스턴스를 제거하여 해당 서비스 인스턴스로 더 이상 호출되지 않게 한다.
- 이것이 바로 스프링 클라우드 로드 밸런서(Spring Cloud Load Balancer) 라이브러리가 추가 구성 없이 제공하는 제품 기본 기능이다.

### 회로 차단기

- 회로 차단기 패턴(circuit breaker pattern)은 전기 회로의 차단기를 모델링했다. 전기 시스템에서 회로 차단기는 전선을 통해 과전류가 흐르는지 탐지한다. 회로 차단기가 문제를 탐지하면 나머지 전기 시스템의 연결을 끊고 하부 구성 요소가 타 버리지 않도록 보호한다.
- 소프트웨어 회로 차단기는 원격 서비스가 호출될 때 호출을 모니터링한다. 호출이 너무 오래 걸리면 차단기가 개입해서 호출을 종료한다. 회로 차단기 패턴은 원격 자원에 대한 모든 호출을 모니터링하고, 호출이 충분히 실패하면 회로 차단기 구현체가 열리면서(pop) 빠르게 실패하고 고장 난 원격 자원에 대한 추가 호출을 방지한다.

### 폴백 처리 

- 폴백 패턴(fallback pattern)을 사용하면 원격 서비스 호출이 실패할 때 예외(exception)를 생성하지 않고 서비스 소비자가 대체 코드 경로를 실행하여 다른 수단을 통해 작업을 수행할 수 있다. 여기에는 보통 다른 데이터 소스에서 데이터를 찾거나 향후 처리를 위해 사용자 요청을 큐(queue)에 입력하는 작업이 포함된다. 사용자 호출에 문제가 있다고 예외를 표시하지는 않지만 나중에 요청을 시도해야 한다고 알려 줄 수 있다.
- 예를 들어 사용자 행동 양식을 모니터링하고 구매 희망 항목을 추천하는 기능을 제공하는 전자 상거래 사이트가 있다고 가정해 보자. 일반적으로 마이크로서비스를 호출하여 사용자 행동을 분석하고 특정 사용자에게 맞춤화된 추천 목록을 반환한다. 하지만 기호 설정(preference) 서비스가 실패하면, 폴백은 모든 사용자의 구매 정보를 기반으로 더욱 일반화된 기호 목록을 검색할 수 있다. 그리고 이 데이터는 완전히 다른 서비스와 데이터 소스에서 추출될 수 있다.

### 벌크헤드

- 벌크헤드(bulkhead) 패턴은 선박을 건조하는 개념에서 유래되었다. 배는 격벽(隔壁)이라는 완전히 격리된 수밀 구획으로 나뉘는데, 선체에 구멍이 뚫려도 침수 구역을 구멍이 난 격벽으로만 제한하므여러 원격 자원과 상호 작용해야 하는 서비스에도 동일한 개념을 적용할 수 있다. 벌크헤드 패턴을 사용할 때 원격 자원에 대한 호출을 자원별 스레드 풀로 분리하면, 느린 원격 자원 호출 하나로 발생한 문제가 전체 애플리케이션을 다운시킬 위험을 줄일 수 있다. 스레드 풀은 서비스의 벌크헤드(격벽) 역할을 한다. 각 원격 자원을 분리하여 스레드 풀에 각각 할당한다. 한 서비스가 느리게 응답한다면 해당 서비스의 호출로 배 전체에 물이 차서 침몰되는 것을 방지한다.
-  그룹에 대한 스레드 풀만 포화되어 요청 처리를 중단하게 될 수 있다. 스레드 풀별로 서비스를 할당하면 다른 서비스는 포화되지 않기 때문에 이러한 병목 현상을 우회하는 데 유용하다.

## 클라이언트 회복성이 중요한 이유?

![image2](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/5.%20%EB%82%98%EC%81%9C%20%EC%83%81%ED%99%A9%EC%97%90%20%EB%8C%80%EB%B9%84%ED%95%9C%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%EC%99%80%20Resilience4j%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%9C%20%ED%9A%8C%EB%B3%B5%EC%84%B1%20%ED%8C%A8%ED%84%B4/images/2.png)
> 회로 차단기가 작동하고 오동작하는 서비스 호출을 빠르고 원만하게 실패하게 한다.

- 첫 번째인 ‘정상 시나리오(the happy path)’에서 회로 차단기는 타이머를 설정하고, 타이머가 만료되기 전에 원격 호출이 완료되면 라이선싱 서비스는 정상적으로 모든 작업을 계속 수행할 수 있다.
- 두 번째 부분적인 서비스 저하 시나리오의 경우 라이선싱 서비스는 회로 차단기를 통해 조직 서비스를 호출한다. 하지만 조직 서비스가 느리게 실행되어 회로 차단기가 관리하는 스레드 타이머가 만료되기 전에 호출이 완료되지 않으면, 회로 차단기는 원격 서비스에 대한 연결을 종료하고 라이선싱 서비스는 호출 오류를 반환한다. 라이선싱 서비스는 조직 서비스 호출이 완료되길 기다리기 위해 자원(자체 스레드 및 커넥션 풀)을 점유하지 않는다.<br><br>또한 조직 서비스에 대한 호출 시간이 만료되면 회로 차단기는 발생한 실패 횟수를 추적하기 시작하는데, 특정 시간 동안 서비스에서 오류가 필요 이상으로 발생하면 회로 차단기는 회로를 ‘차단(trip)’하고 조직 서비스에 대한 모든 호출은 조직 서비스 호출 없이 실패한다.
- 세 번째 시나리오에서 라이선싱 서비스는 회로 차단기의 타임아웃을 기다릴 필요 없이 문제가 있다는 것을 즉시 알 수 있다. 그런 다음 완전히 실패하거나 대체 코드(폴백)를 사용하여 조치하는 것 중에서 선택할 수 있다. 회로 차단기가 차단되면 라이선싱 서비스가 조직 서비스를 호출하지 않았기 때문에 조직 서비스는 회복할 수 있는 기회가 생긴다. 이것으로 조직 서비스는 약간의 여유가 생기므로 서비스 저하가 발생할 때 연쇄 장애를 방지하는 데 도움이 된다.
- 회로 차단기는 때때로 저하된 서비스에 호출을 허용하는데, 이 호출이 연속적으로 필요한 만큼 성공하면 회로 차단기를 스스로 재설정한다. 원격 호출에 대해 회로 차단기 패턴이 제공하는 주요 이점은 다음과 같다.
  - **빠른 실패(fail fast)**: 원격 서비스가 성능 저하를 겪으면 애플리케이션은 빠르게 실패하고 전체 애플리케이션을 완전히 다운시킬 수 있는 자원 고갈 이슈를 방지한다. 대부분의 장애 상황에서 완전히 다운되는 것보다 일부가 다운되는 것이 더 낫다.
  - **원만한 실패(fail gracefully)**: 타임아웃과 빠른 실패를 사용하는 회로 차단기 패턴은 원만하게 실패하거나 사용자 의도를 충족하는 대체 메커니즘을 제공할 수 있게 해 준다. 예를 들어 사용자는 한 가지 데이터 소스에서 데이터를 검색하려고 하고, 해당 데이터 소스가 서비스 저하를 겪고 있다면 다른 위치에서 해당 데이터를 검색할 수 있다.
  - **원활한 회복(recover seamlessly)**: 회로 차단기 패턴이 중개자 역할을 하므로 회로 차단기는 요청 중인 자원이 다시 온라인 상태가 되었는지 확인하고, 사람의 개입 없이 자원에 대한 재접근을 허용하도록 주기적으로 확인한다. 
- 수백 개의 서비스를 가진 대규모 클라우드 기반 애플리케이션에서 이렇게 원활하게 회복하는 것은 서비스를 복구하는 데 필요한 시간을 크게 줄일 수 있어 매우 중요하다. 또한 회로 차단기가 서비스 복원에 직접 개입하기 때문에(실패한 서비스를 재시작하면서) ‘피로한’ 운영자나 애플리케이션 엔지니어가 더 많은 문제를 일으킬 위험을 크게 감소시킨다.
- Resilience4j 이전에는 마이크로서비스에서 회복성 패턴을 구현할 수 있는 가장 일반적인 자바 라이브러리 중 하나로 히스트릭스(Hystrix)를 사용했다. 이제 히스트릭스는 유지 보수 단계로 전환되어 더 이상 새로운 기능이 추가되지 않는다. 히스트릭스의 대체 라이브러리로 권장되는 것 중 하나가 <code>Resilience4j</code>이다.

## Resilience4j 구현

- <code>Resilience4j</code>는 히스트릭스에서 영감을 받은 내결함성 라이브러리다. 네트워크 문제나 여러 서비스의 고장으로 발생하는 결함 내성을 높이기 위해 다음 패턴을 제공한다
  - **회로 차단기(circuit breaker)**: 요청받은 서비스가 실패할 때 요청을 중단한다.
  - **재시도(retry)**: 서비스가 일시적으로 실패할 때 재시도한다.
  - **벌크헤드(bulkhead)**: 과부하를 피하고자 동시 호출하는 서비스 요청 수를 제한한다.
  - **속도 제한(rate limit)**: 서비스가 한 번에 수신하는 호출 수를 제한한다.
  - **폴백(fallback)**: 실패하는 요청에 대해 대체 경로를 설정한다.

- Resilience4j를 사용하면 메서드에 여러 애너테이션을 정의하여 동일한 메서드 호출에 여러 패턴을 적용할 수 있다. 예를 들어 벌크헤드 및 회로 차단기 패턴으로 나가는 호출 수를 제한하려면 메서드에 @CircuitBreaker와 @Bulkhead 애너테이션을 정의할 수 있다. Resilience4j의 재시도 순서에서 주목할 사항은 다음과 같다.

```
Retry(CircuitBreaker(RateLimiter(TimeLimiter(Bulkhead(Function)))))
```

- 호출의 마지막에 Retry(재시도)가 적용된다(필요할 경우). 패턴을 결합하려고 할 때는 이 순서를 기억해야 하며, 각 패턴을 개별적으로 사용할 수도 있다.
- 회로 차단기, 재시도, 속도 제한, 폴백, 벌크헤드 패턴을 구현하려면 스레드와 스레드 관리에 대한 해박한 지식이 필요하다. 그리고 이러한 패턴을 높은 품질로 구현하려면 엄청난 양의 작업이 필요하다. 다행히 스프링 부트와 Resilience4j 라이브러리를 사용하면 여러 마이크로서비스 아키텍처에서 항상 사용되는 검증된 도구를 제공할 수 있다.
- 앞으로 살펴보게 될 것 
  - 스프링 부트/Resilience4j 래퍼(wrapper)를 포함하기 위한 라이선싱 서비스의 그래들 빌드 파일(build.gradle) 구성 방법
  - 회로 차단기, 재시도, 속도 제한 및 벌크헤드 패턴을 이용하여 원격 호출을 래핑하기 위한 스프링 부트/Resilience4j 애너테이션 사용 방법
  - 각 호출에 대한 타임아웃을 설정하여 원격 자원의 회로 차단기를 맞춤 설정하는 방법
  - 회로 차단기가 호출을 중단해야 하거나 호출이 실패할 경우 폴백 전략 구현 방법
  - 서비스 호출을 격리하고 서로 다른 원격 자원 간 벌크헤드를 구축하고자 서비스 내 분리된 스레드 풀을 사용하는 방법

## 스프링 클라우드와 Resilience4j를 사용하여 회원 서비스 설정

- Resilience4j를 사용해 보려면 먼저 프로젝트의 build.gradle 파일에서 필요한 의존성을 포함해야 한다.

> member-service의 build.gradle에 다음 의존성을 추가 
> 
> implementation 'io.github.resilience4j:resilience4j-spring-boot3'
> 
> implementation 'org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j'
> 
> implementation 'org.springframework.boot:spring-boot-starter-aop'

```groovy
...

dependencies {
  
  ...
  
  implementation 'io.github.resilience4j:resilience4j-spring-boot3'
  implementation 'org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j'
  implementation 'org.springframework.boot:spring-boot-starter-aop'
  
  ...
  
}
...

```

- **resilience4j-spring-boot3** :  Resilience4j 스프링 부트 라이브러리를 내려받도록 지시하는데, 이 라이브러리는 커스텀 패턴의 애너테이션을 사용할 수 있게 해 준다.
- **resilience4j-circuitbreaker와 resilience4j-timelimiter** : 회로 차단기 및 속도 제한기(rate limiter)를 구현한 로직이 포함된다.
- **spring-boot-starter-aop** : 스프링 AOP 관점을 실행하는 데 필요

> 관점 지향 프로그래밍(aspect-oriented programming)은 시스템의 다른 부분에 영향을 주는 프로그램 부분(횡단 관심사(cross-cutting concerns))을 분리하여 모듈성을 높이려는 프로그래밍 패러다임이다. AOP는 코드를 수정하지 않고 새로운 동작을 기존 코드에 추가한다.

## 회로 차단기 구현

- 회로 차단기를 이해하려면 전기 시스템과 비교해야 한다. 전기 시스템에서 전선을 통해 너무 많은 전류가 흐른다면 어떻게 될까? 기억하겠지만, 회로 차단기가 문제를 감지하면 시스템 나머지 부분의 연결을 끊고 다른 구성 요소의 추가 손상을 방지한다. 소프트웨어 코드 아키텍처에서도 마찬가지다.
- 코드에서 회로 차단기가 추구하는 것은 원격 호출을 모니터링하고 서비스를 장기간 기다리지 않게 하는 것이다. 이때 회로 차단기는 연결을 종료하고 더 많이 실패하며 오작동이 많은 호출이 있는지 모니터링하는 역할을 한다. 그런 다음 이 패턴은 빠른 실패(fast fail)를 구현하고 실패한 원격 서비스에 추가로 요청하는 것을 방지한다. Resilience4j의 회로 차단기에는 세 개의 일반 상태를 가진 유한 상태 기계가 구현되어 있다.

![image3](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/5.%20%EB%82%98%EC%81%9C%20%EC%83%81%ED%99%A9%EC%97%90%20%EB%8C%80%EB%B9%84%ED%95%9C%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%EC%99%80%20Resilience4j%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%9C%20%ED%9A%8C%EB%B3%B5%EC%84%B1%20%ED%8C%A8%ED%84%B4/images/3.png)
> Resilience4j 회로 차단기 상태: 닫힌, 열린, 반열린 상태 

- 처음에 Resilience4j 회로 차단기는 닫힌 상태에서 시작한 후 클라이언트 요청을 기다린다. 닫힌 상태는 링 비트 버퍼(ring bit buffer)를 사용하여 요청의 성과 및 실패 상태를 저장한다. 요청이 성공하면 회로 차단기는 링 비트 버퍼에 0비트를 저장하지만, 호출된 서비스에서 응답받지 못하면 1비트를 저장한다.
- 실패율을 계산하려면 링을 모두 채워야 한다. 예를 들어 이전 시나리오에서 실패율을 계산하려면 적어도 12 호출은 평가해야 한다. 11개의 요청만 평가했을 때 11개의 호출이 모두 실패하더라도 회로 차단기는 열린 상태로 변경되지 않는다. 회로 차단기는 고장률이 임계 값(구성 설정 가능한)을 초과할 때만 열린다.

![image4](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/5.%20%EB%82%98%EC%81%9C%20%EC%83%81%ED%99%A9%EC%97%90%20%EB%8C%80%EB%B9%84%ED%95%9C%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%EC%99%80%20Resilience4j%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%9C%20%ED%9A%8C%EB%B3%B5%EC%84%B1%20%ED%8C%A8%ED%84%B4/images/4.png)

> 결과가 12개인 Resilience4j 회로 차단기의 링 비트 버퍼. 이 링은 성공한 모든 요청에서는 0이 되고, 호출된 서비스에서 응답받지 못하면 1이 된다.

- 회로 차단기가 열린 상태라면 설정된 시간 동안 호출은 모두 거부되고 회로 차단기는 CallNotPermittedException 예외를 발생시킨다. 설정된 시간이 만료되면 회로 차단기는 반열린 상태로 변경되고 서비스가 여전히 사용 불가한지 확인하고자 일부 요청을 허용한다.
- 반열린 상태에서 회로 차단기는 설정 가능한 다른 링 비트 버퍼를 사용하여 실패율을 평가한다. 이 실패율이 설정된 임계치보다 높으면 회로 차단기는 다신 열린 상태로 변경된다. 임계치보다 작거나 같다면 닫힌 상태로 돌아간다. 이것은 다소 혼란스러울 수 있지만 **열린 상태에서는 회로 차단기가 모든 요청을 거부하고 닫힌 상태에서는 수락한다는 점**을 기억하라.
- 또한 Resilience4j 회로 차단기 패턴에서 다음과 같은 추가 상태를 정의할 수 있다. 다음 상태를 벗어나는 유일한 방법은 회로 차단기를 재설정하거나 상태 전환을 트리거하는 것이다.
  - **비활성 상태(DISABLED)**: 항상 액세스 허용
  - **강제 열린 상태(FORCED_OPEN)**: 항상 액세스 거부

- Resilience4j 구현 방법을 두 가지 큰 범주에서 살펴보면
  - 첫째, Resilience4j 회로 차단기로 라이선스 및 조직 서비스의 데이터베이스에 대한 모든 호출을 래핑(wrapping)한다.
  - 둘째, Resilience4j를 사용하여 두 서비스 간 호출을 래핑한다.
- 두 호출 범주가 다르지만 Resilience4j를 사용하면 이러한 호출이 완전히 동일하다는 것을 알 수 있다.

![image5](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/5.%20%EB%82%98%EC%81%9C%20%EC%83%81%ED%99%A9%EC%97%90%20%EB%8C%80%EB%B9%84%ED%95%9C%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%EC%99%80%20Resilience4j%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%9C%20%ED%9A%8C%EB%B3%B5%EC%84%B1%20%ED%8C%A8%ED%84%B4/images/5.png)
> Resilience4j는 모든 원격 자원 호출 사이에 위치하여 클라이언트를 보호하고, 원격 자원이 데이터베이스나 REST 기반 서비스를 호출하는지는 중요하지 않다.

- 동기식 회로 차단기로 회원 서비스 데이터베이스에서 회원 서비스의 데이터 검색 호출을 래핑하는 방법을 보여 주고 논의를 시작해 보자. 동기식 호출을 이용하여 라이선싱 서비스는 데이터를 검색하지만, 처리를 계속 진행하기 전에 SQL 문이 완료되거나 회로 차단기가 타임아웃이 될 때까지 대기한다.

- 회로 차단기를 사용한 원격 자원 호출 래핑하기

> member-service: src/main/java/.../constant/Authority.java

```java
package org.choongang.member.constant;

public enum Authority {
    USER, // 일반 사용자
    ADMIN // 최고 관리자
}
```

> member-service: src/main/java/.../entity/Member.java

```java
...
public class Member extends Base {
  ...

  @Enumerated(EnumType.STRING)
  @Column(length=10, nullable = false)
  private Authority authority = Authority.USER;
}
```

> member-service: src/main/java/.../repository/MemberRepository.java

```java
...

public interface MemberRepository extends JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member> {
    
  ...

  Optional<Member> findByEmail(String email);
}

```

> member-service: src/main/java/.../service/MemberInfo.java

```java
package org.choongang.member.service;

import lombok.Builder;
import org.choongang.member.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Builder
public class MemberInfo implements UserDetails {

    private String email;
    private String password;
    private Member member;
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

> member-service: src/main/java/.../service/MemberInfoService.java

```java
package org.choongang.member.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.choongang.member.entity.Member;
import org.choongang.member.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberInfoService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @CircuitBreaker(name="memberService")  // Resilience4j 회로 차단기를 사용하여 loadUserByUsername(..) 메서드를 @CircuitBreaker로 래핑한다.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));

        List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority(member.getAuthority().name()));

        return MemberInfo.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .member(member)
                .authorities(authorities)
                .build();
    }
}
```

- 이 코드는 엄청 많아 보이지도 않고 실제로도 많지 않지만 이 한 개의 애너테이션에는 다양한 기능이 들어 있다. @CircuitBreaker 애너테이션을 사용하면 loadUserByUsername() 메서드가 호출될 때마다 해당 호출은 Resilience4j 회로 차단기로 래핑된다. 회로 차단기는 실패한 모든 loadUserByUsername()에 대한 메서드 호출 시도를 가로챈다.

- 이 코드는 데이터베이스가 올바르게 작동한다면 아무 일도 하지 않는다. 다음 코드에서 느리거나 타임아웃된 데이터베이스 쿼리가 수행되는 loadUserByUsername() 메서드를 시뮬레이션해 보자.

> member-service: src/main/java/.../service/MemberInfoService.java

```java
....

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberInfoService implements UserDetailsService {
    
  ...

  @Override
  @CircuitBreaker(name="memberService")  // Resilience4j 회로 차단기를 사용하여 loadUserByUsername(..) 메서드를 @CircuitBreaker로 래핑한다.
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    randomlyRunLong();
    ...
    
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
```

> src/main/java/.../controller/MemberController.java

```java
...
public class MemberController {
  ...
  private final MemberInfoService memberInfoService;
  
  ...
  
  
  @GetMapping("/test")
  public String memberTest() {
    try {
      memberInfoService.loadUserByUsername("user01@test.org");
    } catch (Exception e) {
      if (e instanceof UsernameNotFoundException) {
        e.printStackTrace();
      } else {
        throw e;
      }
    }
    return "OK";
  }
  
  ...
}
```

- ARC에서 http://localhost:8080/api/v1/member/test 엔드포인트를 여러 번 호출하면 회원 서비스에서 다음 에러 메세지를 반환한다.

```json
{
    "timestamp": 1595178498383,
    "status": 500,
    "error": "Internal Server Error",
    "message": "No message available",
    "path": "/api/v1/member/test" 
}
```

- 실패 중인 서비스를 계속 호출하면 결국 링 비트 버퍼가 다 차서 다음과 같은 에러가 표시된다.

### 게시판 서비스에 회로 차단기 추가 

- 메서드 레벨의 애너테이션으로 회로 차단기 기능을 호출에 삽입할 경우 장점은 데이터베이스를 액세스하든지 마이크로서비스를 호출하든지 간에 동일한 애너테이션을 사용할 수 있다는 것이다. 회원 서비스에서 회원과 연관된 게시판 목록을 검색해야 할 때를 예로 들어 보자. 회로 차단기로 게시판 서비스에 대한 호출을 래핑하고 싶다면, 간단하게 다음과 같이 <code>@CircuitBreaker</code>를 추가한다.


> member-service: src/main/java/.../service/client/BoardDiscoveryClient.java

```java
...
public class BoardDiscoveryClient {
  ...
  @CircuitBreaker(name="boardService")
  public List<Board> getBoards() {
    ...
  }
  ...
}
```

> @CircuitBreaker로 구현하는 것은 쉽지만 이 애너테이션의 기본값을 사용할 때는 주의해야 한다. 항상 요구 사항에 적합한 구성을 분석하고 설정할 것을 적극 권장한다.


### 회로 차단기 사용자 정의 

- Resilience4j 회로 차단기를 사용자 정의하는 방법은 member-service 또는 board-service의  bootstrap.yml 또는 서비스 구성 파일에 몇 가지 파라미터를 추가하면 쉽게 해결할 수 있다.
- 회로 차단기 사용자 정의하기 

> member-service : src/main/resources/bootstrap.yml

```yaml
...

spring:
  application:
    name: member-service  # 이름을 지정하면 스프링 클라우드 컨피그 클라이언트는 어떤 서비스가 검색되는지 알 수 있다.
  profiles:
    active: dev # 서비스가 실행될 프로파일을 지정한다. 프로파일은 환경에 매핑된다.
  cloud:
    config:
      uri: http://localhost:8071 # 스프링 클라우드 컨피그 서버의 위치를 지정한다.


resilience4j.circuitbreaker:
  instances:
    memberService: # 회원 서비스의 인스턴스 구성(회로 차단기 에너테이션에 전달되는 이름과 동일
      registerHealthIndicator: true  # 상태 정보 엔드포인트에 대한 구성 정보 노출 여부를 설정한다.
      ringBufferSizeInClosedState: 5  # 링 버퍼의 닫힌 상태 크기를 설정한다.
      ringBufferSizeInHalfOpenState: 3  # 링 버퍼의 반 열린 상태의 크기를 설정한다.
      waitDurationInOpenState: 10s  # 열린 상태의 대기 시간을 설정한다.
      failureRateThreshold: 50  # 실패율 임계치를 백분율(%)로 설정한다.
      recordException:  # 실패로 기록될 예외를 설정한다.
        - org.springframework.web.client.HttpServerErrorException
        - java.io.IOException
        - java.util.concurrent.TimeoutException
        - org.springframework.web.client.ResourceAccessException

    boardService: # 게시판 서비스의 인스턴스 구성(회로 차단기 에너테이션에 전달되는 이름과 동일)
      registerHealthIndicator: true
      ringBufferSizeInClosedState: 6
      ringBufferSizeInHalfOpenState: 4
      waitDurationInOpenState: 20s
      failureRateThreshold: 60


```

- **ringBufferSizeInClosedState**: 회로 차단기가 닫힌 상태일 때 링 비트 버퍼의 크기를 설정한다. 기본값은 100이다.
- **ringBufferSizeInHalfOpenState**: 회로 차단기가 반열린 상태일 때 링 비트 버퍼의 크기를 설정한다. 기본값은 10이다.
- **waitDurationInOpenState**: 열린 상태에서 반열린 상태로 변경하기 전 회로 차단기가 대기해야 할 시간을 설정한다. 기본값은 60,000ms다.
- **failureRateThreshold**: 실패율 임계치의 백분율을 구성한다. 실패율이 임계치보다 크거나 같을 때 회로 차단기는 열린 상태로 변경되고, 단락 점검 호출(short-circuiting call)을 시작한다. 기본값은 50이다.
- **recordExceptions**: 실패로 간주될 예외를 나열한다. 기본적으로 모든 예외는 실패로 기록된다.
- 더 많은 구성 정보를 알고 싶다면 https://resilience4j.readme.io/docs/circuitbreaker 를 참고

## 폴백 처리

- 회로 차단기 패턴의 장점 중 하나는 이 패턴이 ‘중개자’로, 원격 자원과 그 소비자 사이에 위치하기 때문에 서비스 실패를 가로채서 다른 대안을 취할 수 있다는 것이다.
- Resilience4j에서 이 대안을 <code>폴백 전략(fallback strategy)</code>이라고 하며 쉽게 구현할 수 있다.

> member-service: src/main/java/.../service/client/BoardDiscoveryClient.java

```java 
package org.choongang.member.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardDiscoveryClient {

    private final DiscoveryClient discoveryClient;  // Discovery Client를 클래스에 주입한다.
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @CircuitBreaker(name="boardService", fallbackMethod = "getBoardFailure")
    public List<Board> getBoards() {
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public List<Board> getBoardFailure(Throwable t) {
        log.error("fallback 메서드 실행!!: {}", t.getMessage());
        return null;
    }
}
```

- Resilience4j로 폴백 전략을 구현하려면 두 가지 작업이 필요하다. 
  - 첫 번째 필요한 작업은 <code>@CircuitBreaker</code> 또는 다른 애너테이션에 <code>fallbackMethod</code> 속성을 추가하는 것이다. 이 속성은 실패해서 Resilience4j가 호출을 중단할 때 대신 호출할 메서드 이름을 포함해야 한다.
  - 두 번째 필요한 작업은 폴백 메서드를 정의하는 것이다. 이 메서드는 @CircuitBreaker가 보호하는 원래 메서드와 동일한 클래스에 위치해야 한다. 폴백 메서드를 생성하려면 원래 메서드에 동일한 매개변수를 받도록 동일한 서식을 가져야 한다. 동일한 서식을 사용해야 원래 메서드의 모든 매개변수를 폴백 메서드에 전달할 수 있다. 

### 폴백 전략
- 다음은 폴백 전략의 구현 여부를 결정할 때 몇 가지 염두에 두어야 할 사항이다.
  - 폴백은 자원이 타임아웃되거나 실패했을 때 동작 가이드를 제공한다. 타임아웃 예외를 포착하는 데 폴백을 사용하고 에러를 기록하는 것에 아무것도 하지 않는다면 서비스 호출 주위에 표준 try ... catch 블록을 사용해야 한다. 즉, 예외를 잡고 로깅 로직을 try ... catch 블록에 추가한다.
  - 폴백 함수에서 수행할 동작에 유의하기 바란다. 폴백 서비스에서 다른 분산 서비스를 호출하는 경우 @CircuitBreaker로 그 폴백을 또 래핑해야 할 수 있다. 1차 폴백이 발생한 것과 동일한 실패가 2차 폴백에서도 발생할 수 있음을 고려하라. 방어적으로 코딩해야 한다.

## 벌크헤드 패턴 구현

- 마이크로서비스 기반 애플리케이션에서 특정 작업을 완료하기 위해 여러 마이크로서비스를 호출해야 할 경우가 많다. 벌크헤드 패턴을 사용하지 않는다면 이러한 호출의 기본 동작은 전체 자바 컨테이너에 대한 요청을 처리하려고 예약된 동일한 스레드를 사용해서 실행한다. 대규모 요청이라면 하나의 서비스에 대한 성능 문제로 자바 컨테이너의 모든 스레드가 최대치에 도달하고, 작업이 처리되길 기다리는 동안 새로운 작업 요청들은 후순위로 대기한다. 결국 자바 컨테이너는 멈추게 된다.

- 벌크헤드 패턴은 원격 자원 호출을 자체 스레드 풀에 격리해서 한 서비스의 오작동을 억제하고 컨테이너를 멈추지 않게 한다. Resilience4j는 벌크헤드 패턴을 위해 두 가지 다른 구현을 제공한다. 이 구현 타입에 따라 동시 실행 수를 제한할 수 있다.
  - **세마포어 벌크헤드(semaphore bulkhead)**: 세마포어 격리 방식으로 서비스에 대한 **동시 요청 수를 제한**한다. 한계에 도달하면 요청을 거부한다.  
  - **스레드 풀 벌크헤더(thread pool bulkhead)**: 제한된 큐와 고정 스레드 풀을 사용한다. 이 방식은 **풀과 큐가 다 찬 경우만 요청을 거부**한다.  

- Resilience4j는 기본적으로 세마포어 벌크헤드 타입을 사용한다.
- 이 모델은 애플리케이션에서 액세스하는 원격 자원의 수가 적고 각 서비스에 대한 호출량이 상대적으로 고르게 분산되어 있는 경우 잘 작동한다. 문제는 다른 서비스보다 호출량이 훨씬 많거나 완료하는 데 오래 걸리는 서비스가 있다면, 한 서비스가 기본 스레드 풀의 모든 스레드를 점유하기 때문에 모든 스레드를 소진하게 된다.

![image6](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/5.%20%EB%82%98%EC%81%9C%20%EC%83%81%ED%99%A9%EC%97%90%20%EB%8C%80%EB%B9%84%ED%95%9C%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%EC%99%80%20Resilience4j%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%9C%20%ED%9A%8C%EB%B3%B5%EC%84%B1%20%ED%8C%A8%ED%84%B4/images/6.png)
> 기본 Resilience4j 벌크헤드 타입은 세마포어 방식이다.

- 다행히 Resilience4j는 서로 다른 원격 자원 호출 간 벌크헤드를 만들고자 사용하기 쉬운 메커니즘을 제공한다. 다음 그림은 관리 자원이 각 벌크헤드로 분리되었을 때를 잘 보여 준다.

![image7](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/5.%20%EB%82%98%EC%81%9C%20%EC%83%81%ED%99%A9%EC%97%90%20%EB%8C%80%EB%B9%84%ED%95%9C%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%EC%99%80%20Resilience4j%EB%A5%BC%20%EC%82%AC%EC%9A%A9%ED%95%9C%20%ED%9A%8C%EB%B3%B5%EC%84%B1%20%ED%8C%A8%ED%84%B4/images/7.png)

- Resilience4j에서 벌크헤드 패턴을 구현하려면 @CircuitBreaker와 이 패턴을 결합하는 구성을 추가해야 한다. 이 작업을 수행하는 몇 가지 코드를 살펴보자.
  - loadUserByUsername(..) 호출을 위한 별도 스레드 풀 설정
  -  bootstrap.yml 파일에 벌크헤드 구성 정보 생성
  - 세마포어 방식에서 maxConcurrentCalls와 maxWaitDuration 프로퍼티 설정
  - 스레드 풀 방식에서 maxThreadPoolSize, coreThreadPoolSize, queueCapacity, keepAliveDuration 설정  

> member-service: src/main/resources/bootstrap.yml

```yaml
...

resilience4j.bulkhead:
  instances:
    bulkheadMemberService:
      maxWaitDuration: 10ms  # 스레드를 차단할 최대 시간
      maxConcurrentCalls: 20  # 최대 동시 호출 수

resilience4j.thread-pool-bulkhead:
  instances:
    bulkheadMemberService:
      maxThreadPoolSize: 1  # 스레드 풀에서 최대 스레드 수 
      coreThreadPoolSize: 1 # 코어 스레드 풀 크기
      queueCapacity: 1   # 큐 용량 
      keepAliveDuration: 20ms   # 유휴 스레드가 종료되기 전 새 태스크를 기다리는 최대 시간
```

- Resilience4j를 사용하면 애플리케이션 프로퍼티를 통해 벌크헤드의 동작을 맞춤 설정할 수 있다. 회로 차단기와 마찬가지로 인스턴스를 원하는 만큼 생성할 수 있으며, 각 인스턴스에 서로 다른 구성을 설정할 수 있다.
  - **maxWaitDuration**: 벌크헤드에 들어갈 때 스레드를 차단할 최대 시간을 설정한다. 기본값은 0이다.
  - **maxConcurrentCalls**: 벌크헤드에서 허용되는 최대 동시 호출 수를 설정한다. 기본값은 25다.
  - **maxThreadPoolSize**: 최대 스레드 풀 크기를 설정한다. 기본값은 Runtime.getRuntime().availableProcessors()다.
  - **coreThreadPoolSize**: 코어 스레드 풀 크기를 설정한다. 기본값은 Runtime.getRuntime().availableProcessors()다.
  - **queueCapacity**: 큐 용량을 설정한다. 기본값은 100이다.
  - **keepAliveDuration**: 유휴 스레드가 종료되기 전에 새 작업을 기다리는 최대 시간을 설정한다. 이 시간은 스레드 수가 코어 스레드 수보다 많을 때 발생한다. 기본값은 20ms다.

- 사용자에게 맞는 스레드 풀의 적절한 크기는 얼마일까? 이 질문에 답하는 데 다음 공식을 사용할 수 있다.

```
(서비스가 정상일 때 최고점(peak)에서 초당 요청 수×99 백분위수(P99) 지연 시간(단위: 초)) + 부하를 대비해서 약간의 추가 스레드
```

- 서비스가 부하를 받는 상황에서 동작하기 전까지 서비스의 성능 특성을 알지 못할 경우가 많다. 스레드 풀 프로퍼티를 조정해야 하는 주요 지표는 대상이 되는 원격 자원이 정상인 상황에서도 서비스 호출이 타임아웃을 겪고 있을 때다. 다음 코드는 벌크헤드를 설정하는 방법을 보여 준다.

> member-service: src/main/java/.../service/MemberInfoService.java

```java
...
public class MemberInfoService implements UserDetailsService {
  ...

    @CircuitBreaker(name = "memberService", fallbackMethod = "fallbackLoadUserByUserName")
    // Resilience4j 회로 차단기를 사용하여 loadUserByUsername(..) 메서드를 @CircuitBreaker로 래핑한다.
    @Bulkhead(name = "bulkheadMemberService", fallbackMethod = "fallbackLoadUserByUserName")
    // 벌크헤드 패턴을 위한 인스턴스 이름과 폴백 메서드를 설정한다.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        randomlyRunLong();
    ...
    }

    public UserDetails fallbackLoadUserByUserName(String username, Throwable t) throws UsernameNotFoundException {
        return MemberInfo.builder().build();
    }
  
  ...
}
```

- 가장 먼저 주목할 점은 <code>@Bulkhead</code>라는 새로운 애너테이션이며, 이 애너테이션은 벌크헤드 패턴을 설정하고 있다는 것을 나타낸다. 애플리케이션 프로퍼티에 다른 값을 더 설정하지 않는다면 Resilience4j는 앞서 언급한 <code>세마포어 벌크헤드 타입</code>에 대한 기본값들을 사용한다.

- 두 번째 주목할 점은 벌크헤드 타입을 별도로 설정하지 않았다는 것이다. 이 경우 벌크헤드 패턴은 세마포어 방식을 사용하며, 스레드 풀 방식으로 변경하려면 다음과 같이 @Bulkhead 애너테이션을 추가해야 한다.

```java
@Bulkhead(name="bulkheadMemberService", type=Bulkhead.Type.THREADPOOL, fallbackMethod = "fallbackLoadUserByUserName")
```

## 재시도 패턴 구현

- 이름에서 알 수 있듯이, 재시도 패턴(retry pattern)은 서비스가 처음 실패했을 때 서비스와 통신을 재시도하는 역할을 한다. 이 패턴의 핵심 개념은 고장(예:  네트워크 장애)이 나도 동일한 서비스를 한 번 이상 호출해서 기대한 응답을 얻을 수 있는 방법을 제공하는 것이다. 이 패턴의 경우 해당 서비스 인스턴스에 대한 재시도 횟수와 재시도 사이에 전달하려는 간격을 지정해야 한다.

- 회로 차단기와 마찬가지로 Resilience4j를 사용하면 재시도하고 싶지 않은 예외를 지정할 수 있다. 다음 코드는 재시도 구성 매개변수가 포함된 회원 서비스의 bootstrap.yml 파일을 보여 준다.

> member-service: src/main/resources/bootstrap.yml

```yaml
...
resilience4j.retry:
  instances:
    retryMemberService:
      maxRetryAttempts: 5  # 재시도 최대 횟수
      waitDuration: 10000  # 재시도 간 대기 시간
      retry-exceptions:  # 재시도 대상이 되는 예외(exception) 목록
        - java.util.concurrent.TimeoutException
```

- **maxRetryAttempts**: 서비스에 대한 재시도 최대 횟수를 정의한다. 이 매개변수의 기본값은 3이
- **waitDuration**: 재시도 사이의 대기 시간을 정의하고 기본값은 500ms다.
- **retry-exceptions**: 재시도할 예외 클래스 목록을 설정하고 기본값은 없다(empty).
- **intervalFunction**: 실패 후 대기 시간 간격을 수정하는 함수를 설정한다.
- **retryOnResultPredicate**: 결과에 따라 재시도 여부를 판별하도록 설정한다. 재시도하려면 true를 반환해야 한다.
- **retryOnExceptionPredicate**: 예외에 따라 재시도 여부를 판별하도록 설정한다. retryOnResultPredicate과 마찬가지로 true를 반환하면 재시도한다.
- **ignoreExceptions**: 무시해서 재시도하지 않는 에러 클래스 리스트를 설정한다. 기본값은 없다(empty).

- 다음 코드는 재시도 패턴을 설정하는 방법을 보여 준다.

> member-serivce: src/main/java/.../service/MemberInfoService.java

```java
...
public class MemberInfoService implements UserDetailsService {
  ...
  @Override
  @Retry(name="retryMemberService", fallbackMethod = "fallbackLoadUserByUserName")  // 재시도 패턴을 위해 인스턴스 이름과 폴백 메서드를 설정한다.
  @CircuitBreaker(name="memberService", fallbackMethod = "fallbackLoadUserByUserName")  // Resilience4j 회로 차단기를 사용하여 loadUserByUsername(..) 메서드를 @CircuitBreaker로 래핑한다.
  @Bulkhead(name="bulkheadMemberService", fallbackMethod = "fallbackLoadUserByUserName") // 벌크헤드 패턴을 위한 인스턴스 이름과 폴백 메서드를 설정한다.
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    ...    
  }
  
  ...
}
```

## 속도 제한기 패턴 구현

- 재시도 패턴은 주어진 시간 내 소비할 수 있는 양보다 더 많은 호출로 발생하는 서비스 과부하를 막는다. 이 패턴은 고가용성과 안정성을 위한 API를 준비하는 데 필수 기술이다.
- Resilience4j는 속도 제한기 패턴을 위해 AtomicRateLimiter와 SemaphoreBasedRateLimiter라는 두 가지 구현체를 제공한다. RateLimiter의 기본 구현체는 AtomicRateLimiter다.
- 먼저 <code>SemaphoreBasedRateLimiter</code>가 가장 단순하다. SemaphoreBasedRateLimiter는 하나의 java.util.concurrent.Semaphore에 현재 스레드 허용(permission) 수를 저장하도록 구현되었다. 이 경우 모든 사용자 스레드는 semaphore.tryAcquire() 메서드를 호출하고 새로운 limitRefreshPeriod가 시작될 때 semaphore.release()를 실행하여 내부 스레드에 호출을 트리거한다.
- SemaphoreBasedRate와 달리 <code>AtomicRateLimiter</code>는 사용자 스레드가 직접 모든 허용 로직을 실행하기 때문에 스레드 관리가 필요 없다. AtomicRateLimiter는 시작부터 나노초 단위의 사이클(cycle)로 분할하고 각 사이클 기간이 갱신 기간(단위: 나노초)이다. 그런 다음 매 사이클의 시작 시점에 가용한 허용(active permissions) 수를 설정함으로써 사이클 기간을 제한한다. 이 방식을 더 잘 이해할 수 있도록 다음 설정을 살펴보자.
  - **ActiveCycle**: 마지막 호출에서 사용된 사이클 번호
  - **ActivePermissions**: 마지막 호출 후 가용한 허용 수
  - **NanoToWait**: 마지막 호출 후 허용을 기다릴 나노초 수
- 이 구현에는 몇 가지 까다로운 로직이 있는데, 더 잘 이해하려면 이 패턴에 대해 다음 Resilience4j 선언을 고려할 수 있다.
  - 사이클은 동일한 시간 단위다.
  - 가용한 허용 수가 충분하지 않다면, 현재 허용 수를 줄이고 여유가 생길 때까지 대기할 시간을 계산함으로써 허용을 예약할 수 있다. 이 예약 기능은 Resilience4j에서 일정 기간 동안(limitForPeriod) 허용되는 호출 수를 정의할 수 있어 가능하다. 허용이 갱신되는 빈도 (limitRefreshPeriod)와 스레드가 허용을 얻으려고 대기할 수 있는 시간(timeoutDuration)으로 산출한다.
  - 이 패턴을 위해서는 타임아웃 시간, 갱신 제한 기간, 기간 동안 제한 수를 지정해야 한다. 다음 코드는 재시도 구성 매개변수가 포함된 라이선싱 서비스의 bootstrap.yml 파일을 보여 준다.

> member-service: src/main/resources/bootstrap.yml

```yaml
...

resilience4j.ratelimiter:
  instances:
    memberService:
      timeoutDuration: 1000ms  # 스레드가 허용을 기다리는 시간을 정의한다.
      limitRefreshPeriod: 5000  # 갱신 제한 기간을 정의한다.
      limitForPeriod: 5  # 갱신 제한 기간 동안 가용한 허용 수를 정의한다.
```

- **timeoutDuration**:  스레드가 허용을 기다리는 시간을 정의한다. 이 매개변수의 기본값은 5s(초)다. 
- **limitRefreshPeriod**: 갱신을 제한할 기간을 설정한다. 각 기간 후 속도 제한기는 권한 수를 limitRefreshPeriod 값으로 재설정한다. limitRefreshPeriod의 기본값은 500ns(나노초)다.
- **limitForPeriod**:는 한 번의 갱신 기간 동안 가용한 허용 수를 설정한다. 이 기본값은 50이다.

> src/main/java/.../service/MemberInfoService.java

```java
...
public class MemberInfoService implements UserDetailsService {
  
    ...
  
  @Override
  @RateLimiter(name="memberService", fallbackMethod = "fallbackLoadUserByUserName")  // 속도 제한기 패턴을 위한 인스턴스 이름과 폴백 메서드를 설정한다.
  @Retry(name="retryMemberService", fallbackMethod = "fallbackLoadUserByUserName")  // 재시도 패턴을 위해 인스턴스 이름과 폴백 메서드를 설정한다.
  @CircuitBreaker(name="memberService", fallbackMethod = "fallbackLoadUserByUserName")  // Resilience4j 회로 차단기를 사용하여 loadUserByUsername(..) 메서드를 @CircuitBreaker로 래핑한다.
  @Bulkhead(name="bulkheadMemberService", fallbackMethod = "fallbackLoadUserByUserName") // 벌크헤드 패턴을 위한 인스턴스 이름과 폴백 메서드를 설정한다.
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    ...
  }
  
  ...
}
```

- 벌크헤드 패턴과 속도 제한기 패턴의 주요 차이점은 벌크헤드 패턴이 동시 호출 수를 제한하는 역할을 하고(예: 한 번에 X개의 동시 호출만 허용), 속도 제한기는 주어진 시간 프레임 동안 총 호출 수를 제한할 수 있다는 것이다(예: Y초마다 X개의 호출 허용).
- 자신에게 적합한 패턴을 선택하려면 어떤 것이 필요한지 다시 확인하기 바란다. 동시 횟수를 차단하고 싶다면 벌크헤드가 최선이지만 특정 기간의 총 호출 수를 제한하려면 속도 제한기가 더 낫다. 두 시나리오를 모두 검토하고 있다면 이 둘을 결합할 수도 있다.