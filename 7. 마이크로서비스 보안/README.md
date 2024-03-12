# 마이크로 서비스 보안 

- 견고한 마이크로서비스 아키텍처를 갖게 되면서 보안 취약점을 막는 작업은 점점 더 중요해지고 있다. 이 장에서는 보안과 취약점을 함께 다룬다. 취약점을 애플리케이션의 약점이나 결함으로 정의할 것이다. 물론 모든 시스템에는 취약점이 존재하지만, 이러한 취약점이 악용되고 피해를 주는지에 따라 큰 차이가 있다.
- 보안을 언급하면 개발자는 무의식적으로 한숨을 내쉴 때가 많다. 개발자에게서 “보안은 막연하고 이해하기 어려우며 디버깅하는 것은 더 어려워요.”라는 하소연을 듣는다. 하지만 필자는 보안을 걱정하지 않는다고 말하는 개발자(아마도 신출내기 개발자를 제외하고)를 여태껏 보지 못했다. 마이크로서비스 아키텍처를 안전하게 보호하는 일은 다음과 같은 여러 보호 계층과 연관되어 복잡하고 힘든 작업이다.
  - **애플리케이션 계층**: 적절하게 사용자를 통제하여 사용자 본인 여부와 수행하려는 작업의 수행 권한이 있는지 확인한다.
  - **인프라스트럭처**: 취약점의 위험도를 최소화하도록 서비스를 항상 실행하고 패치하고 최신화한다.
  - **네트워크 계층**: 서비스가 명확히 정의된 포트를 통해 인가된 소수의 서버에만 접근할 수 있도록 네트워크 접근 통제를 구현한다.

- 인증 및 권한 부여(인가) 제어를 구현하는 데 스프링 기반의 서비스를 보호하는 <code>스프링 클라우드 시큐리티(Security) 모듈</code>과 <code>키클록(Keycloak)</code>을 사용할 수 있다. 키클록은 최신 애플리케이션 및 서비스를 위한 ID 및 액세스 관리(identity and access management)용 오픈 소스 소프트웨어다. 이 오픈 소스 소프트웨어는 자바로 작성되었고 SAML(Security Assertion Markup Language) v2와 OpenID Connect(OIDC)/OAuth2 연합 ID(federated identity) 프로토콜을 지원한다.

## OAuth2 소개

- OAuth2는 토큰 기반의 보안 프레임워크로 권한 부여 패턴을 설명하지만 실제 인증을 수행하는 방법은 정의하지 않는다. 따라서 사용자는 ID 제공자(IdP, Identity provider)라고 하는 제삼자(3rd party) 인증 서비스로 자신을 인증할 수 있다. 사용자는 인증에 성공하면 모든 요청과 함께 전달할 토큰을 제공받고 인증 서비스에 이 토큰의 유효성을 확인한다.

- OAuth2의 주요 목적은 사용자 요청을 수행하기 위해 여러 서비스를 호출할 때, 요청을 처리하는 모든 서비스에 자격 증명(credentials)을 제시하지 않고도 각 서비스에서 사용자를 인증하는 것이다. OAuth2를 사용하면 그랜트(grants)라는 인증 체계를 통해 REST 기반의 서비스를 보호할 수 있다. OAuth2 명세에는 네 가지 그랜트 타입이 있다.
  - 패스워드(password)
  - 클라이언트 자격 증명(client credential)
  - 인가 코드(authorization code)
  - 암시적(implicit)
- 그랜트 타입을 모두 살펴보거나 예제를 제공하기는 너무 많아서 대신에 다음 사항을 다룰 것이다. 
  - 비교적 단순한 OAuth2 그랜트 타입인 패스워드(password) 그랜트 타입으로 마이크로서비스에서 OAuth2 사용 방법을 논의한다.
  - JWT(JSON Web Tokens)를 사용하여 더욱 안전한 OAuth2 솔루션을 제공하고 OAuth2 토큰 정보를 인코딩하는 표준을 만든다.
  - 마이크로서비스를 구축할 때 고려해야 할 다른 보안 사항을 살펴본다.

