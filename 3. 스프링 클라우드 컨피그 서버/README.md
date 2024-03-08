# 스프링 클라우드 컨피그 서버

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
    driverClassName: oracle.jdbc.driver.OracleDriver
    url: jdbc:oracle:thin:@localhost:1521:orcl
    username: 사용자명
    password: 비밀번호
```

> src/resources/config/member-service-test.yml

```yaml
spring:
  datasource:
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:test
    username: sa
    password:
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

- member-service는 처음 시작할 때 세 가지 정보, 즉 스프링 프로파일, 애플리케이션 이름, 스프링 클라우드 컨피그 서비스와 통신하는 데 사용할 엔드포인트를 전달받는다. 스프링 프로파일은 스프링 서비스에 대한 프로퍼티 환경에 매핑된다.

![image6](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/6.png)

- member-service는 부팅하면 전달된 스프링 프로파일에 설정된 엔드포인트로 스프링 클라우드 컨피그 서비스와 통신한다. 그런 다음 스프링 클라우드 컨피그 서비스는 구성된 백엔드 저장소(파일 시스템, 깃, 볼트)를 사용하여 URI에 매개변수로 전달된 스프링 프로파일에 해당하는 구성 정보를 검색한다. 적절한 프로퍼티 값이 라이선싱 서비스로 다시 전달되면 스프링 부트 프레임워크는 이 값을 애플리케이션의 적절한 부분에 주입한다.


## spring initializr에서 member-service 프로젝트 생성 

![image7](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/7.png)

> build.gradle 
> 
> spring-cloud-starter-bootstrap 추가

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

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}

repositories {
	mavenCentral()
}

