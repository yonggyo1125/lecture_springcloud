# 스프링 클라우드 게이트웨이를 이용한 서비스 라우팅
- 마이크로서비스와 같은 분산형 아키텍처에서는 보안과 로깅, 여러 서비스 호출에 걸친 사용자 추적처럼 중요한 작업을 해야 할 시점이 온다. 이 기능을 구현하려고 모든 개발 팀이 독자적인 솔루션을 구축할 필요 없이 이러한 특성을 모든 서비스에 일관되게 적용하길 원할 것이다. 공통 라이브러리나 프레임워크를 사용해서 이들 기능을 각 서비스에 직접 구축할 수도 있지만, 이 방법은 다음과 같은 결과를 초래할 수 있다.
    - **이러한 기능을 각 서비스에 일관되게 구현하기 어렵다**: 개발자는 제품 기능을 제공하는 데 매달리고 정신없이 바쁜 일상에 쫓겨 서비스 로깅이나 추적 기능이 요구되는 규제 산업에서 일하지 않는다면 이러한 기능 구현을 잊어버리기 쉽다.
    - **보안과 로깅 같은 횡단 관심사(cross-cutting concerns)의 구현 책임을 개별 개발 팀에 전가하면 잘못 구현하거나 아예 누락할 수 있다**: 횡단 관심사는 애플리케이션 전체에 적용할 수 있고, 애플리케이션의 다른 부분에 영향을 줄 수 있는 프로그램 설계 일부나 기능을 나타낸다.
    - **모든 서비스에 걸쳐 강한 의존성을 만들 수 있다**: 모든 서비스에 공유되는 공통 프레임워크에 더 많은 기능을 추가할수록 서비스 재컴파일과 재배포 없이 공통 코드의 동작 변경이나 추가는 더욱 어려워진다. 갑작스런 공유 라이브러리에 내장된 핵심 기능 업그레이드는 장기적인 이전 작업이 된다.
- 이 문제를 해결하려면 **횡단 관심사를 독립적으로 배치할 수 있고, 아키텍처의 모든 마이크로서비스 호출에 대한 필터와 라우터 역할을 할 수 있는 서비스로 추상화**해야 한다. 이러한 서비스를 <code>게이트웨이(gateway)</code>라고 한다. 서비스 클라이언트는 더 이상 마이크로서비스를 직접 호출하지 않는다. 그 대신 모든 호출은 단일 정책 시행 지점(PEP, Policy Enforcement Point) 역할을 하는 서비스 게이트웨이를 경유한 다음 최종 목적지로 라우팅된다.
- 구체적으로 살펴볼 스프링 클라우드 게이트웨이 사용 방법은 다음과 같다.
    - 하나의 URL 뒤에 모든 서비스를 배치하고 서비스 디스커버리(service discovery)를 사용하여 해당 호출을 실제 서비스 인스턴스에 매핑하는 방법
    - 서비스 게이트웨이를 경유하는 모든 서비스 호출에 상관관계 ID를 삽입하는 방법
    - HTTP 응답에 전달받은 상관관계 ID를 삽입하여 클라이언트로 재전송하는 방법

## 서비스 게이트웨이란?
- 지금까지 이전 장에서 구축한 마이크로서비스를 사용하여 웹 클라이언트로 개별 서비스를 직접 호출하거나 유레카 등 서비스 디스커버리 엔진을 사용하여 프로그램 방식으로 호출했다.

![image1](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/1.png)
> 서비스 게이트웨이가 없다면 서비스 클라이언트는 각 서비스의 엔드포인트를 직접 호출한다.

- 서비스 게이트웨이는 서비스 클라이언트와 호출되는 서비스 사이에서 중개 역할을 하고, 서비스 게이트웨이가 관리하는 하나의 URL로 통신한다. 또한 서비스 클라이언트 호출에서 보낸 경로를 분해하고 서비스 클라이언트가 호출하려는 서비스를 결정한다.
- 다음 그림은교통 정리를 하는 교통 경찰처럼 서비스 게이트웨이가 사용자를 대상 마이크로서비스와 해당 인스턴스까지 안내하는 방법을 보여 준다.

![image2](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/2.png)
> 서비스 게이트웨이는 서비스 클라이언트와 해당 서비스 인스턴스 사이에 위치하고, 모든 서비스 호출(내,외부 호출 모두)은 서비스 게이트웨이를 통과한다.

- 서비스 게이트웨이는 애플리케이션 내 마이크로서비스를 호출하기 위해 유입되는 모든 트래픽의 게이트키퍼(gatekeeper) 역할을 한다. 서비스 게이트웨이가 있으면 서비스 클라이언트는 각 서비스 URL을 직접 호출하지 않고 서비스 게이트웨이에 호출을 보낸다.
- 또한 서비스 게이트웨이는 클라이언트와 개별 서비스의 호출 사이에 있기 때문에 서비스를 호출하는 중앙 정책 시행 지점(PEP) 역할도 한다. 이 지점을 사용하면 각 개발 팀이 서비스 횡단 관심사를 구현하지 않고 한곳에서 수행할 수 있다. 서비스 게이트웨이에서 구현할 수 있는 횡단 관심사의 예는 다음과 같다.
  - **정적 라우팅(static routing)**: 서비스 게이트웨이는 단일 서비스 URL과 API 경로로 모든 서비스를 호출한다. 모든 서비스에 대해 하나의 서비스 엔드포인트만 알면 되므로 개발이 편해진다.
  - **동적 라우팅(dynamic routing)**: 서비스 게이트웨이는 유입되는 서비스 요청을 검사하고 요청 데이터를 기반으로 서비스 호출자를 위한 지능적 라우팅을 수행할 수 있다. 예를 들어 베타 프로그램에 참여하는 고객의 서비스 호출은 사람들이 사용하는 버전과 다른 버전의 코드가 실행되는 특정 서비스 클러스터로 라우팅된다.  
  - **인증(authentication)과 인가(authorization)**: 모든 서비스 호출이 서비스 게이트웨이로 라우팅되기 때문에 서비스 게이트웨이는 서비스 호출자가 자신의 인증 여부를 확인할 수 있는 적합한 장소다.  
  - **지표 수집(metric collection)과 로깅(logging)**: 서비스 호출이 게이트웨이를 통과하기 때문에 서비스 게이트웨이를 지표와 로그를 수집하는 데 사용할 수 있다. 또한 사용자 요청에 대한 중요한 정보가 있는지 확인하여 균일한 로깅을 보장할 수 있다. 그렇다고 개별 서비스에서 자료를 수집해서는 안 된다는 것은 아니다. 서비스 게이트웨이를 사용하면 서비스 호출 회수 및 응답 시간처럼 많은 기본 지표를 한곳에서 더 잘 수집할 수 있다.