- OAuth2의 진정한 강점은 애플리케이션 개발자가 제삼자 ID 제공자와 쉽게 통합할 수 있으며, 자격 증명을 제삼자 서비스에 계속 전달하지 않고도 해당 서비스에서 사용자를 인증하고 인가할 수 있다는 것이다.
- OpenID Connect(OIDC)는 OAuth2 프레임워크에 기반을 둔 상위 계층으로 애플리케이션에 로그인한 사람(신원)에 대한 인증 및 프로파일 정보를 제공한다. 인가(authorization) 서버가 OIDC를 지원하는 경우, ID 제공자(identity provider)라고도 한다. 그럼 서비스를 보호하는 기술적 세부 사항을 설명하기 전에 키클록 아키텍처를 먼저 살펴보자.

---

## 키클록 소개
- 키클록(Keycloak)은 서비스와 애플리케이션을 위한 ID 및 액세스 관리용 오픈 소스 솔루션이다. 키클록의 주요 목표는 서비스와 애플리케이션의 코딩을 전혀 또는 거의 하지 않고 서비스와 애플리케이션을 쉽게 보호하는 것이다. 키클록의 주요 특징은 다음과 같다.
  - 인증을 중앙 집중화하고 SSO(Single Sign-On) 인증을 가능하게 한다.
  - 개발자는 인가와 인증처럼 보안 측면에 대해 걱정하기보다는 비즈니스 기능에 집중할 수 있다.
  - 2단계 인증(two-factor authentication)이 가능하다.
  - LDAP과 호환된다.
  - 애플리케이션과 서버를 쉽게 보호할 수 있는 여러 어댑터를 제공한다.
  - 패스워드 정책을 재정의할 수 있다.

![image1](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/1.png)
> 키클록을 사용하면 사용자가 자격 증명을 지속적으로 제시하지 않고 인증할 수 있다.

- 키클록 보안은 보호 자원, 자원 소유자, 애플리케이션, 인증 및 인가 서버 등 네 가지 구성 요소로 구분할 수 있다. 그림은 이 네 컴포넌트가 상호 작용하는 방식을 보여 준다.
  - **보호 자원(protected resource)**: 적절한 권한이 있는 인증된 사용자만 접근할 수 있게 하여 보호하려는 자원(이 경우 마이크로서비스)이다.
  - **자원 소유자(resource owner)**: 이 소유자는 어떤 애플리케이션이 서비스를 호출할 수 있는지, 어떤 사용자가 서비스에 대한 접근 권한을 부여받았는지, 사용자가 서비스에서 어떤 작업을 수행할 수 있는지 정의한다. 자원 소유자가 등록한 모든 애플리케이션에는 애플리케이션을 식별하는 애플리케이션 이름과 시크릿 키(secret key)가 지정된다. 애플리케이션 이름과 시크릿 키 조합은 액세스 토큰을 인증할 때 전달되는 자격 증명의 정보 일부다.  
  - **애플리케이션(application)**: 사용자를 대신하여 서비스를 호출하는 애플리케이션이다. 결국 사용자는 서비스를 직접 호출할 때가 거의 없고, 작업을 수행하려고 애플리케이션에 의존한다. 
  - **인증 및 인가 서버(authentication/authorization server)**: 인증 서버는 사용 중인 애플리케이션과 서비스 사이의 중개자다. 인증 서버를 사용하면 사용자는 자격 증명을 모든 서비스(애플리케이션이 사용자 대신 호출하는)에 전달하지 않고도 자신을 인증할 수 있다.  
