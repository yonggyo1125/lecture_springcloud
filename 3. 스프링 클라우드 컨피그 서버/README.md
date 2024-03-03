> 소프트웨어 개발자는 애플리케이션 구성 정보(configuration)와 코드를 분리하는 것이 중요하다는 말을 항상 듣는다. 대부분 이 말은 코드에서 하드코딩된 값을 사용하지 않는 것을 의미한다. 이 원칙을 무시하면 구성 정보가 변경될 때마다 애플리케이션을 재컴파일하고 재배포해야 하므로 애플리케이션이 더 복잡해진다.
> 
> 애플리케이션 코드에서 구성 정보를 완전히 분리하면 개발자와 운영자가 재컴파일 과정을 거치지 않고 구성 정보를 변경할 수 있다. 하지만 개발자에게는 애플리케이션과 함께 관리하고 배포해야 할 또 다른 산출물이 생겨 복잡함도 가중된다.
> 
> 많은 개발자가 프로퍼티 파일(YAML, JSON, XML)을 사용해서 구성 정보를 저장한다. 이러한 파일 안에 애플리케이션 구성을 설정하는 것은 간단한 작업이므로 대부분의 개발자는 구성 파일을 소스 제어(있다면)에 추가하고 애플리케이션의 한 부분으로 배포하는 것 이상을 하지 않는다. 애플리케이션 수가 적은 경우 효과가 있지만 수백 개의 마이크로서비스가 많은 인스턴스를 실행하고 있는 클라우드 기반의 애플리케이션을 처리하고 있다면 이 방식은 문제가 된다. 갑자기 쉽고 간단한 프로세스가 중차대한 문제로 급부상해서 전체 팀은 전체 구성 파일과 씨름해야 한다.
> 
> 예를 들어 수백 개의 마이크로서비스가 있고 각 마이크로서비스에는 세 가지 환경에 대해 서로 다른 환경 구성이 포함되어 있다고 가정해 보자. 이 파일을 외부에서 관리하지 않는다면 변경 사항이 있을 때마다 코드 저장소에서 파일을 검색하고, 코드 저장소에서 검색해서 통합 과정(있다면)을 따라 애플리케이션을 재시작해야 한다. 이러한 재앙적인 상황을 피하려면 클라우드 기반 마이크로서비스 개발을 위한 모범 사례로 다음 사항을 고려해야 한다.

- 배포되는 실제 코드와 구성 정보를 완전히 분리한다.
- 여러 환경에서도 절대 변경되지 않는 불변(immutable) 애플리케이션 이미지를 빌드한다.
- 서버가 시작할 때 마이크로서비스가 읽어 오는 환경 변수 또는 중앙 저장소를 통해 모든 애플리케이션 구성 정보를 주입한다.

# 구성과 복잡성 관리

> 클라우드에서 실행하는 마이크로서비스에서는 마이크로서비스의 인스턴스가 사람의 개입을 최소화하여 신속하게 시작되어야 하므로 애플리케이션 구성을 관리하는 것은 중요하다. 사람이 서비스를 배포하기 위해 수동으로 구성하거나 건드려야 한다면 애플리케이션에서 구성 불일치(configuration drift)나 예기치 않은 장애, 확장 문제 대응을 위한 지연 시간 등이 발생할 수 있다.

## 구성관리 4가지 원칙 

- **분리(segregate)**: 서비스의 물리적 배포에서 서비스 구성 정보를 완전히 분리해야 한다. 실제로 애플리케이션 구성 정보는 서비스 인스턴스와 함께 배포되어서는 안 되며, 시작 중인 서비스에 환경 변수로 전달되거나 서비스가 시작할 때 중앙 저장소에서 읽어 들여야 한다.
- **추상화(abstract)**: 서비스 인터페이스 뒷단에 있는 구성 데이터의 접근 방식을 추상화해야 한다. 애플리케이션 구성 데이터를 조회하는 데 서비스 저장소(파일 기반 또는 JDBC 데이터베이스 기반)에서 직접 읽어 오는 코드를 작성하기보다 REST 기반 JSON 서비스를 사용해야 한다.
- **중앙 집중화(centralize)**: 클라우드 기반의 애플리케이션에는 실제로 수백 개의 서비스가 실행될 수 있어 구성 데이터를 보관하는 데 사용되는 여러 저장소 수를 최소화하는 것이 중요하다. 가능한 적은 수의 저장소로 애플리케이션 구성 정보를 모아야 한다.
- **견고화(harden)**: 애플리케이션 구성 정보는 배포되는 서비스와 완전히 분리되고 중앙 집중화되므로 사용하고 구현할 솔루션은 가용성이 높고 이중화가 필요하다.

## 구성 관리 아키텍처

![image1](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/1.png)