ext {
	set('springCloudVersion', "2023.0.0")
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.cloud:spring-cloud-starter-config'
    implementation 'org.springframework.cloud:spring-cloud-starter-bootstrap'
    compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'com.oracle.database.jdbc:ojdbc11'
	annotationProcessor 'org.projectlombok:lombok'
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

## 스프링 클라우드 컨피그 사용을 위한 라이선싱 서비스 구성

- 그래들 의존성을 정의했다면 라이선싱 서비스에 스프링 클라우드 컨피그 서버 위치를 알려 주어야 한다. 스프링 클라우드 컨피그를 사용하는 스프링 부트 서비스는 bootstrap.yml, bootstrap.properties, application.yml, application.properties 파일 중 한곳에서 구성 정보를 설정할 수 있다.

- 앞서 언급했듯이 bootstrap.yml 파일은 다른 구성 정보보다 먼저 애플리케이션 프로퍼티를 읽는다. 일반적으로 bootstrap.yml 파일에 서비스에 대한 애플리케이션 이름, 애플리케이션 프로파일, 구성 서버에 연결할 URI를 넣는다. 스프링 클라우드 컨피그 서버에 저장되지 않고 로컬에 유지하려는 서비스의 다른 구성 정보는 로컬의 application.yml 파일에서 설정할 수 있다.

- 앞서 언급했듯이 bootstrap.yml 파일은 다른 구성 정보보다 먼저 애플리케이션 프로퍼티를 읽는다. 일반적으로 bootstrap.yml 파일에 서비스에 대한 애플리케이션 이름, 애플리케이션 프로파일, 구성 서버에 연결할 URI를 넣는다. 스프링 클라우드 컨피그 서버에 저장되지 않고 로컬에 유지하려는 서비스의 다른 구성 정보는 로컬의 application.yml 파일에서 설정할 수 있다.

- member-service가 스프링 클라우드 컨피그 서비스와 통신하게 하려면 이러한 매개변수를 member-service의 bootstrap.yml이나 docker-compose.yml 파일 또는 서비스를 시작할 때 JVM 매개변수를 사용하여 정의한다. 다음 코드는 bootstrap.yml 파일을 사용할 때 이 파일의 내용을 보여 준다.

> src/resources/bootstrap.yml

```yaml
spring:
  application:
    name: member-service  # 이름을 지정하면 스프링 클라우드 컨피그 클라이언트는 어떤 서비스가 검색되는지 알 수 있다.
  profiles:
    active: dev # 서비스가 실행될 프로파일을 지정한다. 프로파일은 환경에 매핑된다.
  cloud:
    config:
      uri: http://localhost:8071 # 스프링 클라우드 컨피그 서버의 위치를 지정한다.
```

- <code>spring.application.name</code>은 애플리케이션 이름(예 member-service)이며, 스프링 클라우드 컨피그 서버 내 config 디렉터리 이름과 직접적으로 매핑되어야 한다
- <code>spring.profiles.active</code> 프로퍼티는 스프링 부트에 애플리케이션이 실행할 프로파일을 지정한다. 프로파일(profile)은 스프링 부트 애플리케이션에서 사용될 구성 데이터를 구분하는 메커니즘이다. member-service 프로파일은 클라우드 구성 환경에서 서비스가 직접 매핑될 환경을 지원한다. 예를 들어 dev 프로파일을 전달하면 컨피그 서버는 dev 프로퍼티를 사용하고, 프로파일을 설정하지 않으면 라이선싱 서비스는 default 프로파일을 사용한다.
- <code>spring.cloud.config.uri</code>는 라이선싱 서비스가 컨피그 서버 엔드포인트를 찾을 위치다.

- 프로퍼티 없이 이 명령을 실행하면 member-service 서버는 자동으로 bootstrap.yml 파일에 기정의된 엔드포인트(이 경우 http://localhost:8071)와 활성화된 프로파일(dev)을 사용하여 스프링 클라우드 컨피그 서버에 연결을 시도한다.

- default 값을 재정의하고 다른 환경을 지정하려면 라이선싱 서비스 프로젝트를 JAR 파일로 컴파일한 후 D 시스템 프로퍼티 오버라이드를 사용하여 JAR를 실행하면 된다. 다음 명령줄 호출은 JVM 매개변수를 사용하여 라이선싱 서비스를 시작하는 방법을 보여 준다.

```
java -Dspring.cloud.config.uri=http://localhost:8071 \ 
     -Dspring.profiles.active=dev \
     -jar build/libs/member-service-0.0.1-SNAPSHOT.jar
```

- 이 예는 스프링 프로퍼티를 명령줄로 재정의하는 방법을 보여 준다. 즉, 명령줄에서 다음 두 매개변수를 재정의한다.

```
spring.cloud.config.uri 
spring.profiles.active
```

- 도커를 사용하면 모든 서비스 시작을 통제하는 환경별 도커 컴포즈 파일로 다양한 환경을 시뮬레이션할 수 있다. 컨테이너에 필요한 환경별 값들은 컨테이너의 환경 변수로 전달된다. 예를 들어 개발 환경에서 라이선싱 서비스를 시작하려면 docker/docker-compose.yml 파일에 다음 코드에 표시된 항목이 포함된다.

> configserver 에서 <code>gradlew bootBuildImage</code>를 사용하여 docker image를 생성한다.
> member-service에서 <code>gradlew bootBuildImage</code>를 사용하여 docker image를 생성한다.
>
>
> docker/docker-compose.yml

```yaml
version: '3.8'
services:
  configserver:
    image: configserver:0.0.1-SNAPSHOT
    ports:
      - "8071:8071"
  memberservice:
    image: member:0.0.1-SNAPSHOT
    environment:  # member-service 컨테이너를 위한 환경 변수를 지정한다.
      SPRING_PROFILE_ACTIVE: "dev" # SPRING_PROFILES_ACTIVE 환경 변수를 스프링 부트 서비스 명령줄로 전달하고 스프링 부트에 실행할 프로파일을 알려준다.
      SPRING_CLOUD_CONFIG_URI: http://localhost:8071  # 컨피그 서비스의 엔드포인트
```

- YML 파일의 환경 항목은 두 변수 값인 SPRING_PROFILES_ACTIVE(member-service가 실행될 스프링 부트 프로파일)와 SPRING_CLOUD_CONFIG_URI(member-service에 전달되어 구성 데이터를 읽어 올 스프링 클라우드 구성 서버의 인스턴스를 정의)를 포함한다. 도커 컴포즈 파일을 설정한 후 도커 컴포즈 파일이 있는 곳에서 다음 명령만 실행하면 서비스들을 시작할 수 있다.

```
docker-compose up
```

## 스프링 클라우드 컨피그 서버를 사용하여 데이터 소스 연결 

> QueryDSL 의존성 추가 

```groovy
...

dependencies {
    
    ...
    
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
	implementation 'com.querydsl:querydsl-jpa:5.1.0:jakarta'
	annotationProcessor 'com.querydsl:querydsl-apt:5.1.0:jakarta'
	annotationProcessor 'jakarta.persistence:jakarta.persistence-api:3.1.0'
	annotationProcessor 'jakarta.annotation:jakarta.annotation-api:2.1.1'
    
    ...
    
    testImplementation 'com.h2database:h2'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
	...
}

...


def querydslDir = "$buildDir/generated/querydsl"

sourceSets {
    main.java.srcDirs += [ querydslDir ]
}

tasks.withType(JavaCompile) {
    options.getGeneratedSourceOutputDirectory().set(file(querydslDir))
}

clean.doLast {
    file(querydslDir).deleteDir()
}
```

> src/java/.../member/config/SecurityConfig.java

```java
package org.choongang.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable());
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

> src/java/.../member/entity/Base.java

```java
package org.choongang.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter @Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Base {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime modifiedAt;
}
```

> src/java/.../member/entity/Member.java

```java
package org.choongang.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Builder
@Entity
@NoArgsConstructor @AllArgsConstructor
public class Member extends Base {
    @Id
    @GeneratedValue
    private Long seq;

    @Column(length=65, unique = true)
    private String email;

    @Column(length=65)
    private String password;

    @Column(length=65)
    private String confirmPassword;

    @Column(length=45)
    private String name;
}
```

> src/java/.../member/repository/MemberRepository.java

```java
package org.choongang.member.repository;

import org.choongang.member.entity.Member;
import org.choongang.member.entity.QMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MemberRepository extends JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member> {
    default boolean exists(String email) {
        return exists(QMember.member.email.eq(email));
    }
}
```

> src/java/.../member/controller/RequestJoin.java

```java
package org.choongang.member.controller;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestJoin {
    @NotBlank @Email
    private String email;

    @NotBlank
    @Size(min=8)
    private String password;

    @NotBlank
    private String confirmPassword;

    @NotBlank
    private String name;

    @AssertTrue
    private boolean agree;
}
```

> resources/messages/commons.properties
> resources/messages/errors.properties
> resources/messages/validations.properties 파일 추가 
>
> src/java/.../member/config/BeanConfig.java

```java
package org.choongang.member.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class BeanConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasenames("messages.commons", "messages.validations", "messages.errors");

        return ms;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());

        return om;
    }
}
```

> resources/messages/validations.properties

```properties
NotBlank=필수입력항목
Email=이메일을 입력하세요.
NotBlank.email=이메일을 입력하세요.
NotBlank.password=비밀번호를 입력하세요.
NotBlank.confirmPassword=비밀번호를 확인하세요.
NotBlank.requestJoin.name=회원명을 입력하세요.
Size.requestJoin.password=비밀번호는 8자리 이상 입력하세요.
AssertTrue.requestJoin.agree=회원가입 약관에 동의하세요.
Duplicated.requestJoin.email=이미 가입된 이메일입니다.
Mismatch.requestJoin.confirmPassword=비밀번호가 일치하지 않습니다.
```

> src/java/.../common/Utils.java

```java
package org.choongang.member.common;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Utils {
    private final MessageSource messageSource;


    public Map<String, List<String>> getErrorMessages(Errors errors) {
        try {
            Map<String, List<String>> messages = errors.getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(FieldError::getField, e -> _getErrorMessages(e.getCodes()), (m1, m2) -> m2));


            List<String> gMessages = errors.getGlobalErrors()
                    .stream()
                    .map(o -> {
                        try {
                            String message = messageSource.getMessage(o.getCode(), null, null);
                            return message;
                        } catch (Exception e) {
                            return "";
                        }
                    }).filter(s -> !s.isBlank()).toList();

            messages.put("global", gMessages);
            return messages;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private List<String> _getErrorMessages(String[] codes) {
        List<String> messages = Arrays.stream(codes)
                .map(c -> {
                    try {
                        String message = messageSource.getMessage(c, null, null);
                        return message;
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(s -> !s.isBlank()).toList();

        return messages;
    }
}
```

> src/java/.../member/rests/JSONData.java 

```java
package org.choongang.member.common.rests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class JSONData {
    private boolean success = true;
    private HttpStatus status = HttpStatus.OK;
    private Object messages;
    @NonNull
    private Object data;
}
```

> src/java/.../member/common/exceptions/CommonException.java

```java
package org.choongang.member.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;

public class CommonException extends RuntimeException {

    private HttpStatus status;
    private Errors errors;

    private boolean messageCode;

    public CommonException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public CommonException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public CommonException(Errors errors, HttpStatus status) {
        this.status = status;
        this.errors = errors;

    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setMessageCode(boolean messageCode) {
        this.messageCode = messageCode;
    }

    public boolean isMessageCode() {
        return messageCode;
    }
}
```

> src/java/.../member/common/exceptions/BadRequestException.java

```java
package org.choongang.member.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;

public class BadRequestException extends CommonException{

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String code, boolean messageCode) {
        this(code);
        setMessageCode(messageCode);
    }

    public BadRequestException(Errors errors) {
        super(errors, HttpStatus.BAD_REQUEST);
    }
}
```

> src/java/.../member/controller/JoinValidator.java

```java
package org.choongang.member.service;

import lombok.RequiredArgsConstructor;
import org.choongang.member.controller.RequestJoin;
import org.choongang.member.repository.MemberRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class JoinValidator implements Validator {

    private final MemberRepository memberRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestJoin.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RequestJoin form = (RequestJoin)target;
        String email = form.getEmail();
        String password = form.getPassword();
        String confirmPassword = form.getConfirmPassword();

        if (StringUtils.hasText(email) && memberRepository.exists(email)) {
            errors.rejectValue("email", "Duplicated");
        }

        if (StringUtils.hasText(password) && StringUtils.hasText(confirmPassword) && !password.equals(confirmPassword)) {
            errors.rejectValue("confirmPassword", "Duplicated");
        }
    }
}
```

> src/java/.../member/service/JoinService.java

```java
package org.choongang.member.service;

import lombok.RequiredArgsConstructor;
import org.choongang.member.controller.RequestJoin;
import org.choongang.member.entity.Member;
import org.choongang.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;

    public void join(RequestJoin form) {
        String hash = encoder.encode(form.getPassword());
        Member member = Member.builder()
                .email(form.getEmail())
                .password(hash)
                .name(form.getName())
                .build();

        memberRepository.saveAndFlush(member);
    }
}
```

> src/java/.../member/controller/MemberController.java

```java
package org.choongang.member.controller;

import lombok.RequiredArgsConstructor;
import org.choongang.member.common.exceptions.BadRequestException;
import org.choongang.member.service.JoinService;
import org.choongang.member.service.JoinValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {
    private final JoinService joinService;
    private final JoinValidator joinValidator;

    /**
     * 회원 가입 처리
     * @param form
     * @param errors
     * @return
     */
    @PostMapping
    public ResponseEntity<Object> join(RequestJoin form, Errors errors) {
        joinValidator.validate(form, errors);

        errorProcess(errors);

        joinService.join(form);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private void errorProcess(Errors errors) {
        if (errors.hasErrors()) {
            throw new BadRequestException(errors);
        }
    }
}
```

> src/java/.../member/controller/MemberController.java

```java
package org.choongang.member.controller;

import lombok.RequiredArgsConstructor;
import org.choongang.member.common.exceptions.BadRequestException;
import org.choongang.member.service.JoinService;
import org.choongang.member.service.JoinValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {
    private final JoinService joinService;
    private final JoinValidator joinValidator;

    /**
     * 회원 가입 처리
     * @param form
     * @param errors
     * @return
     */
    @PostMapping
    public ResponseEntity<Object> join(@Valid @RequestBody RequestJoin form, Errors errors) {
        joinValidator.validate(form, errors);

        errorProcess(errors);

        joinService.join(form);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private void errorProcess(Errors errors) {
        if (errors.hasErrors()) {
            throw new BadRequestException(errors);
        }
    }
}
```

> 회원 가입 기능이 완성되었으므로 통합테스트를 통한 기능 동작 여부를 체크 
> src/test/.../member/controller/MemberControllerTest.java

```java
package org.choongang.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.choongang.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.profiles.active=test")
public class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("[통합]회원가입 테스트")
    void joinTest() throws Exception {
        RequestJoin form = new RequestJoin();
        form.setEmail("user01@test.org");
        form.setName("사용자01");
        form.setPassword("_aA123456");
        form.setConfirmPassword(form.getPassword());
        form.setAgree(true);
        String params = objectMapper.writeValueAsString(form);

        mockMvc.perform(post("/api/v1/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(params)).andDo(print())
                .andExpect(status().isCreated());

        assertTrue(memberRepository.exists(form.getEmail()));
    }
}
```

![image8](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/8.png)

## 스프링 클라우드 컨피그 서버를 사용하여 프로퍼티 갱신

- 스프링 클라우드 컨피그 서버를 사용하려고 할 때 개발 팀에서 가장 먼저 제기하는 질문 중 하나는 ‘프로퍼티가 변경될 때 어떻게 동적으로 애플리케이션을 갱신하는가’이다. 안심하라. 컨피그 서버는 항상 최신 프로퍼티 버전을 제공한다. 내부 저장소에서 프로퍼티가 변경되면 항상 최신 상태로 유지된다.

- 하지만 스프링 부트 애플리케이션은 시작할 때만 프로퍼티를 읽기 때문에 컨피그 서버에서 변경된 프로퍼티가 자동으로 애플리케이션에 적용되지는 않는다. 그러나 개발 팀은 스프링 부트 액추에이터(Spring Boot Actuator)의 <code>@RefreshScope</code> 애너테이션을 사용하여 스프링 애플리케이션이 구성 정보를 다시 읽게 만드는 /refresh 엔드포인트에 접근할 수 있다.


> src/java/.../member/MemberServiceApplication.java 

```java 
package org.choongang.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@SpringBootApplication
@RefreshScope
public class MemberServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MemberServiceApplication.class, args);
	}

}
```

- <code>@RefreshScope</code> 애너테이션에는 몇 가지 유의할 사항이 있다. 이 애너테이션은 애플리케이션 구성에 있는 사용자가 정의한 스프링 프로퍼티만 다시 로드한다. 즉, 스프링 데이터에 사용되는 데이터베이스 구성 정보와 같은 항목은 이 애너테이션으로 갱신되지 않는다.

### 마이크로서비스 갱신

> 마이크로서비스와 함께 스프링 클라우드 컨피그 서비스를 사용할 때 프로퍼티를 동적으로 변경하기 전에 먼저 고려해야 할 사항은 동일한 서비스에 대한 여러 인스턴스가 실행 중일 수 있다는 것이다. 서비스의 모든 인스턴스를 새로운 애플리케이션 구성으로 갱신해야 한다. 이러한 문제를 해결할 몇 가지 방법이 있다.
> 
> 스프링 클라우드 컨피그 서비스는 변경이 발생할 때 이 서비스를 사용하는 모든 클라이언트에 게시(publish)할 수 있는 스프링 클라우드 버스(Spring Cloud Bus)라는 푸시(push) 기반의 메커니즘을 제공한다. 이 스프링 클라우드 버스에는 별도 미들웨어인 RabbitMQ가 필요하다. 이 방법은 변경을 감지하는 매우 유용한 수단이지만 모든 스프링 클라우드 컨피그 백엔드(예를 들어 콘술(Consul))가 이 푸시 메커니즘을 지원하는 것은 아니다. 다음 장에서 스프링 클라우드 서비스 디스커버리와 유레카(Eureka)를 사용하여 서비스의 모든 인스턴스를 등록할 것이다.
>
> 애플리케이션 구성 갱신 이벤트를 처리하는 기술 중 하나는 스프링 클라우드 컨피그에서 애플리케이션 프로퍼티를 새로 고치는 것이다. 그런 다음 서비스 디스커버리 엔진에 질의하여 서비스의 모든 인스턴스를 찾고 /refresh 엔드포인트를 직접 호출하는 간단한 스크립트를 작성한다.
>
> 물론 모든 서버를 재시작하여 새로운 프로퍼티를 가져올 수도 있다. 이 방법은 특히 도커 같은 컨테이너에서 서비스를 운영하고 있다면 손쉽게 할 수 있는 일이다. 도커 컨테이너는 수초 만에 재시작되고 애플리케이션 구성을 강제로 다시 읽게 한다.
> 
> 클라우드 기반 서버는 일시적(ephemeral)이라는 것을 기억하라. 새로운 구성으로 새 인스턴스를 시작한 후 이전 서비스를 해체하는 것을 두려워하지 마라.

## 깃과 함게 스프링 클라우드 컨피그 서버 사용

- 파일 시스템을 스프링 클라우드 컨피그 서버의 백엔드 저장소로 사용하는 것은 클라우드 기반 애플리케이션에 실용적이지 못하다. 개발 팀이 컨피그 서버의 모든 인스턴스에 마운트되는 공유 파일 시스템을 설정하고 관리해야 하며, 컨피그 서버는 애플리케이션 구성 프로퍼티를 호스팅하는 데 다양한 백엔드 저장소와 통합될 수 있기 때문이다.
- 다른 한 가지 방법은 깃 소스 제어 저장소와 함께 스프링 클라우드 컨피그 서버를 사용한 것이다. 깃을 사용하면 구성 관리할 프로퍼티를 소스 제어하에 두는 모든 이점을 얻을 수 있고, 프로퍼티 관리 파일의 배포를 빌드 및 배포 파이프라인에 쉽게 통합할 수 있다. 깃을 사용하려면 스프링 클라우드 컨피그 서비스의 <code>bootstrap.yml</code> 파일에 구성을 추가해야 하는데 다음 코드를 보면 알 수 있다.

> 스프링 클라우드 컨피그 서버 : src/resources/bootstrap.yml 
 
```yaml
spring:
  application:
    name: config-server
  profiles:
    active:
      - native,git # 쉼표로 분리된 프로파일을 모두 매핑한다.

  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config
        git: # 스프링 클라우드 컨피그에 깃을 백앤드 저장소로 사용하라고 지시한다.
          uri: https://github.com/yonggyo1125/lecture_springclound_config.git # 스프링 클라우드 컨피그에 깃 서버와 레포지토리 URL을 알린다.
          searchPaths: member-service # 스프링 클라우드 컨피그에 구성 파일을 찾는 데 사용될 깃 경로를 알린다.
          default-label: master

server:
  port: 8071
```

- 주요 네 가지 구성 프로퍼티는 다음과 같다.
    - spring.profiles.active
    - spring.cloud.config.server.git
    - spring.cloud.config.server.git.uri
    - spring.cloud.config.server.git.searchPaths

- **spring.profiles.active** 프로퍼티는 스프링 컨피그 서비스에 대한 active 프로파일을 모두 설정한다. 쉼표(,)로 구분된 프로파일 목록은 스프링 부트 애플리케이션과 같은 우선순위 규칙을 갖는다. 즉, active 프로파일들은 default 프로파일보다 우선하고 마지막 프로파일이 최우선한다.
- **spring.cloud.config.server.git** 프로퍼티는 스프링 클라우드 컨피그 서버에 파일 시스템이 아닌 백엔드 저장소를 사용하도록 지시한다.

> 깃허브 사용 권한이 필요하다면 사용자 이름이나 패스워드(또는 개인 토큰이나 SSH 구성 정보) 등 깃 구성 정보를 구성 서버의 bootstrap.yml 파일에 설정해야 한다.

- **spring.cloud.config.server.git.uri** 프로퍼티는 연결하려는 깃 리포지터리 URL을 제공한다.
- **spring.cloud.config.server.git.searchPaths** 프로퍼티는 클라우드 컨피그 서버가 부팅될 때 검색될 깃 리포지터리의 상대 경로를 스프링 컨피그에 지정한다. 파일 시스템 구성 정보처럼 spring.cloud.config.server.git.searchPaths 값은 구성 서비스에서 호스팅되는 각 서비스를 쉼표(,)로 구분해서 표기한다.

> 스프링 클라우드 컨피그에서 환경 저장소의 기본 구현체는 깃 백엔드다.

## 볼트와 스프링 클라우드 컨피그 서비스 통합 

- 사용 가능한 또 다른 백엔드 저장소로 하시코프 볼트(HashiCorp Vault)가 있다. 볼트는 시크릿(secrets)에 안전하게 접근할 수 있는 도구이며 패스워드, 인증서, API 키 등 접근을 제한하거나 제한하려는 어떤 정보로도 시크릿을 정의 할 수 있다.

### 볼트 설치 및 실행 

- hashicorp/vault 도커 이미지 설치

```
docker pull hashicorp/vault
```

- 볼트 컨테이너 생성(개발 모드)

```
docker run --cap-add=IPC_LOCK -p 8200:8200 -e 'VAULT_DEV_ROOT_TOKEN_ID=myroot' -e 'VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200' hashicorp/vault
```

- docker run 명령은 다음 두 가지 매개변수를 입력받는다.
  - **VAULT_DEV_ROOT_TOKEN_ID**: 이 매개변수는 생성된 루트 토큰(root token) ID를 설정한다. 루트 토큰은 볼트 구성을 시작하는 초기 액세스 토큰이다. 초기 생성된 토큰 ID를 주어진 값으로 설정한다.
  - **VAULT_DEV_LISTEN_ADDRESS**: 이 매개변수는 개발 서버의 IP 주소와 포트를 설정한다. 기본값은 0.0.0.0:8200이다.
- 서버를 실행하면 Root Token을 다음과 같이 확인할 수 있는데, 로그인할 때 필요하므로 따로 메모해 놓는다.

![image9](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/9.png)

## 볼트 UI

- 볼트는 시크릿 생성 과정을 도와주는 통합 인터페이스를 제공한다. 이 UI를 사용하려면 http://0.0.0.0:8200/ui/vault/auth에 접속한다. 이 URL은 docker run 명령에서 VAULT_DEV_LISTEN_ADDRESS 매개변수로 설정되었다. 그림 5-10에서 볼트 UI의 로그인 페이지를 볼 수 있다.

- 최초 로그인 시에는 Method에는 Token을 Token은 터미널에 노출되고 있는 Root Token을 입력합니다.

![image10](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/10.png)

- 다음은 시크릿 생성 단계다. 시크릿을 생성하려면 로그인한 후 대시보드의 Secrets 탭을 클릭하자. 이 예에서는 설정한 후 member-service라는 시크릿을 만들고 member.vault.property라는 프로퍼티에 choongang 값을 설정한다. 이 정보는 암호화되며 접근은 제한된다는 것을 기억하자. 이를 위해 우선 새로운 시크릿 엔진을 생성한 후 특정 시크릿을 엔진에 추가해야 한다. 

- 새 엔진 생성

![image11](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/11.png)

- 범용 KV 선택

![image12](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/12.png)

![image13](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/13.png)

- Path에는 스프링 애플리케이션의 이름을 입력 합니다.(예: member-service)

- Create secret 버튼을 클릭

![image14](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/14.png)

- member.vault.property라는 프로퍼티에 choongang 값을 설정한다.

![image15](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/15.png)

- 볼트와 시크릿 구성을 완료했으니 볼트와 통신할 수 있는 스프링 클라우드 컨피그 서버를 구성하자. 이를 위해 컨피그 서버의 bootstrap.yml 파일에 볼트 프로파일을 추가한다.

> 스프링 클라우드 컨피그 서버 : src/resources/bootstrap.yml

```yaml
spring:
  application:
    name: config-server
  profiles:
    active:
      - vault
    #  - native,git # 쉼표로 분리된 프로파일을 모두 매핑한다.

  cloud:
    config:
      server:
        vault:    # 스프링 클라우드 컨피그에 저장소로 볼트를 사용하도록 지시한다.
          port: 8200   # 스프링 클라우드 컨피그에 볼트 포트를 지정한다.
          host: 127.0.0.1    # 스프링 클라우드 컨피그에 볼트 호스트를 지정한다.
          kvVersion: 2   # KV 시크릿 엔진 버전을 설정한다.
          backend: member-service
          profile-separator: /
          
  ...

```

- 여기에서 중요한 것은 KV 시크릿 엔진 버전이다. spring.cloud.config.server.vault.kvVersion 기본값은 1이다. 하지만 볼트 0.10.0 이상 버전을 사용한다면 버전 2 사용을 권장한다.

- 이제 모두 설정했으니 HTTP 요청으로 컨피그 서버를 테스트해 보자. 여기에서는 cURL 명령이나 포스트맨 같은 REST 클라이언트를 사용할 수 있다.

```
curl -X "GET" "http://localhost:8071/member-service/default" -H "X-Config-Token: 발급받은 root 토큰(터미널에서 확인)"
```

- 모두 성공적으로 구성되었다면 다음과 같은 응답이 반환된다.

```
{"name":"member-service","profiles":["default"],"label":null,"version":null,"state":null,"propertySources":[{"name":"vault:member-service","source":{"member.vault.property":"choongang"}}]}
```

# 중요한 구성정보 보호 

- 기본적으로 스프링 클라우드 컨피그 서버는 애플리케이션 구성 파일 안의 모든 프로퍼티를 평문으로 저장한다. 여기에는 데이터베이스 자격 증명 등 중요한 정보도 포함되어 있다. 중요한 자격 증명을 평문으로 소스 코드 저장소에 저장하는 것은 매우 나쁜 관행이다. 불행하게도 이 경우는 생각보다 훨씬 더 자주 발생한다.

- 스프링 클라우드 컨피그는 중요한 프로퍼티를 쉽게 암호화할 수 있는 기능을 제공하며, 대칭(공유 시크릿) 및 비대칭 암호화(공개/비공개) 키 사용을 지원한다. 비대칭 암호화는 현대적이고 더 복잡한 알고리즘을 사용하기 때문에 대칭 암호화보다 더 안전하다. 하지만 컨피그 서버의 <code>bootstrap.yml</code> 파일에 한 개의 프로퍼티만 정의하면 되므로 대칭 키를 사용하는 것이 더 편리할 때가 있다.

## 대칭 암호화 키 설정 

- 대칭 암호화 키는 암호 생성자가 값을 암호화하고 암호 해독자가 해독하는 데 사용되는 공유 시크릿에 불과하다. 스프링 클라우드 컨피그 서버에서 대칭 암호화 키는 <code>bootstrap.yml</code> 파일에서 설정하거나 <code>ENCRYPT_KEY</code>라는 OS 환경 변수로 서비스에 전달되는 문자열이다.

> 대칭 키의 길이는 12문자 이상이 되어야 하고 불규칙 문자열이 이상적이다.
> 문자열은 노출되지 않도록 환경 변수 형태로 등록한다.


```yaml
spring:
  application:
    name: config-server
  profiles:
    active:
      - native
     # - vault
     #  - native,git # 쉼표로 분리된 프로파일을 모두 매핑한다.

 ...

server:
  port: 8071

encrypt:
  key: ${secretKey}  # 컨피그 서버는 이 환경변수로 입력되는 값을 대칭 키로 사용한다.
```

- 환경 변수 설정 

![image16](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/16.png)

![image17](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/17.png)

![image18](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/3.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%BB%A8%ED%94%BC%EA%B7%B8%20%EC%84%9C%EB%B2%84/images/18.png)

## 프로퍼티 암호화와 복호화

- 이제 스프링 클라우드 컨피그와 함께 사용될 프로퍼티의 암호화를 시작할 준비가 되었다.
- 데이터에 액세스하는 데 사용하는 member-service의 Oracle 데이터베이스 패스워드를 암호화한다. spring.datasource.password라는 프로터티 값은 현재 평문으로 되어 있다.
- 스프링 클라우드 컨피그 인스턴스를 실행하면 스프링 클라우드 컨피그(프레임워크)는 ENCRYPT_KEY 환경 변수 또는 bootstrap 파일의 프로퍼티가 설정을 감지하고 /encrypt와 /decrypt 두 개의 엔드포인트를 스프링 클라우드 컨피그 서비스에 자동으로 추가한다.

```
curl -X POST http://localhost:8071/encrypt  -d "비밀번호"
```

- 생성예시

```
4b93b6c7060f269410654622559659db1e9049bd829a15521df43b825e6a860d
```

- 결괏값을 복호화하려면 /decrypt 엔드포인트에 암호화된 문자열을 전달해서 복호화한다.
- 다음 구문(syntax)을 사용하여 라이선싱 서비스에 대한 암호화된 프로퍼티를 깃허브나 파일 시스템 기반 구성 파일에 추가할 수 있다.

> 스프링 컨피그 서버 : src/resources/config/member-service-dev.yml

```yaml
spring:
  datasource:
    driverClassName: oracle.jdbc.driver.OracleDriver
    url: jdbc:oracle:thin:@localhost:1521:orcl
    username: PROJECT
    password: '{cipher}4b93b6c7060f269410654622559659db1e9049bd829a15521df43b825e6a860d'
```

- 스프링 클라우드 컨피그 서버는 암호화된 프로퍼티 앞에 <code>{cipher}</code>가 필요하다. <code>{cipher}</code>는 컨피그 서버가 암호화된 값을 처리하도록 지정한다.