- 앞서 언급했듯이, 키클록 보안 구성 요소는 서비스 사용자를 인증하기 위해 상호 작용한다. 사용자는 자격 증명과 보호 자원(마이크로서비스)에 액세스하는 데 사용하는 애플리케이션/디바이스를 제공하여 키클록 서버로 인증한다. 사용자의 자격 증명이 유효하다면 키클록 서버는 서비스 간 전달할 수 있는 인증 토큰을 제공한다.
- 그런 다음 보호 자원은 키클록 서버에 토큰의 유효성을 확인하고 사용자에게 할당된 역할(role)을 가져온다. 역할은 서로 연관된 사용자를 그룹으로 만들고 사용자가 접근할 수 있는 자원을 정의하는 데 사용된다. 여기에서는 인가된 서비스 엔드포인트를 사용자가 호출하는 데 사용할 수 있는 HTTP 동사(verb)를 결정하고자 키클록 역할을 활용할 것이다.
- 웹 서비스 보안은 매우 복잡한 주제다. 누가 우리 서비스를 호출할지(내부 또는 외부 사용자), 서비스를 어떻게 호출할지(내부 웹 기반 클라이언트, 모바일 디바이스나 웹 애플리케이션), 그리고 사용자는 우리 코드로 어떤 행동을 할지 이해해야 한다.

> 인증과 인가
> 
> 개발자가 인증(authentication)과 인가(권한 부여, authorization) 용어의 의미를 종종 혼용한다는 것을 발견했다. 인증은 자격 증명을 제시하여 사용자가 누구인지 증명하는 행위다. 인가(권한 부여)는 사용자가 원하는 작업을 수행할 수 있는지 여부를 결정한다. 예를 들어 사용자 ID와 패스워드를 제공하여 자신의 신원은 증명할 수 있지만 급여 데이터처럼 민감한 데이터를 볼 수 있는 권한은 없을 것이다. 말하자면 사용자는 권한을 부여받기 전에 인증되어야 한다.

---

## 작게 시작하기: 스프링과 키클록으로 한 개의 엔드포인트 보호

- 인증과 인가 부분을 설정하는 방법을 이해하고자 다음을 수행한다.
  - 키클록 서비스를 도커에 추가한다.
  - 키클록 서비스를 설정하고 애플리케이션을 사용자 신원을 인증하고 권한을 부여할 수 있는 인가된 애플리케이션으로 등록한다.
  - 스프링 시큐리티를 사용하여 서비스를 보호한다. 키클록 서비스에 대한 인증을 제공하고자  UI를 만들지 않고 포스트맨에서 사용자 로그인을 시뮬레이션한다.
  - 인증된 사용자만 호출할 수 있도록 회원 및 게시판 서비스를 보호한다.  


### 도커에 키클록 추가하기 

- keycloak 도커 이미지를 다운받는다.

```
docker pull jboss/keycloak
```


- keycloak 컨테이너 생성 및 백그라운드 실행
```
docker run --name keycloak -p 8073:8080 -d -e KEYCLOAK_USER=<사용할 아이디> -e KEYCLOAK_PASSWORD=<사용할 비밀번호> jboss/keycloak
```

> 현재 개발환경에서는 키클록 서버 포트가 8080으로 설정할 경우 다른 인스턴스와 중복될 수 있다 따라서 -p 8073:8080으로 설정하여 8073 포트로 접근할 수 있도록 설정한다. 

### 키 클록 설정 

- 서비스들이 실행되면 http://keycloak:8073/auth에 접속하여 키클록 관리자 콘솔을 열어 보자. 키클록의 구성을 설정하는 과정은 간단하다.
- 첫 번째 단계로 키클록에 접속하면 웰컴 페이지가 출력된다. 이 페이지는 관리자 콘솔, 문서화, 이슈 리포트 등 다양한 옵션을 보여 준다. 우리는 관리자 콘솔을 선택한다. 그림에서 이 웰컴 페이지를 보여 준다.

![image2](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/2.png)
> 키클록 웰컴 페이이지