> 마이크로서비스는 시작하면 수명 주기 동안 여러 단계를 거치고, 애플리케이션 구성 데이터는 서비스의 부트스트래핑 단계에서 읽힌다.

![image2](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/2.png)

1. 마이크로서비스 인스턴스가 시작되면 서비스 엔드포인트를 호출하여 동작 중인 환경별 구성 정보를 읽어 온다. 구성 관리 서비스에 대한 접속 정보(접속 자격 증명, 서비스 엔드포인트 등)는 마이크로서비스가 시작할 때 전달된다.
2. 실제 구성 정보는 저장소에 보관된다. 구성 저장소 구현체에 따라 구성 데이터를 보관하는 다양한 방법을 선택할 수 있다. 예를 들어 소스 제어되는 파일, 관계형 데이터베이스, 키-값 데이터 저장소 같은 방법이 있다.
3. 애플리케이션 구성 데이터의 실제 관리는 응용 프로그램이 배포되는 방식과는 독립적으로 한다. 구성 관리에 대한 변경 사항은 일반적으로 빌드 및 배포 파이프라인으로 처리되며, 여기에서 수정 사항에 대한 버전 정보는 태그를 달아 여러 환경(개발, 스테이징, 운영 환경 등)에 배포할 수 있다.
4. 관리하는 구성 정보가 변경되면 애플리케이션 구성 데이터를 사용하는 서비스는 변경 사항을 통지받고 애플리케이션 데이터 복제본을 갱신해야 한다.

# 스프링 클라우드 컨피그 서버 구축