- 초반부에서 유레카를 소개할 때 중앙 집중식 로드 밸런서가 어떻게 서비스의 단일 장애 지점과 병목점이 될 수 있는지 이야기했다. 서비스 게이트웨이도 올바르게 구현되지 않는다면 동일한 위험이 발생할 수 있기 때문에 구현할 때 다음 사항을 염두에 두기 바란다.
  - **로드 밸런서는 서비스 앞에 있을 때 유용하다**: 이 경우 로드 밸런서를 여러 서비스 게이트웨이 인스턴스 앞에 두는 것은 적절한 설계이며, 필요할 때 서비스 게이트웨이 구현체를 확장할 수 있다. 하지만 모든 서비스 인스턴스 앞에 로드 밸런서를 두는 것은 병목점이 될 수 있어 좋은 생각은 아니다.
  - **서비스 게이트웨이를 무상태(stateless)로 작성하라**: 어떤 정보도 서비스 게이트웨이의 메모리에 저장하면 안 된다. 주의하지 않으면 게이트웨이의 확장성을 제한할 수 있다. 따라서 데이터는 모든 서비스 게이트웨이 인스턴스에 복제되어야 한다.
  - **서비스 게이트웨이를 가볍게 유지하라**: 서비스 게이트웨이는 서비스를 호출할 때 ‘병목점’이 될 수 있다. 서비스 게이트웨이에서 여러 데이터베이스를 호출하는 복잡한 코드가 있다면 추적하기 어려운 성능 문제의 원인이 될 수 있다.
- 이제 스프링 클라우드 게이트웨이를 사용하여 서비스 게이트웨이를 구현하는 방법을 살펴보자. 스프링 클라우드 팀에서 선호하는 API 게이트웨이이기 때문에 우리는 스프링 클라우드 게이트웨이를 사용한다.

---

## 스프링 클라우드 게이트웨이 소개

- 스프링 클라우드 게이트웨이는 스프링 프레임워크 6, 프로젝트 리액터(Project Reactor), 스프링 부트 3.0을 기반으로 한 API 게이트웨이 구현체다. 그렇다면 논블로킹은 무슨 뜻일까? 논블로킹 애플리케이션은 주요 스레드를 차단하지 않는 방식으로 작성된다. 따라서 이러한 스레드는 언제나 요청을 받아 백그라운드에서 비동기식으로 처리하고 처리가 완료되면 응답을 반환한다. 스프링 클라우드 게이트웨이는 다음 기능들을 제공한다.
  - **애플리케이션의 모든 서비스 경로를 단일 URL에 매핑한다**: 스프링 클라우드 게이트웨이는 하나의 URL에 제한되지 않고 실제로 여러 경로의 진입점을 정의하고 경로 매핑을 세분화할 수 있다(각 서비스 엔드포인트가 고유한 경로로 매핑된다). 하지만 가장 일반적인 사용 사례라면 모든 서비스 클라이언트 호출이 통과하는 단일 진입점을 제공하는 것이다.
  - **게이트웨이로 유입되는 요청과 응답을 검사하고 조치를 취할 수 있는 필터(filters)를 작성한다**: 이 필터를 사용하면 코드에 정책 시행 지점을 삽입해서 모든 서비스 호출에 다양한 작업을 수행할 수 있다. 즉, 이 필터로 유입되고 유출되는 HTTP 요청 및 응답을 수정할 수 있다. 
  - **요청을 실행하거나 처리하기 전에 해당 요청이 주어진 조건을 충족하는지 확인할 수 있는 서술자(predicates)를 만든다**: 스프링 클라우드 게이트웨이에는 자체 Route Predicate Factories 세트가 포함되어 있다.
- 스프링 클라우드 게이트웨이를 시작하기 위해 다음 사항을 준비
  - 스프링 클라우드 게이트웨이를 위한 스프링 부트 프로젝트를 설정하고 의존성을 적절히 구성한다.
  - 유레카와 통신할 수 있는 게이트웨이를 구성한다.