- 다음 단계는 컨테이너 생성시에 정의된 사용자 이름과 패스워드를 입력하는 것이다. 다음 그림은 이 단계를 보여 준다.
- 데이터 구성 설정을 계속하려면 <code>realm(영역)</code>을 생성해야 한다. realm은 키클록이 사용자, 자격 증명, 역할(role), 그룹을 관리하는 객체를 참조하는 개념이다. realm을 생성하려면 키클록에 로그인한 후 드롭다운 메뉴에서 Add realm 항목을 클릭한다. 이 realm은 spmia-realm으로 명명한다.

![image3](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/3.png)
> 키클록 로그인 페이지

- 다음 그림은 키클록 서비스에서 이 책에서 사용할 spmia-realm 생성 방법을 보여 준다.

![image4](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/4.png)
> 키클록 'Add realm' 페이지는 사용자가 realm 이름을 입력할 수 있는 양식(form)을 보여 준다 
 
- realm이 생성되면 다음 그림 처럼 구성이 있는 spmia-realm의 메인 페이지를 볼 수 있다.

![image5](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/5.png)
> 키클록 spmia-realm 구성 페이지 

### 클라이언트 애플리케이션 등록

- 구성의 다음 단계는 클라이언트를 생성하는 것이다. 키클록에서 클라이언트는 사용자 인증을 요청할 수 있는 개체다. 클라이언트는 SSO 솔루션으로 보호하려는 애플리케이션 또는 서비스인 경우가 많다. 클라이언트를 생성하려면 Clients 메뉴를 선택한다. 그러면 다음과 같은 페이지가 보인다.

![image6](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/6.png)
> spmia-realm의 클라이언트 리스트 페이지 

- 라이언트 리스트가 출력되면 상기 그림에 있는 표의 오른쪽 위에 있는 Create 버튼을 누른다. 그러면 다음과 같은 정보를 요구하는 Add Client 입력란이 표시된다.
  - 클라이언트 ID
  - 클라이언트 프로토콜
  - 루트 URL
- 다음처럼 정보를 입력한다.

![image7](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/7.png)
> 키클록 클라이언트 정보 

- 클라이언트를 저장하면 다음 그림 처럼 클라이언트 구성(client configuration) 페이지가 표시된다. 이 페이지에서 다음 정보를 입력한다.
  - 액세스 타입(Access Type): Confidential
  - 서비스 계정 활성화(Service Accounts Enabled): On
  - 권한 부여 활성화(Authorization Enabled): On
  - 유효한 리다이렉트 URIs(Valid Redirect URIs): http://localhost:8080/*
  - 웹 오리진(Web Origins): *

![image8](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/8.png)

- 이 예제에서는 choongang이라는 글로벌 클라이언트만 생성했지만 이 페이지에서 클라이언트를 구성할 수도 있다.
- 다음 단계는 클라이언트 역할 설정으로 Roles 메뉴를 선택한다. 클라이언트 역할을 더 잘 이해하고자 애플리케이션에 관리자(admins)와 일반 사용자(regular users) 두 가지 유형의 사용자가 있다고 가정해 보자.
- Roles 페이지가 로드되면 미리 정의한 클라이언트 역할 목록이 표시된다. Roles 테이블 오른쪽 위에 표시된 Add Role 버튼을 눌러 보자. 그럼 다음처럼 ‘Add Role’ 폼이 표시된다.

![image9](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/9.png)
> 키클록의 'Add Role' 페이지

- ‘Add Role’ 페이지에서 다음 클라이언트 역할을 생성해야 한다. 결과적으로 그림 9-10과 유사한 목록이 표시될 것이다.
  - USER
  - ADMIN

![image10](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/10.png)
> 키클록 클라이언트에 대한 역할 - USER와 ADMIN

- 이제 기본 클라이언트 구성을 완료했으므로 ‘Credentials(자격 증명)’ 페이지를 접속해 보자. ‘Credentials’ 페이지는 인증 과정에 필요한 클라이언트 시크릿(client secret)을 표시한다. 다음 그림에서 ‘Credentials’ 페이지 내용을 보여 준다.

