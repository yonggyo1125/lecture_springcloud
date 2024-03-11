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



---

## 사전 필터 만들기

---

## 서비스에서 상관관계 ID 사용

---

## 상관관계 ID를 수신하는 사후 필터 작성 