### 스프링 부트 게이트웨이 프로젝트 설정
- 스프링 부트를 사용하여 스프링 클라우드 게이트웨이(Spring Cloud Gateway) 서비스를 설정한다.
- Spring Initializr(https://start.spring.io/)에서 새로운 프로젝트를 생성해서 시작해 보자.

![image3](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/3.png)

- 다음 추가 의존성을 build.gradle에 추가한다.
> implementation 'org.springframework.cloud:spring-cloud-starter-bootstrap'
>
> build.gradle

```groovy
plugins {
	id 'java'
	id 'org.springframework.boot' version '3.2.0'
	id 'io.spring.dependency-management' version '1.1.4'
}

group = 'org.choongang'
version = '0.0.1-SNAPSHOT'

java {
	sourceCompatibility = '17'
}

repositories {
	mavenCentral()
}

ext {
	set('springCloudVersion', "2023.0.0")
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-actuator'
	implementation 'org.springframework.cloud:spring-cloud-starter-config'
	implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
	implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
	implementation 'org.springframework.cloud:spring-cloud-starter-bootstrap'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

dependencyManagement {
	imports {
		mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
	}
}

tasks.named('test') {
	useJUnitPlatform()
}
```

- 다음 단계는 스프링 클라우드 컨피그 서버에서 설정을 조회하는 데 필요한 구성 정보를 src/main/resources/bootstrap.yml 파일에 설정하는 것이다.

> src/main/resources/bootstrap.yml

```yaml
spring:
  application:
    name: gateway-server  # 스프링 클라우드 컨피그 클라이언트가 조회될 서비스를 알 수 있도록 게이트웨이 서비스의 이름을 지정한다.
  cloud:
    config:
      uri: http://localhost:8071  # 스프링 클라우드 컨피그 서버의 위치를 설정한다.
```

### 유레카와 통신하는 스프링 클라우드 게이트웨이 구성

- 스프링 클라우드 게이트웨이는 앞서 만든 넷플릭스 유레카 디스커버리 서비스와 통합할 수 있다. 이를 통합하려면 방금 생성한 게이트웨이 서비스를 위한 구성(컨피그) 서버에 유레카 구성 정보를 추가해야 한다.
- 새로운 게이트웨이 서비스를 추가하는 첫 번째 단계는 스프링 컨피그 서버 저장소(저장소는 볼트, 깃, 파일 시스템 또는 클래스패스가 될 수 있다는 것을 기억하자)에 게이트웨이 서비스를 위한 구성 파일을 생성하는 것이다.
- 파일 이름은 해당 서비스의 bootstrap.yml 파일에서 정의된 spring.application.name 프로퍼티로 설정된다. 게이트웨이 서비스에서 spring.application.name을 gateway-server로 정의했다면, 구성 파일 이름도 gateway-server로 지정해야 한다. 확장자는 .properties나 .yml 둘 중 하나를 선택할 수 있다.

- 스프링 클라우드 컨피그 서버에 유레카 구성 정보 설정하기
> 스프링 클라우드 컨피그 서버 : src/main/resources/config/gateway-server.yml

```yaml
server:
  port: 8072

eureka:
  instance:
    preferIpAddress: true
  client:
    registerWithEureka: true
    fetchRegistry: true
    serviceUrl:
      defaultZone: http://localhost:8070/eureka/

management:
  endpoint:
    gateway:
      enabled: true
  endpoints:
    web:
      exposure:
        include:
          - "gateway"
```

---

## 스프링 클라우드 게이트웨이에서 라우팅 구성 

- 스프링 클라우드 게이트웨이는 본래 리버스 프록시다. <code>리버스 프록시(reverse proxy)</code>는 자원에 도달하려는 클라이언트와 자원 사이에 위치한 중개 서버다. 클라이언트는 어떤 서버와 통신하고 있는지도 알지 못한다. 리버스 프록시는 클라이언트 요청을 캡처한 후 클라이언트를 대신하여 원격 자원을 호출한다.
- 마이크로서비스 아키텍처에서 스프링 클라우드 게이트웨이(리버스 프록시) 클라이언트의 마이크로서비스 호출을 받아 상위(upstream) 서비스에 전달한다. 서비스 클라이언트는 오직 게이트웨이와 통신한다고 생각한다. 하지만 실제로는 그렇게 간단하지 않다. 상위 서비스와 통신하려면 게이트웨이는 유입된 호출이 상위 경로에 매핑하는 방법을 알아야 한다. 스프링 클라우드 게이트웨이에서는 이를 수행할 수 있는 몇 가지 메커니즘을 제공한다.
  - 서비스 디스커버리를 이용한 자동 경로 매핑
  - 서비스 디스커버리를 이용한 수동 경로 매핑

### 서비스 디스커버리를 이용한 자동 경로 매핑 

- 게이트웨이에 대한 모든 경로 매핑은 /configserver/src/main/resources/config/gateway-server.yml 파일에서 경로를 정의해서 수행한다. 하지만 스프링 클라우드 게이트웨이는 다음 코드처럼 gateway-server 구성 파일에 구성 정보를 추가해서 서비스 ID를 기반으로 요청을 자동으로 라우팅할 수 있다.
- gateway-server.yml 파일에 discovery locator 설정하기  

> 스프링 클라우드 컨피그 서버 : src/main/resources/config/gateway-server.yml

```yaml
...

spring:
  cloud:
    gateway:
      discovery.locator:  # 서비스 디스커버리에 등록된 서비스를 기반으로 게이트웨이가 경로를 생성하도록 설정한다.
        enabled: true
        lowerCaseServiceId: true
```

- 스프링 클라우드 게이트웨이는 호출되는 서비스의 유레카 서비스 ID를 자동으로 사용하여 하위 서비스 인스턴스와 매핑한다.
- 예) http://localhost:8072/board-service/api/v1/board

![image4](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/4.png)


- 게이트웨이 서버는 http://localhost:8072 엔드포인트로 액세스된다. 호출하려는 서비스(게시판 서비스)는 엔드포인트 경로의 첫 번째 부분(board-service)으로 표시된다. 

![image5](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/5.png)
> 스프링 클라우드 게이트웨이는 요청을 게시판 서비스의 인스턴스로 매핑하고 board-service 애플리케이션 이름을 사용한다.

- 유레카와 함께 스프링 클라우드 게이트웨이를 사용하는 장점은 호출할 수 있는 엔드포인트가 하나라는 사실 외에 게이트웨이를 수정하지 않고도 서비스 인스턴스를 추가 및 제거할 수 있다는 것이다. 예를 들어 유레카에 새로운 서비스를 추가하면 게이트웨이는 서비스의 물리적 엔드포인트 위치에 대해 유레카와 통신하기 때문에 자동으로 호출을 라우팅할 수 있다.

- 게이트웨이 서버가 관리하는 경로를 확인하려면 게이트웨이 서버의 actuator/gateway/routes 엔드포인트를 통해 경로 목록을 볼 수 있다. 이 엔드포인트는 서비스의 모든 매핑 목록을 반환한다.

![image6](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/6.png)
> 유레카에 매핑된 각 서비스는 이제 스프링 클라우드 게이트웨이 경로로 매핑된다.

- 스프링 클라우드 게이트웨이에 등록된 서비스의 매핑 정보를 보여 준다. 또한 서술자(predicate), 관리 포트, 경로 ID, 필터 등 추가적인 데이터를 확인할 수 있다.

### 서비스 디스커버리를 이용한 수동 경로 매핑

- 스프링 클라우드 게이트웨이는 유레카 서비스 ID로 생성된 자동화된 경로에만 의존하지 않고 명시적으로 경로 매핑을 정의할 수 있어 코드를 더욱 세분화할 수 있다.
- 게이트웨이 기본 경로인 /board-service/api/v1/board로 게시판 서비스에 액세스하는 대신 게시판 서비스 이름을 줄여 경로를 단순화한다고 가정해 보자. 스프링 클라우드 컨피그 서버 저장소에 있는 구성 파일(/configserver/src/main/resources/config/gateway-server.yml)을 수동으로 정의하여 수행할 수 있다. 다음 코드에서 이 수동 매핑 방법을 볼 수 있다.

- gateway-server.yml 파일에서 수동 경로 매핑하기

> 스프링 클라우드 컨피그 서버 : src/main/resources/config/gateway-server.yml