![image11](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/11.png)

- 다음 구성 단계는 realm roles(영역 역할)를 생성하는 것이다. realm roles(영역 역할)를 사용하면 각 사용자에 대한 역할을 더 잘 통제할 수 있다. 이 단계는 선택적이다. 이러한 역할을 생성하지 않으려면 사용자를 직접 생성하면 된다. 하지만 나중에 각 사용자의 역할을 식별하고 관리하는 것이 더 어려울 수 있다.
- realm roles를 생성하려면 왼쪽 메뉴에서 Roles 항목을 클릭한 후 테이블 오른쪽 위에 있는 Add Role 버튼을 누른다. 클라이언트 역할과 마찬가지로 choongang-user와 choongang-admin이라는 두 가지 유형의 realm roles를 생성한다. 다음 그림은 choongang-admin을 위한 realm의 역할 생성 방법을 보여 준다.

![image12](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/12.png)
> choongang-admin을 위한 realm 역할 생성

![image13](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/13.png)
> choongang-admin의 realm role(영역 역할)에 대한 추가 구성 정보 설정 

- choongang-admin용 realm 역할이 구성되었으므로 동일한 방식으로 choongang-user 역할을 생성해 보자. 완료되면 다음과 같은 유사한 목록이 표시되어야 한다.

![image14](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/14.png)
> choongang의 spmia-realm 역할 목록 

### 사용자 구성 

- 지금까지 애플리케이션 및 realm의 역할, 이름, 시크릿을 정의했으며 개별 사용자의 자격 증명과 사용자 역할을 설정할 준비를 완료했다. 사용자를 생성하려면 키클록 관리자 콘솔의 왼쪽 메뉴에서 Users 항목을 클릭한다.
- 이 장의 예에서는 user01와 user02 두 개의 사용자 계정을 정의한다. user01 계정은 choongang-user 역할이 지정되고, user02 계정은 choongang-admin 역할이 지정된다
- 다음은 ‘Add user’ 페이지를 보여 준다. 이 페이지에서 사용자 이름을 입력하고, 사용자 및 이메일 확인 옵션을 활성화하자.

![image15](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/15.png)
> choongang spmia-realm을 위한 키클록 'Add user'페이지 
>
> 키클록에서는 이름, 성, 이메일, 주소, 생년월일, 전화번호 등 부가 속성을 사용자에 추가할 수 있지만, 이 예에서는 필수 속성만 설정한다.

- 양식을 저장한 후 Credentials 탭을 클릭하자. 사용자 패스워드를 입력하고 Temporary(임시) 옵션을 비활성화한 후 Set Password 버튼을 누른다. 

![image16](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/16.png)
> choongang 사용자 자격 증명에 대한 사용자 비밀번호를 설정하고 Temporary(임시) 옵션을 비활성화한다.

- 패스워드가 설정되면 Role Mappings 탭을 클릭하고 사용자에게 특정 역할을 지정하자.

![image17](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/17.png)
> realm role을 생성한 사용자에게 매핑한다.

- 구성을 완료하려면 다른 사용자인 user02에도 동일한 단계를 반복하자. 이 예에서는 user02에게 choongang-user 역할을 지정한다.

### 사용자 인증 

- 이제 패스워드 그랜트(password grant) 플로에 대한 애플리케이션 및 사용자 인증을 수행할 수 있는 키클록 서버 기능이 준비되었다. 바로 인증 서비스를 시작하기 위해 왼쪽 메뉴에서 Realm Settings 항목을 선택한 후 OpenID Endpoint Configuration 링크를 클릭하여 이 realm에 대한 가용한 엔드포인트 목록을 살펴보자.

![image18](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/18.png)
> 키클록 spmia-realm을 위한 OpenID Endpoint Configuration 링크 선택 