- 스프링 클라우드 컨피그(Spring Cloud Config) 서버는 스프링 부트로 만든 REST 기반의 애플리케이션이다. 독립형 서버로 제공되지 않기 때문에 기존 스프링 부트 애플리케이션에 구성 서버 기능을 추가하거나 새로운 스프링 부트 프로젝트를 만들어 추가해야 한다. 가장 좋은 방법은 분리하는 것
- 컨피그 서버를 구축하기 위해 가장 먼저 할 일은 Spring Initializr(https://start.spring.io/)로 스프링 부트 프로젝트를 생성하는 것이다. 이를 위해 ‘initializr’ 페이지에서 다음 단계를 수행할 것이다. 

![image3](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/3.png)

- Initializr에서 생성한 프로젝트를 내려받아 프로젝트를 임포트 했다면 컨피그 서버 프로젝트의 루트 디렉토리에 다음과 같은 build.gradle 파일을 볼 수 있을 것이다.


> build.gradle

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'org.choonang'
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
    implementation 'org.springframework.cloud:spring-cloud-config-server'
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

> 부트스트랩(bootstrap) 파일은 스프링 클라우드의 특정 파일 타입이며 스프링 애플리케이션 이름, 스프링 클라우드 구성 서버 위치, 암호화/복호화 정보 등을 지정한다. 특히 부트스트랩 파일은 부모 격인 스프링 ApplicationContext로 로드되고, 이 부모 컴포넌트는 application.properties나 application.yml 파일을 사용하는 컴포넌트보다 먼저 로드된다.
>
> src/main/resources/bootstrap.yml

```yaml
spring:
  application:
    name: config-server   # 컨피그 서버 애플리케이션 이름(이 경우 config-server)
server:
  port: 8071  # 서버 포트
```

강조할 부분은 두가지 이다
1. 첫번째는 애플리케이션의 이름, 추후 설명할 서비스 디스커버리(Service Discovery)를 위해 구축할 아키텍처 내 모든 서비스 이름을 지정하는 것이 중요
2. 스프링 컨피그 서버가 요청받은 구성 데이터를 제공할 떄 사용되는 수신 대기 포트

## 스프링 클라우드 컨피그 부트스트랩 클래스 설정

- 스프링 클라우드 컨피그 서비스를 생성하는 다음 단계는 부트스트랩 클래스를 설정하는 것
- 모든 스프링 클라우드 서비스는 시작하는데 사용되는 부트스트랩 클래스가 필요하다.

> src/main/java/org/choonang/configserver/ConfigserverApplication.java

```java
package org.choonang.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication  // 이 컨피그 서비스는 스프링 부트 애플리케이션이므로 @SpringBootApplication 애너테이션을 추가해야 한다.
@EnableConfigServer // @EnableConfigServer 애너테이션은 이 서비스를 스프링 클라우드 컨피그 서비스로 활성화한다. 
public class ConfigserverApplication {

	public static void main(String[] args) { // main() 메서드는 이 서비스와 스프링 컨테이너를 시작한다.
		SpringApplication.run(ConfigserverApplication.class, args);
	}

}
```

## 스프링 클라우드 컨피그 서버에 파일 시스템 사용

> 스프링 클라우드 컨피그 서버는 bootstrap.yml 파일에서 애플리케이션의 구성 데이터를 보관할 저장소를 지정한다. 파일 시스템 기반의 저장소를 설정하는 것이 가장 쉽다. 이제 부트스트랩 파일(bootstrap.yml 또는 bootstrap.properties)을 변경해 보자. 다음 코드에서 파일 시스템 저장소를 설정하는 데 필요한 내용을 볼 수 있다.

> src/resources/bootstrap.yml

```yaml
spring:
  application:
    name: config-server
  profiles:
    active: native # 백앤드 저장소(파일 시스템)와 관련된 스프링 프로파일을 설정한다.

  cloud:
    config:
      server:
        native:
          search-locations: file:///D:/configs # 구성파일이 저장된 검색 위치를 설정한ㄷ나.
          
server:
  port: 8071
```

- 애플리케이션 구성 정보를 저장하고자 파일 시스템을 사용하므로 스프링 클라우드 컨피그 서버가 native 프로파일로 실행되도록 지정해야 한다.  
- 스프링 프로파일을 사용하여 dev, test, staging, production, native 등 다양한 환경에 빈을 매핑할 수 있다.
- 상기 예에서는 파일 시스템 위치(file:///D:/configs)를 사용했지만, 다음 코드 처럼 특정 클래스패스(classpath)를 지정할 수도 있다. 

```yaml
spring:
  application:
    name: config-server
  profiles:
    active: native

  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config

server:
  port: 8071
  
```

> classpath 속성을 사용하면 스프링 클라우드 컨피그 서버는 src/main/resources/config 폴더를 찾는다.


## 서비스의 구성 파일 설정

- 스프링 클라우드 컨피그는 모든 것이 계층 구조로 동작한다. 애플리케이션 구성 정보는 애플리케이션 이름과 환경별로 구성할 프로퍼티를 표시한다. 각 환경에서 다음과 같은 구성 프로파일을 설정한다.
- 구성 서비스를 구축하는 것은 여러분 환경에서 실행되는 마이크로서비스 하나가 추가된다는 의미다. 설정이 완료되면 HTTP 기반 REST 엔드포인트로 서비스 콘텐츠에 액세스할 수 있다.
- 애플리케이션 구성 파일의 명명 규약은 appname-env.properties 또는 appname-env.yml 파일이다.
- 환경 이름은 구성 정보를 찾기 위해 액세스할 URL로 직접 변환된다. 나중에 라이선싱 마이크로서비스를 시작할 때 실행하려는 환경은 서비스를 구동하면 명령줄에서 전달하는 스프링 부트 프로파일로 지정된다. 프로파일이 명령줄로 전달되지 않으면 스프링 부트는 기본적으로 애플리케이션과 패키지된 application.properties에 포함된 구성 데이터를 사용한다.


> src/resources/config/member-service.yml

```yaml
spring:
  datasource:
    driverClassName: oracle.jdbc.driver.OracleDriver

  # JPA 설정
  jpa:
    properties:
      hibernate:
        show_sql: true
        format_sql: true
        use_sql_comments: true
    hibernate:
      ddlAuto: create

logging:
  level:
    org:
      hibernate:
        type:
          descriptor:
            sql: trace
```

> src/resources/config/member-service-dev.yml

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:orcl
    username: 사용자명
    password: 비밀번호
```

- 구성 서버를 시작할 수 있는 작업을 완료했으니 main() 메서드를 실행하여 시작해봅시다.
- 웹 브라우저 주소창에 http://localhost:8071/member-service/default 를 입력하면 member-service.yml 파일에 포함된 모든 속성과 함께 반환된 JSON으로 표시된다.

![image4](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/4.png)
> 스프링 클라우드 컨피그는 환경별 프로퍼티를 HTTP 기반의 엔드포인트로 제공한다.


- dev 프로파일의 회원 서비스에 대한 구성 정보를 보려면 GET http://localhost:8071/member-service/dev 엔드포인트를 호출하면 된다.

![image5](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/5.png)
> dev 프로파일에 대한 회원 서비스의 구성 정보 조회

- dev 엔드포인트를 호출할 때 스프링 클라우드 컨피그 서버는 default와 dev의 구성 프로퍼티를 모두 반환했다는 것을 알 수 있다. 스프링 클라우드 컨피그가 두 구성 정보를 모두 반환하는 이유는 스프링 프레임워크가 문제를 해결하는 데 계층적 메커니즘을 수행하기 때문이다.
-  스프링 프레임워크가 이 일을 수행할 때 default 프로파일에서 정의된 프로퍼티를 먼저 찾은 후 특정 환경에 값이 있다면 그 값으로 default 값을 교체한다.


# 스프링 클라우드 컨피그와 스프링 부트 클라이언트 통합


# 중요한 구성정보 보호 