```yaml
spring:
  cloud:
    gateway:
      discovery.locator:  # 서비스 디스커버리에 등록된 서비스를 기반으로 게이트웨이가 경로를 생성하도록 설정한다.
        enabled: true
        lowerCaseServiceId: true
      routes:
        - id: board-service  # 이 선택적(optional) ID는 임의의 경로에 대한 ID다.
          uri: lb://board-service   # 이 경로 대상 URI를 설정한다.

          predicates:  # 경로(path)는 load() 메서드로 설정되지만, 여러 옵션 중 하나다.
            - Path=/board/**

          filters:  # 응답을 보내기 전이나 후에 요청 또는 응답을 수정하고 스프링 web.filters들을 필터링 한다.
            - RewritePath=/board/(?<path>.*),/$\{path}  # 매개변수 및 교체 순서(replacement order)로 경로 정규식(path regexp)을 받아 요청 경로를 /board/**에서 /**으로 변경한다.
```

- 이 구성을 추가하면 /board/api/v1/board 경로로 조직 서비스에 액세스할 수 있다. 이제 게이트웨이 서버의 엔드포인트를 재확인하면 그림과 같은 결과가 출력된다. 주의 깊게 살펴보면 조직 서비스에 대해 두 개의 항목이 있음을 알 수 있다.
  - 첫 번째 서비스 항목은 gateway-server.yml 파일에서 정의한 매핑(board/**:board-service)이고,
  - 다른 서비스 항목은 조직 서비스에 대해 유레카 ID를 기반으로 게이트웨이에서 생성된 자동 매핑(board-service/**: board-service)이다.

>  이트웨이가 유레카 서비스 ID를 기반으로 자동화된 경로 매핑을 사용하여 서비스를 노출할 때, 실행 중인 서비스 인스턴스가 없다면 게이트웨이는 서비스 경로를 아예 노출하지 않는다. 그러나 수동으로 경로를 서비스 디스커버리 ID에 매핑하면 유레카에 등록된 인스턴스가 없더라도 게이트웨이는 여전히 경로를 표시한다. 이때 존재하지 않는 서비스에 대한 경로를 호출하면 HTTP 500 에러가 반환된

- 유레카 서비스 ID 경로에 대한 자동화된 매핑을 제외하고 직접 정의한 조직 서비스 경로만 지원하려고 한다면 gateway-server.yml 파일에 추가한 spring.cloud.gateway.discovery.locator 항목을 제거한다.

![image7](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/7.png)
> 게시판 서비스를 수동으로 매핑할 때 /actuator/gateway/routes에 대한 게이트웨이 호출 결과

> 자동 라우팅 사용 여부는 신중하게 고려해야 한다. 새로운 서비스가 많이 추가되지 않는 안정적인 환경에서 수동으로 경로를 추가하는 것은 단순하지만, 대규모 환경에서 새로운 서비스가 많다면 지루한 작업이 될 수 있다

- gateway-server.yml 파일에서 discovery locator 항목 제거하기 

> 스프링 클라우드 컨피그 서버 : src/main/resources/config/gateway-server.yml 

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: board-service  # 이 선택적(optional) ID는 임의의 경로에 대한 ID다.
          uri: lb://board-service   # 이 경로 대상 URI를 설정한다.

          predicates:  # 경로(path)는 load() 메서드로 설정되지만, 여러 옵션 중 하나다.
            - Path=/board/**

          filters:  # 응답을 보내기 전이나 후에 요청 또는 응답을 수정하고 스프링 web.filters들을 필터링 한다.
            - RewritePath=/board/(?<path>.*),/$\{path}  # 매개변수 및 교체 순서(replacement order)로 경로 정규식(path regexp)을 받아 요청 경로를 /board/**에서 /**으로 변경한다.
```

- 이제 게이트웨이 서버의 actuator/gateway/routes 엔드포인트를 호출하면 정의한 조직 서비스 매핑만 표시되어야 한다.

![image8](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/8.png)
> 게시판 서비스에 대한 수동 매핑만 있을 때 actuator/gateway/routes 호출 결과 

### 동적으로 라우팅 구성을 재로딩 

- 동적인 라우팅 재로딩 기능은 게이트웨이 서버의 재시작 없이 경로 매핑을 변경할 수 있기 때문에 유용하다. 따라서 기존 경로를 빠르게 수정할 수 있고, 해당 환경 내 각 게이트웨이 서버를 새로운 경로로 변경하는 작업을 수행한다.
- actuator/gateway/routes 엔드포인트를 입력하면 현재 게이트웨이에서 게시판 서비스(board service)를 볼 수 있다. 이제 새로운 매핑을 바로 추가하려면, 구성 파일을 변경하고 해당 변경 사항을 스프링 클라우드 컨피그 서버가 검색하는 구성 데이터가 저장된 깃 저장소를 반영하여 깃 허브에 커밋한다.
- 스프링 액추에이터(Spring Actuator)는 라우팅 구성 정보를 다시 로드할 수 있도록 POST 기반 엔드포인트 경로인 actuator/gateway/refresh를 노출한다. 이 actuator/gateway/refresh를 호출한 후 /routes 엔드포인트를 호출하면 두 개의 새로운 경로를 확인할 수 있다. actuator/gateway/refresh의 응답은 응답 내용(body) 없이 HTTP 200 상태 코드만 반환한다

---

## 스프링 클라우드 게이트웨이의 진정한 능력: Predicate와 Filter Factories

- 게이트웨이로 모든 요청을 프록시(proxy)할 수 있기 때문에 서비스 호출을 단순화할 수 있다. 하지만 스프링 클라우드 게이트웨이의 진정한 힘은 게이트웨이를 통하는 모든 서비스 호출에 적용될 사용자 정의 로직을 작성할 때 발휘된다. 대부분의 경우 모든 서비스에서 보안, 로깅, 추적 등 일관된 애플리케이션 정책을 적용하기 위해 이러한 사용자 정의 로직이 사용된다.
- 애플리케이션 정책 전략을 구현하기 위해 애플리케이션의 각 서비스를 수정하지 않고 모든 서비스에 적용하길 원하기 때문에 이러한 애플리케이션 정책들은 횡단 관심사(cross-cutting concerns)로 간주된다. 이러한 방식으로 스프링 클라우드 게이트웨이 Predicate과 Filter Factories는 스프링 관점(aspect) 클래스와 유사하게 사용할 수 있다.
- 이들을 사용하면 다양한 동작을 일치시키거나 가로챌 수 있어 원래 코드 작성자 모르게 호출 동작을 변경하거나 꾸밀 수 있다. 서블릿 필터나 스프링 관점 클래스가 특정 서비스에 맞게 적용되었더라도 게이트웨이와 게이트웨이의 Predicate 및 Filter Factories를 사용하면 게이트웨이로 라우팅되는 모든 서비스에 대한 공통 관심사를 구현할 수 있다.
- 서술자(predicate)를 사용하면 요청을 처리하기 전에 요청이 조건 집합을 충족하는지 확인할 수 있다. 그림은 요청이 스프링 클라우드 게이트웨이를 통해 유입될 때 서술자와 필터를 적용한 이 게이트웨이 아키텍처를 보여 준다.

![image9](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/9.png)
> 요청이 생성될 때 스프링 클라우드 게이트웨이 아키텍처에서 서술자와 필터를 적용하는 방법  

- 먼저 게이트웨이 클라이언트(브라우저, 앱 등) 스프링 클라우드 게이트웨이에 요청을 보낸다. 요청이 수신되면 게이트웨이 핸들러(gateway handler)로 이동하며, 이 핸들러는 요청된 경로가 액세스하려는 특정 경로의 구성과 일치 여부를 확인하는 역할을 한다. 정보가 모두 일치하면 필터를 읽고 해당 필터에 요청을 보내는 역할을 하는 게이트웨이로 진입한다. 요청이 모든 필터를 통과하면 해당 요청은 이제 설정된 경로의 마이크로서비스로 전달된다.

### 게이트웨이 Predicate Factories

- 게이트웨이의 서술자는 요청을 실행하거나 처리하기 전에 요청이 조건 집합을 충족하는지 확인하는 객체다. 경로마다 논리 AND로 결합할 수 있는 여러 Predicate Factories를 설정할 수 있다. 다음 표는 스프링 클라우드 게이트웨이의 모든 Predicate Factories를 나열한다.
- 이러한 서술자는 코드에 프로그래밍 방식이나 앞 절에서 생성한 구성 파일을 사용하여 적용할 수 있다. 책에서는 다음과 같은 구성 파일 안에 있는 predicates를 통해 서술자를 사용한다.

```yaml
predicates
    - Path=/board/**
```

> 스프링 클라우드 게이트웨이의 내장형 predicates

|Predicate| 설명                                                             |예|
|-----|----------------------------------------------------------------|------|
|Before| 설정된 일시 전에 발생한 요청인지 확인한다.                                       |Before=2020-03-11T...|
|After| 설정된 일시 이후에 발생한 요청인지 확인한다.                                      |After=2020-03-11T...|
|Between| 설정된 두 일시 사이에 발생한 호출인지 확인한다. 시작 일시는 포함되고 종료 일시는 포함되지 않는다(미만).   |Between=2020-03-11T..., 2020-04-11T...|
|Header| 헤더 이름과 정규식 매개변수를 사용하여 해당 값과 정규식을 확인한다.                         |Header=X-Request-Id, \d+|
|Host| “.” 호스트 이름 패턴으로 구분된 안티-스타일 패턴을 매개변수로 받아 Host 헤더를 주어진 패턴과 비교한다. |Host=**.example.com|
|Method| HTTP 메서드(verb)를 비교한다.                                          |Method=GET|
|Path|스프링 PathMatcher를 사용한다.|Path=/board/{bid}}
|Query|필수 매개변수와 정규식 매개변수를 사용하여 쿼리 매개변수와 비교한다.|Query=id, 1|
|Cookie|쿠키 이름과 정규식 매개변수를 사용하여 HTTP 요청 헤더에서 쿠키를 찾아 그 값과 정규식이 일치하는지 비교한다.|Cookie=SessionID, abc|
|RemoteAddr|IP 목록에서 요청의 원격 주소와 비교한다.|RemoteAddr=192.168.3.5/24|

### 게이트웨이 Filter Factories

- 게이트웨이의 Filter Factories를 사용하면 코드에 정책 시행 지점(PEP)을 삽입하여 모든 서비스 호출에 대해 일관된 방식으로 작업을 수행할 수 있다. 즉, 이러한 필터로 수신 및 발신하는 HTTP 요청과 응답을 수정할 수 있다. 다음 표에는 스프링 클라우드 게이트웨이의 모든 필터 목록이 포함되어 있다.

> 스프링 클라우드 게이트웨이의 내장형 필터

|Filter|설명| 예                                        |
|-----|-------|------------------------------------------|
|AddRequestHeader|매개변수로 받은 이름과 값을 HTTP 요청 헤더에 추가한다.| AddRequestHeader=X-Board-ID, F39s2       |
|AddResponseHeader|매개변수로 받은 이름과 값을 HTTP 응답 헤더에 추가한다.| AddResponseHeader=X-Board-ID, F39s2      |
|AddRequestParameter|매개변수로 받은 이름과 값을 HTTP 쿼리 매개변수로 추가한다.| AddRequestParameter=X-Board-ID, F39s2    |
|PrefixPath|HTTP 요청 경로에 접두 경로를 추가한다.| PrefixPath=/api                          |
|RequestRatelimiter|다음 세 개의 매개변수를 받는다.<br>- replenishRate: 사용자에게 초당 허용된 요청 수를 나타낸다.<br>- capacity: 폭증 허용 용량을 정의한다.<br>- keyResolverName: KeyResolver 인터페이스를 구현하는 빈(bean) 이름을 정의한다.| RequestRateLimiter=10, 20, #{@userKeyResolver} |
|RedirectTo|HTTP 상태 코드와 URL을 받는다. 상태 코드는 300 리다이렉트 HTTP 코드가 되어야 한다.| RedirectTo=302,http://localhost:8072     |
|RemoveNonProxy|KeepAlive, Proxy-Authenticate 또는 Proxy-Authorization 같은 헤드를 제거한다.| NA                                       |
|RemoveRequestHeader|HTTP 요청에서 매개변수로 받은 이름과 일치하는 헤더를 제거한다.| RemoveRequestHeader=X-Request-Foo        |
|RemoveResponseHeader|HTTP 응답에서 매개변수로 받은 헤더 이름과 일치하는 헤더를 제거한다.| RemoveResponseHeader=X-Board-ID          |
|RewritePath|경로 정규식(regexp) 매개변수와 대체(replacement) 매개변수를 설정한다.| RewritePath=/board/(?<path>.*), /$\{path}|
|SecureHeaders|응답에 secure header를 추가하고 요청 경로를 수정할 경로 템플릿 매개변수를 받는다.|NA|
|SetPath|매개변수로 경로 템플릿을 받는다. 경로에 대한 템플릿화된 세그먼트로 요청 경로를 조작한다. 스프링 프레임워크의 URI 템플릿을 사용하며, 복수로 매칭되는 세그먼트가 허용된다.|SetPath=/{board}|
|SetResponseHeader|HTTP 응답 헤더를 설정하고자 이름과 값을 설정한다.|SetResponseHeader=X-Response-ID, 123|

### 사용자 정의 필터 

- 게이트웨이를 경유하는 모든 요청을 프록시하는 기능을 사용하여 서비스 호출을 단순화할 수 있다. 하지만 스프링 클라우드 게이트웨이의 진정한 강점은 게이트웨이를 통과하는 모든 서비스 호출에 적용될 수 있는 사용자 정의 로직을 작성하려고 할 때 발휘된다. 이 사용자 정의 로직은 대부분 모든 서비스 간에 보안, 로깅 및 추적 등 일관된 애플리케이션 정책을 적용하는 데 사용된다.
- 스프링 클라우드 게이트웨이 내에서 필터를 사용하여 사용자 정의 로직을 만들 수 있다. 필터를 사용하여 각 서비스 요청이 통과하는 비즈니스 로직 체인을 구현할 수 있다. 스프링 클라우드 게이트웨이는 다음 두 가지 종류의 필터를 지원한다.
  - **사전 필터(pre-filters)**: 실제 요청이 목적지로 전송되기 전에 사전 필터가 호출된다. 사전 필터는 일반적으로 서비스가 일관된 메시지 형식인지 확인하는 작업(예 주요 HTTP 헤더 존재 여부 확인)을 수행하거나 서비스를 호출하는 사용자가 인증되었는지 확인하는 게이트키퍼 역할을 한다.
  - **사후 필터(post-filters)**: 사후 필터는 대상 서비스 이후에 호출되고 응답은 클라이언트로 다시 전송된다. 일반적으로 대상 서비스의 응답을 다시 기록하거나 오류를 처리하거나 민감한 정보에 대한 응답을 검사하려고 사후 필터를 구현한다.

![image10](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/10.png)
> 사전 필터, 대상 라우팅, 사후 필터는 클라이언트 요청이 흘러가는 파이프라인을 형성하고, 요청이 게이트웨이에 유입되면 사용자 지정 필터가 요청을 조작할 수 있다.

- 상기 그림에서 전개된 흐름을 따라가면 항상 서비스 클라이언트(게이트웨이로 노출된 서비스를 호출하는)에서 시작된다는 것을 알 수 있다. 이 과정에는 다음과 같은 단계가 있다.
  - 게이트웨이에서 정의된 모든 <code>사전 필터</code>는 요청이 게이트웨이에 유입될 때 호출된다. 사전 필터는 **HTTP 요청이 실제 서비스에 도달하기 전에 검사하고 수정**한다. **하지만 사용자를 다른 엔드포인트나 서비스로 리다이렉션할 수는 없다.**
  - 게이트웨이에 들어오는 요청에 대해 사전 필터가 실행된 후 게이트웨이는 목적지(서비스가 향하는 곳)를 결정한다.
  - 대상 서비스가 호출된 후 게이트웨이의 <code>사후 필터</code>가 호출된다. 사후 필터는 **호출된 서비스의 응답을 검사하고 수정한다.**

- 게이트웨이 필터를 구현하는 방법을 이해하는 가장 좋은 방법은 실제 동작하는 모습을 보는 것이다. 이를 위해 다음 여러 절에서 사전 및 사후 필터를 만들고, 이 필터들에 클라이언트 요청을 보낼 것이다. 그림은 각 서비스에 대한 요청을 처리하는 데 이들 필터가 어떻게 작용하는지 보여 준다.

![image11](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/11.png)
> 게이트웨이 필터는 서비스 호출 및 로깅에 대한 중앙 집중식 추적 기능을 제공하는데, 이들 필터를 사용하면 마이크로서비스 호출에 대해 사용자 정의 규칙 및 정책을 시행할 수 있다.

- 상기 그림의 흐름에 따라 다음과 같은 사용자 정의 필터가 사용되는 것을 알 수 있다.
  - **추적 필터(tracking filter)**: 추적 필터는 게이트웨이로 들어오는 모든 요청과 연관된 상관관계 ID가 있는지 확인하는 사전 필터다. 상관관계 ID는 고객 요청을 수행할 때 실행되는 모든 마이크로서비스에 걸쳐 전달되는 고유 ID다. 상관관계 ID를 사용하면 호출이 일련의 마이크로서비스를 통과할 때 발생하는 이벤트 체인을 추적할 수 있다.
  - **대상 서비스(target service)**: 대상 서비스는 조직 또는 라이선스 서비스일 수 있다. 두 서비스 모두 HTTP 요청 헤더에서 상관관계 ID를 받는다.
  - **응답 필터(response filter)**: 응답 필터는 서비스 호출과 연관된 상관관계 ID를 클라이언트에 전송될 HTTP 응답 헤더에 삽입하는 사후 필터다. 이 방식으로 클라이언트도 요청과 연결된 상관관계 ID에 액세스할 수 있다.  

---

## 사전 필터 만들기

- 스프링 클라우드 게이트웨이에서 필터를 만드는 것은 간단하다. 시작하려면 먼저 게이트웨이로 유입되는 모든 요청을 검사하고, 요청에서 tmx-correlation-id라는 HTTP 헤더의 포함 여부를 확인하는 TrackingFilter라는 사전 필터를 만든다. tmx-correlation-id 헤더에는 여러 마이크로서비스를 거쳐 사용자 요청을 추적하는 데 사용되는 고유한 GUID(Globally Universal ID)가 포함된다.
  - tmx-correlation-id가 HTTP 헤더에 없으면 게이트웨이 TrackingFilter가 상관관계 ID를 생성하고 설정한다.
  - 상관관계 ID가 이미 있다면 게이트웨이는 아무 일도 하지 않는다(상관관계 ID가 있다는 것은 이 특정 서비스 호출이 사용자 요청을 수행하는 서비스 호출 체인의 한 부분임을 의미한다).

> gateway-service : src/main/java/.../filters/FilterUtils.java 

```java
package org.choongang.gateway.filters;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@Component
public class FilterUtils {
    public static final String CORRELATION_ID = "tmx-correlation-id";
    public static final String AUTH_TOKEN     = "tmx-auth-token";
    public static final String USER_ID        = "tmx-user-id";
    public static final String ORG_ID         = "tmx-org-id";
    public static final String PRE_FILTER_TYPE = "pre";
    public static final String POST_FILTER_TYPE = "post";
    public static final String ROUTE_FILTER_TYPE = "route";

    public String getCorrelationId(HttpHeaders requestHeaders){
        if (requestHeaders.get(CORRELATION_ID) !=null) {
            List<String> header = requestHeaders.get(CORRELATION_ID);
            return header.stream().findFirst().get();
        }
        else{
            return null;
        }
    }

    public ServerWebExchange setRequestHeader(ServerWebExchange exchange, String name, String value) {
        return exchange.mutate().request(
                        exchange.getRequest().mutate()
                                .header(name, value)
                                .build())
                .build();
    }

    public ServerWebExchange setCorrelationId(ServerWebExchange exchange, String correlationId) {
        return this.setRequestHeader(exchange, CORRELATION_ID, correlationId);
    }
}
```

> gateway-service : src/main/java/.../filters/TrackingFilter.java 

```java
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
```

- 스프링 클라우드 게이트웨이에서 글로벌 필터를 생성하려면 GlobalFilter 클래스를 구현한 후 filter() 메서드를 재정의해야 한다. 이 메서드는 필터에서 구현하는 비즈니스 로직을 포함한다. 이전 코드에서 주목해야 할 중요한 점은 ServerWebExchange 개체에서 HTTP 헤더를 추출하는 방식이다.

```java 
HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
```

- 호출이 전달되면, 필터를 통과할 때 전달된 상관관계 ID가 기록된 로그 메시지가 다음과 같이 콘솔에 출력되어야 한다.
- 
```yaml
2024-03-11T22:18:54.548+09:00 DEBUG 9916 --- [gateway-server] [ctor-http-nio-2] o.c.gateway.filters.TrackingFilter       : tmx-correlation-id generated in tracking filter: ba1561f9-9374-4a18-b302-6631cfe2d644
```

- 콘솔에서 이 메시지가 출력되지 않는다면 게이트웨이 서버의 bootstrap.yml 구성 파일에 코드 8-11처럼 로그를 설정한다. 그런 다음 이 마이크로서비스를 빌드하고 실행하자.

> gateway-server: src/main/resources/bootstrap.yml

```yaml
logging:
  level:
    com.netflix: WARN
    org.springframework.web: WARN
    org.choongang: DEBUG
```

---

## 서비스에서 상관관계 ID 사용

- 이제 게이트웨이를 통과하는 모든 마이크로서비스 호출에 상관관계 ID가 추가되었기 때문에 다음 사항을 확인하고자 한다.
  - 상관관계 ID는 호출된 마이크로서비스가 쉽게 액세스할 수 있다.
  - 마이크로서비스로 수행될 모든 하위 서비스 호출에도 상관관계 ID가 전파된다.

- 이를 구현하려고 각 마이크로서비스에서 <code>UserContextFilter</code>, <code>UserContext</code>, <code>UserContext Interceptor</code> 세 가지 클래스 세트를 빌드한다. 이러한 클래스는 유입되는 HTTP 요청의 상관관계 ID(나중에 추가할 다른 정보도 함께)를 읽기 위해 협업하고, 애플리케이션의 비즈니스 로직에서 쉽게 액세스하고 사용할 수 있는 클래스에 ID를 매핑해서 모든 하위 서비스 호출에 전파할 것이다. 그림 8-12는 라이선싱 서비스에서 이러한 다양한 부분을 어떻게 빌드하는지 보여 준다.

![image12](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/12.png)
> 공통 클래스들을 사용하여 상관관계 ID를 하위 서비스 호출에 전파한다.

- 게이트웨이로 라이선싱 서비스를 호출할 때 TrackingFilter는 게이트웨이로 유입되는 모든 호출에 대해 상관관계 ID를 삽입한다.
- 사용자가 정의할 수 있는 HTTP ServletFilter인 UserContextFilter 클래스는 상관관계 ID를 UserContext 클래스에 매핑한다. UserContext 클래스는 해당 호출의 나중 부분에서 사용될 것을 대비하여 그 값을 스레드에 저장한다.
- 회원 서비스의 비즈니스 로직은 게시판 서비스에 대한 호출을 실행한다.
- RestTemplate은 조직 서비스를 호출한다. RestTemplate은 사용자 정의 스프링 인터셉터 클래스인 UserContextInterceptor를 사용하여 상관관계 ID를 아웃바운드 호출의 HTTP 헤더에 삽입한다.

### 유입되는 HTTP 요청을 가로채는 UserContextFilter

- 첫 번째 작성할 클래스는 <code>UserContextFilter</code>다. 이 클래스는 서비스로 들어오는 모든 HTTP 요청을 가로채고, HTTP 요청에서 사용자 컨텍스트 클래스로 상관관계 ID(와 몇 가지 다른 정보)를 매핑하는 HTTP 서블릿 필터다.



> member-service: src/main/java/.../utils/UserContextFilter.java

```java
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
```

### 서비스에 쉽게 액세스할 수 있는 HTTP 헤더를 만드는 UserContext

- UserContext 클래스는 마이크로서비스가 처리하는 각 서비스 클라이언트 요청의 HTTP 헤더 값을 보관한다. 이 클래스는 java.lang.ThreadLocal에서 값을 조회하고 저장하는 getter/setter 메서드로 구성된다.

> member-service: src/main/java/.../utils/UserContext.java

```java
package org.choongang.member.utils;

import org.springframework.stereotype.Component;

@Component
public class UserContext {
    public static final String CORRELATION_ID = "tmx-correlation-id";
    public static final String AUTH_TOKEN     = "tmx-auth-token";
    public static final String USER_ID        = "tmx-user-id";
    public static final String ORGANIZATION_ID = "tmx-organization-id";

    private String correlationId= new String();
    private String authToken= new String();
    private String userId = new String();
    private String organizationId = new String();

    public String getCorrelationId() { return correlationId;}
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getOrganizationId() {
        return organizationId;
    }
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

}
```
- 여기에서 UserContext 클래스는 유입되는 HTTP 요청에서 가져온 값을 보유하는 POJO에 불과하다.

> member-service: src/main/java/.../utils/UserContextHolder.java
-  UserContextHolder 클래스로 사용자 요청을 처리하는 해당 스레드에서 호출하는 모든 메서드에 접근 가능한 ThreadLocal 변수에 UserContext를 저장한다.

```java
package org.choongang.member.utils;

import org.springframework.util.Assert;

public class UserContextHolder {
    private static final ThreadLocal<UserContext> userContext = new ThreadLocal<UserContext>();

    public static final UserContext getContext(){
        UserContext context = userContext.get();

        if (context == null) {
            context = createEmptyContext();
            userContext.set(context);

        }
        return userContext.get();
    }

    public static final void setContext(UserContext context) {
        Assert.notNull(context, "Only non-null UserContext instances are permitted");
        userContext.set(context);
    }

    public static final UserContext createEmptyContext(){
        return new UserContext();
    }
}
```
### 상관관계 ID 전파를 위한 사용자 정의 RestTemplate과 UserContextInterceptor 

- 이 클래스는 RestTemplate 인스턴스에서 실행되는 모든 HTTP 기반 서비스 발신 요청에 상관관계 ID를 주입한다. 이 작업은 서비스 호출 간 링크를 설정하는 데 수행된다. 이를 위해 RestTemplate 클래스에 주입된 스프링 인터셉터를 사용할 것이다.

> member-service: src/main/java/.../utils/UserContextInterceptor.java

```java
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
```

- UserContextInterceptor를 사용하려면 RestTemplate 빈(bean)을 정의한 후 UserContext Interceptor를 그 빈에 추가해야 한다. 

> member-service: src/main/java/.../config/BeanCojnfig.java

```java
...
public class BeanConfig {
  ...

  @Bean
  @LoadBalanced  // 이 RestTemplate 객체가 로드 밸런서를 사용한다는 것을 나타낸다.
  public RestTemplate restTemplate() {

    RestTemplate template = new RestTemplate();
    List<ClientHttpRequestInterceptor> interceptors = template.getInterceptors();
    if (interceptors == null) { // RestTemplate 인스턴스에 UserContextInterceptor를 추가한다.
      template.setInterceptors(Collections.singletonList(new UserContextInterceptor()));
    } else {
      interceptors.add(new UserContextInterceptor());
      template.setInterceptors(interceptors);
    }

    return template;
  }
}
```
- 이 빈 정의가 코드에 있다면 @Autowired 애너테이션을 사용하고, RestTemplate을 클래스에 주입할 때마다 코드에서 생성된 RestTemplate(UserContextInterceptor가 설정된)을 사용한다.

> 로그 수집, 인증 등
> 
> 이제 각 서비스에 전달된 상관관계 ID가 있기 때문에 호출과 관련된 모든 서비스를 통과하는 트랜잭션을 추적할 수 있다. 이를 위해 각 서비스가 모든 서비스의 로그 항목을 단일 지점으로 캡처하는 중앙 로그 집계 지점에 기록해야 한다. 로그 집계 서비스에 수집된 모든 로그 항목에는 이와 연관된 상관관계 ID가 있다.
>
> 로그 집계 솔루션을 구현하는 것은 이 장 범위를 벗어나지만, 곧 다루게될  스프링 클라우드 슬루스(Sleuth) 사용 방법을 살펴볼 것이다. 스프링 클라우드 슬루스는 여기에서 작성한 TrackingFilter를 사용하지 않지만, 상관관계 ID를 추적하고 모든 호출에 삽입되도록 하는 데 동일한 개념을 사용한다.

---

## 상관관계 ID를 수신하는 사후 필터 작성 

- 스프링 게이트웨이 서비스 클라이언트를 대신하여 실제 HTTP 호출을 실행하고 대상 서비스 호출의 응답을 다시 검사한다는 것을 기억하라. 그런 다음 응답을 변경하거나 추가 정보를 더할 수 있다. 사전 필터에서 데이터를 캡처하는 것과 연관되었다면 게이트웨이 사후 필터는 지표를 수집하고 사용자의 트랜잭션과 관련된 모든 로깅을 완료하는 데 이상적인 위치다. 마이크로서비스에 전달한 상관관계 ID를 사용자에게 다시 전달해서 이것을 활용하고자 한다. 이러한 방식으로 메시지 본문을 건드리지 않고 상관관계 ID를 호출자에 다시 전달할 수 있다.

> gateway-server: src/main/java/.../filters/ResponseFilter.java

```java
package org.choongang.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Configuration
public class ResponseFilter {
    private final Logger logger = LoggerFactory.getLogger(ResponseFilter.class);

    @Autowired
    private FilterUtils filterUtils;

    @Bean
    public GlobalFilter postGlobalFilter() {
        return (exchange, chain) -> {
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
                String correlationId = filterUtils.getCorrelationId(requestHeaders);  // 원본 HTTP 요청에 전달된 상관관계 ID를 가진다.
                logger.debug("Adding the correlation id to the outbound headers. {}", correlationId);
                exchange.getResponse().getHeaders().add(FilterUtils.CORRELATION_ID, correlationId); // 응답에 상관관계 ID를 삽입한다.
                // 게이트웨이로 유입된 해당 사용자 요청의 오고 가는 항목을 모두 보여 주는 '북앤드'가 되도록 발신 요청 URI를 로깅한다.
                logger.debug("Completing outgoing request for {}.", exchange.getRequest().getURI());
            }));
        };
    }
}
```

- ResponseFilter가 구현되었다면 서비스를 실행하고 이 기능이 구현된 회원 및 게시판 서비스를 호출할 수 있다. 서비스 호출이 완료되면 그림처럼 호출의 HTTP 응답 헤더에 tmx-correlation-id를 볼 수 있다.


![image13](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/13.png)
> tmx-correlation-id를 응답 헤더에 추가하고 서비스 클라이언트에 되돌려 준다.

- 또한 그림 처럼 콘솔에서 로그 메시지를 볼 수 있다. 전달된 상관관계 ID 984967ee-8f9d-4ac4-aca6-3e1474009316가 사전 및 사후 필터를 통과할 때 기록된다.
- 
  ![image14](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/6.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EA%B2%8C%EC%9D%B4%ED%8A%B8%EC%9B%A8%EC%9D%B4%EB%A5%BC%20%EC%9D%B4%EC%9A%A9%ED%95%9C%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%9D%BC%EC%9A%B0%ED%8C%85/images/14.png)
> 사전 필터 데이터, 데이터를 처리하는 게시판 서비스, 사후 필터를 보여 주는 Logger 출력