- 토큰 엔드포인트를 복사 
![image19](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/19.png)
> realm 역할을 생성된 사용자와 매핑

- 이제 액세스 토큰을 획득하려는 사용자를 시뮬레이션한다. 이 시뮬레이션에서 ARC는 http://keycloak:8073/auth/realms/spmia-realm/protocol/openid-connect/token 엔드포인트에 POST 호출을 할 때 애플리케이션, 시크릿 키, 사용자 ID와 패스워드를 전달한다.
- 사용자가 인증 토큰을 획득하는 것을 시뮬레이션하려면 애플리케이션 이름과 시크릿 키를 ARC에 설정해야 한다. 이를 위해 기본 인증(basic authentication) 방법을 사용하여 이 정보를 인증 서버에 전달한다. 다음은 기본 인증을 실행하는 ARC 설정 방법을 보여 준다. 이전에 정의한 애플리케이션 이름을 Username으로, 시크릿 키를 Password로 전달한다.

```
Username: <CLIENT_APPLICATION_NAME>
Password: <CLIENT_APPLICATION_SECRET>
```

![image20](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/20.png)
> 애플리케이션 키와 시크릿을 사용한 기본 인증 설정 

- 하지만 토큰을 얻기 위해 호출할 준비가 아직 끝나지 않았다. 애플리케이션 이름과 시크릿 키가 구성 설정되면 다음 정보를 HTTP 양식(form) 매개변수로 서비스에 전달해야 한다.
  - **grant_type**: 실행할 그랜트 타입이다. 이 예에서는 password 그랜트를 사용한다.
  - **username**: 로그인하는 사용자 이름이다.
  - **password**: 로그인하는 사용자 패스워드다.
  - **client_id**: 애플리케이션 키 
  - **client_secret**: 애플리케이션 시크릿 

![image21](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/21.png)
> 액세스 토큰을 요청할 때 사용자의 자격증명이 HTTP 양식 매개변수로 /openid-connect/token 엔드포인트에 전달된다.

- 클라이언트 자격 증명의 유효성 검증을 성공한 후 반환되는 JSON 페이로드
```json

{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJkakZIbXI4UURpOFdDWC05eEw0bzc1REstdTEwNWR4cUJaTktVeUZ3Zld3In0.eyJleHAiOjE3MTAyMTQ1NzgsImlhdCI6MTcxMDIxNDI3OCwianRpIjoiZWUyOTQ3ZTYtNWFiMi00MjQ5LTlhYzktZDdmYTljNWMzMzg4IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDczL2F1dGgvcmVhbG1zL3NwbWlhLXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjJmY2FjNWRiLTkwMmMtNGFlYy1iN2IyLTg0MWYzMmZhY2Y1NiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImNob29uZ2FuZyIsInNlc3Npb25fc3RhdGUiOiIxNjZlYWZhNy1kM2MzLTQ0ZTAtYjdlMi1hMzNhZjdkNTJmYTMiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImNob29uZ2FuZy1hZG1pbiIsIm9mZmxpbmVfYWNjZXNzIiwiZGVmYXVsdC1yb2xlcy1zcG1pYS1yZWFsbSIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiY2hvb25nYW5nIjp7InJvbGVzIjpbIkFETUlOIl19LCJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJzaWQiOiIxNjZlYWZhNy1kM2MzLTQ0ZTAtYjdlMi1hMzNhZjdkNTJmYTMiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6InVzZXIwMSJ9.ETvBzvyltUsCLD76NjF8-apGlkQWt41S7Nxn0Be2G_HsRXv-koDuShluMnbeP-B8NrOflPvoPXtnn6FwXx5SB4bjHFT2dLUTsa-gebWAn0Bc4wJyxE2nI2bVpjvaeqWi-7NETUnAtFa4myXAQqX4sKWm2drt1p_yOeQdGTDwIw3I0qlz5jVvqfjn3xGp8ntbVtVAHBYamjtzOlwBIQNXLvH8cH8_OkqpUHezpXJl1-r6xkix5Npppt63IhH1vC03ODDJwPaNnJ-qoJs7K_AsxyCeN19jMSmn_JMTXRqm1mbl-ztZMcUQ6yXe1XYYYNVLecxLOowSgNDtTXf-FCegvA",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJjYWU2ZjQ4YS1hMDhmLTRkYTYtYWY1MS0zYTliNzk1MTAyZTMifQ.eyJleHAiOjE3MTAyMTYwNzgsImlhdCI6MTcxMDIxNDI3OCwianRpIjoiNGU4NjY0MmUtNTQzOS00OTIyLTgxMDAtMGRhOTU3ZGI0NDBmIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDczL2F1dGgvcmVhbG1zL3NwbWlhLXJlYWxtIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDo4MDczL2F1dGgvcmVhbG1zL3NwbWlhLXJlYWxtIiwic3ViIjoiMmZjYWM1ZGItOTAyYy00YWVjLWI3YjItODQxZjMyZmFjZjU2IiwidHlwIjoiUmVmcmVzaCIsImF6cCI6ImNob29uZ2FuZyIsInNlc3Npb25fc3RhdGUiOiIxNjZlYWZhNy1kM2MzLTQ0ZTAtYjdlMi1hMzNhZjdkNTJmYTMiLCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJzaWQiOiIxNjZlYWZhNy1kM2MzLTQ0ZTAtYjdlMi1hMzNhZjdkNTJmYTMifQ.6bvX8SoTUr5STIYRzFiJxIk6Hf6SIxhN9yuvzQkdz4Y",
  "token_type": "Bearer",
  "not-before-policy": 0,
  "session_state": "166eafa7-d3c3-44e0-b7e2-a33af7d52fa3",
  "scope": "profile email"
}
```
- 이 JSON 페이로드에는 다음 다섯 가지 속성이 포함된다.
  - **access_token**: 사용자가 보호 자원에 대한 서비스를 호출할 때 제시하는 액세스 토큰이다.
  - **token_type**: 인가(authorization) 명세에 따라 다양한 토큰 타입을 정의할 수 있다. 가장 일반적으로 사용하는 토큰 타입은 베어러 토큰(Bearer Token)이다.
  - **refresh_token**: 만료된 액세스 토큰을 재발급하려고 리프레시 토큰(refresh token)을 인가 서버에 제출한다.
  - **expires_in**: 액세스 토큰이 만료되기까지 걸리는 시간(초)이다. 스프링에서 인가 토큰 만료의 기본값은 12시간이다.
  - **scope**: 이 액세스 토큰의 유효 범위를 지정한다.

- 이제 인가 서버에서 유효한 액세스 토큰을 얻었으므로 https://jwt.io에서 JWT를 디코딩하여 액세스 토큰의 모든 정보를 조회할 수 있다. 다음은 디코딩된 JWT 결과를 보여 준다.

![image21](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/7.%20%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%B3%B4%EC%95%88/images/21.png)
> 발급된 액세스 토큰을 기반으로 사용자 정보 조회

---

## 키클록으로 조직 서비스 보호하기

- 키클록 서버에 클라이언트를 등록하고 역할과 함께 사용자 계정을 설정했다면 이제 스프링 시큐리티(Spring Security)와 키클록 스프링 부트 어댑터(Keycloak Spring Boot Adapter)로 자원(resources)을 보호하는 방법을 알아보자. 액세스 토큰의 생성 및 관리는 키클록 서버의 책임이지만, 스프링에서는 어떤 사용자 역할이 어떤 행위를 할 수 있는 권한 여부는 서비스별로 정해진다. 보호 자원을 설정하려면 다음 작업을 수행해야 한다.


--- 

## 마이크로서비스 보안을 마치며

