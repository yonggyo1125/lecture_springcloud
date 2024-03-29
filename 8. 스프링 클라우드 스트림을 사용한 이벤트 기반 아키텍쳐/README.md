# 스프링 클라우드 스트림을 사용한 이벤트 기반 아키텍처
- 인간은 환경과 상호 작용할 때 항상 동적인 상태에 있다. 일반적으로 대화는 동기적이거나 선형적이지 않으며, 요청-응답 모델처럼 단순하게 정의되지도 않는다. 그것은 마치 우리가 주변 사물과 지속적으로 메시지를 주고받듯이 메시지가 주도하는 것처럼 보인다. 보통 메시지를 받으면 메시지에 반응하여 진행 중인 주요 작업을 중단할 때가 많다.

- 이 장에서는 비동기 메시지를 사용하여 다른 마이크로서비스와 통신하는 스프링 기반 마이크로서비스를 설계하고 구현하는 방법을 다룬다. 애플리케이션 간 통신에서 비동기식 메시지를 사용하는 것은 더 이상 새롭지 않다. 새로운 점은 메시지를 사용하여 상태 변경을 표현하는 이벤트로 통신한다는 개념이다. 이러한 개념을 <code>이벤트 기반 아키텍처(EDA, Event-Driven Architecture)</code> 또는 <code>메시지 기반 아키텍처(MDA, Message-Driven Architecture)</code>라고 한다. EDA 기반의 접근 방식으로 특정 라이브러리나 서비스에 밀접하게 결합하지 않고 변화에 대응할 수 있는 높은 수준의 분리된 시스템을 구축할 수 있다. 마이크로서비스를 통합할 때 EDA를 사용하면 애플리케이션에서 발송하는 이벤트(메시지) 스트림을 서비스는 수신만 하면 되기 때문에 애플리케이션에 새로운 기능을 빠르게 추가할 수 있다.

- 스프링 클라우드 프로젝트를 사용하면 <code>스프링 클라우드 스트림(Spring Cloud Stream)</code>이라는 하위 프로젝트를 이용하여 메시지 기반 솔루션을 손쉽게 구축할 수 있다. 스프링 클라우드 스트림은 하부 메시징 플랫폼과 연관된 세부 구현에서 서비스를 보호함으로써 메시지 발행(publish)과 소비(consume)를 쉽게 구현할 수 있게 한다.


## 메시징과 EDA, 마이크로서비스의 사례

- 마이크로서비스 기반 애플리케이션을 구축하는 데 왜 메시징이 중요할까? 이 질문에 답하기 위해 예제를 하나 살펴보자

- 운영 환경에 이 서비스들을 배포한 후 게시판 서비스에서 정보를 조회할 때  회원 서비스 호출이 지나치게 오래 걸린다는 것을 발견했다고 하자. 게시판 서비스 데이터의 사용 패턴을 살펴보면 게시판 데이터는 거의 변경이 없고 게시판 서비스에서 읽어 오는 대부분의 데이터가 게시판 레코드의 기본 키(primary key)로 수행된다는 것을 알 수 있었다. 데이터베이스 액세스 비용을 들이지 않고 게시판 데이터의 읽기를 캐싱할 수 있다면 회원 서비스에 대한 호출 응답 시간을 크게 향상시킬 수 있을 것이다. 캐싱 솔루션을 구현하려면 다음 세 가지 핵심 요구 사항을 고려해야 한다.
  - **캐싱된 데이터는 회원 서비스의 모든 인스턴스에 일관성이 있어야 한다**: 이것은 어떤 회원 서비스의 인스턴스에 접근하든 동일한 게시판 데이터 읽기가 보장되어야 하므로 회원 서비스 내에서 로컬로 데이터를 캐싱할 수 없다는 것을 의미한다.
  - **회원 서비스를 호스팅하는 컨테이너 메모리에 게시판 데이터를 캐싱하면 안 된다**: 서비스를 호스팅하는 런타임 컨테이너는 크기가 제한되어 있을 때가 많고, 다양한 액세스 패턴으로 데이터를 가져갈 수 있다. 로컬 캐싱은 클러스터 내 다른 서비스들과 동기화를 보장해야 하므로 복잡성을 유발한다.
  - **업데이트나 삭제로 게시판 레코드가 변경될 때 회원 서비스는 게시판 서비스의 상태 변화를 인식해야 한다**: 이 경우 회원 서비스는 특정 게시판의 캐싱된 데이터를 모두 무효화하고 캐시에서 제거해야 한다.  

- 이러한 요구 사항을 구현하는 두 가지 접근 방법이 있다. 
  - 첫 번째 방법은 동기식 요청-응답 모델로 요구 사항을 구현하는 것이다. 게시판 상태가 변경되면 회원과 게시판 서비스는 REST 엔드포인트를 이용하여 서로 통신한다. 
  - 두 번째 방법은 조직 서비스는 자기 데이터가 변경되었음을 알리려고 비동기 이벤트(메시지)를 발송하는 것이다. 이 방법을 사용하면 게시판 서비스는 게시판 레코드가 업데이트 또는 삭제(상태 변경)되었음을 나타내는 메시지를 큐에 발행한다. 회원 서비스는 중개자(메시지 브로커 또는 대기열)와 함께 수신하여 게시판 이벤트가 발생했는지 확인하고, 발생하면 캐시에서 조직 데이터를 삭제한다.

### 동기식 요청-응답 방식으로 상태 변화 전달

- 게시판 데이터의 캐시(cache)에 분산 키-값 저장소 데이터베이스인 레디스(Redis)(https://redis.io/)를 사용한다. 다음 그림은 레디스처럼 전통적인 동기식 요청-응답 프로그래밍 모델을 사용한 캐싱 솔루션 구축 방법을 개괄적으로 보여 준다.

![image1](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/8.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%8A%A4%ED%8A%B8%EB%A6%BC%EC%9D%84%20%EC%82%AC%EC%9A%A9%ED%95%9C%20%EC%9D%B4%EB%B2%A4%ED%8A%B8%20%EA%B8%B0%EB%B0%98%20%EC%95%84%ED%82%A4%ED%85%8D%EC%B3%90/images/1.png)
> 동기식 요청-응답 모델에서 강하게 결합된 서비스는 복잡하고 깨지기 쉽다.


- 그림에서 사용자가 회원 서비스를 호출하면 회원 서비스는 게시판 데이터를 조회해야 한다. 이 작업을 수행하고자 회원 서비스는 먼저 레디스 클러스터에서 원하는 게시판을을 게시판 ID로 조회한다. 조직 데이터가 조회되지 않는다면 REST 기반 엔드포인트로 조직 서비스를 호출하여 전달받은 데이터를 레디스에 저장한 후 사용자에게 전달한다.

- 누군가 조직 서비스의 REST 엔드포인트를 사용하여 게시판 레코드를 업데이트하거나 삭제하면, 게시판 서비스는 캐시에 있는 게시판 데이터를 무효화하려고 회원 서비스의 엔드포인트를 호출해야 한다. 상기 그림에서 게시판 서비스가 회원 서비스를 호출하여 레디스 캐시를 무효화할 때 적어도 다음 세 가지 문제를 발견할 수 있다
  - 게시판 서비스와 회원 서비스는 강하게 결합되어 있고 이러한 결합은 깨지기 쉽다.
  - 캐시를 무효화하는 회원 서비스 엔드포인트가 변경되면 게시판 서비스도 함께 변경되어야 한다. 이러한 방식은 유연하지 못하다.
  - 게시판 데이터 변경을 알리도록 회원 서비스를 호출하는지 확인하는 게시판 서비스의 코드를 수정하지 않은 채 게시판 데이터에 대한 새로운 소비자를 추가할 수는 없다.

- 서비스 간 강한 결합
  - 데이터를 조회하려고 회원 서비스는 게시판 서비스에 의존한다. 하지만 게시판 레코드가 업데이트되거나 삭제될 때마다 게시판 서비스가 회원 서비스와 직접 통신하기 때문에 게시판 서비스에서 다시 회원 서비스로 강한 결합이 생겼다. 레디스 캐시의 데이터를 무효화하려면, 게시판 서비스는 회원 서비스가 캐시를 무효화하기 위해 노출한 엔드포인트를 호출하거나 회원 서비스가 소유한 레디스 서버와 직접 통신해서 캐시 데이터를 삭제해 주어야 한다.
  - 게시판 서비스가 레디스와 통신하면 다른 서비스가 소유하는 데이터 저장소와 직접 통신하게 되므로 그 자체로 문제가 된다. 이것은 마이크로서비스 환경에서 절대 금기해야 할 사항이다. 게시판 데이터는 당연히 회원 서비스에 속한다고 주장할 수 있지만, 회원 서비스는 그 데이터를 특정 컨텍스트에 사용하고 나중에 변환하거나 데이터와 연관된 비즈니스 규칙을 만들 가능성도 있다. 게시판 서비스가 레디스와 직접 통신하게 되면 회원 서비스 팀이 구현한 규칙을 실수로 깨뜨릴 수 있다.
- 쉽게 깨지는 서비스 관계
  - 회원 서비스와 게시판 서비스 사이의 강한 결합은 오히려 두 서비스 간 취성(쉽게 깨지는 성질)을 만든다. 회원 서비스가 다운되거나 느려지면 게시판 서비스는 회원 서비스와 직접 통신하기 때문에 영향을 받는다. 그리고 게시판 서비스가 회원 서비스의 레디스 데이터 저장소와 직접 통신하게 되면 게시판 서비스와 레디스 사이에 의존성이 만들어진다. 이 경우 공유된 레디스 서버에 문제가 발생하면 두 서비스 모두 다운될 위험이 있다.
- 게시판 서비스의 변경에 관심 있는 새 서비스를 추가할 때의 경직성
  - 게시판 데이터 변경에 관심이 있는 다른 서비스가 생겼다면 게시판 서비스에서 그 서비스로 호출을 추가해야 한다. 이것은 게시판 서비스의 코드를 변경하고 재배포해야 한다는 것을 의미하며 코드는 유연하지 못하게 된다.
  - 상태 변경을 전달하려고 동기식 요청-응답 모델을 사용하면, 애플리케이션의 핵심 서비스와 다른 서비스 간에 거미줄 같은 종속적 형태를 띠기 시작한다. 이 거미줄 중앙 지점은 애플리케이션 내 주요 장애 지점이 된다.  

### 메시징을 사용한 서비스 간 상태 변화 전달

- 메시징 방식에서는 회원 및 게시판 서비스 사이에 토픽(topic)이 추가된다. 메시징 시스템은 게시판 서비스에서 데이터를 읽는 데 사용되지 않고, 게시판 서비스가 자신이 관리하는 데이터의 상태 변경을 발행하는 데 사용된다.

![image2](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/8.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%8A%A4%ED%8A%B8%EB%A6%BC%EC%9D%84%20%EC%82%AC%EC%9A%A9%ED%95%9C%20%EC%9D%B4%EB%B2%A4%ED%8A%B8%20%EA%B8%B0%EB%B0%98%20%EC%95%84%ED%82%A4%ED%85%8D%EC%B3%90/images/2.png)
> 게시판 상태가 변경될 때, 메시지는 회원 및 게시판 서비스 사이에 위치한 메시지 큐에 기록된다.

### 메시징 아키텍처의 장점

- 게시판 데이터가 변경될 때 게시판 서비스는 메시지를 토픽에 발행한다. 회원 서비스는 메시지에 대한 토픽을 모니터링하고 메시지가 도착하면 레디스 캐시에서 해당하는 게시판 레코드를 삭제한다. 상태 전달에서 메시지 큐(토픽)는 회원 서비스와 게시판 서비스 사이의 중개자 역할을 한다. 이 방식은 다음 네 가지 이점을 제공한다.
  - 느슨한 결합(loose coupling)
  - 내구성(durability) 
  - 확장성(scalability)
  - 유연성(flexibility)

- 느슨한 결합
  - 마이크로서비스 애플리케이션은 상호 작용하고 데이터가 각자 관리되는 수십 개의 작고 분산된 서비스로 구성될 수 있다. 앞서 설명한 동기식 설계에서 보았듯이 동기식 HTTP 응답은 회원 및 게시판 서비스 간 강한 종속성을 만든다. 이러한 종속성을 완전히 제거할 수는 없지만 서비스가 소유한 데이터를 직접 관리하는 엔드포인트만 노출해서 종속성을 최소화할 수 있다.
  - 메시징 방식에서는 상태 변경을 전달하는데, 두 서비스는 서로 알지 못하기 때문에 이들을 분리할 수 있다. 게시판 서비스는 상태 변화를 발행해야 할 때 메시지 큐에 기록한다. 회원 서비스는 메시지가 수신되었다는 것만 알고 누가 발행했는지는 알지 못한다.  

- 확장성 
  - 메시지가 큐에 저장되므로 메시지 발신자는 메시지 소비자에게서 응답을 기다릴 필요가 없다. 발신자는 하던 일을 계속하면 된다. 마찬가지로 큐에서 메시지를 읽어 오는 소비자가 메시지를 충분히 빠르게 처리하지 못한다면, 메시지 소비자를 더 많이 가동시켜 큐의 메시지를 처리하게 하는 것은 큰 일이 아니다. 확장성을 사용한 방식은 마이크로서비스 모델에 적합하다.
  - 중요한 점 하나는 마이크로서비스의 새 인스턴스를 시작하는 일이 간단해야 한다는 것이다. 그러면 추가된 마이크로서비스는 메시지 큐를 처리하는 또 다른 서비스가 될 수 있으며, 이것은 수평 확장(scaling horizontally)의 한 예다. 큐에 있는 메시지를 읽는 기존의 확장 메커니즘은 한 번에 처리할 수 있는 스레드 수를 증가하는 것과 연관이 있었다. 불행히도 이 방식을 사용하면 메시지 소비자에서 사용 가능한 CPU 수가 결국 제한된다. 마이크로서비스 모델은 메시지를 소비하는 서비스를 호스팅하는 머신 수를 늘려 확장하기 때문에 이러한 제약이 없다.  

- 유연성
  - 메시지 발신자는 누가 메시지를 소비할지 모른다. 이것은 원본 발송 서비스에 영향을 주지 않은 채 새로운 메시지 소비자(및 새로운 기능)를 쉽게 추가할 수 있음을 의미한다. 이는 기존 서비스를 건드리지 않고 새로운 기능을 애플리케이션에 추가할 수 있기 때문에 매우 강력한 개념이다. 그리고 새로운 소비자의 코드는 발행되는 이벤트를 수신하고 적절히 대응할 수 있다.  

### 메시징 아키텍쳐의 단점 

- 다른 아키텍처 모델처럼 메시지 기반의 아키텍처 역시 장단점이 있다. 메시지 기반 아키텍처는 복잡할 수 있고, 개발 팀이 다음 사항을 포함한 몇 가지 주요 사항에 세심한 주의를 기울여야 한다.
  - 메시지 처리의 의미론(message handling semantics)
  - 메시지 가시성(message visibility)
  - 메시지 코레오그래피(message choreography)   

- 메시지 처리의 의미론
  - 마이크로서비스 기반 애플리케이션에서 메시지를 사용하려면 메시지를 발행하고 소비하는 방법을 이해하는 것 이상이 필요하다. 애플리케이션이 메시지가 소비되는 순서에 따라 어떻게 동작하는지와 메시지가 순서대로 처리되지 않을 때 어떻게 되는지를 이해해야 한다. 예를 들어 한 고객의 모든 주문을 받은 순서대로 처리해야 한다는 엄격한 요구 사항이 있다면, 모든 메시지를 서로 독립적으로 소비하는 경우와 다르게 메시지 처리를 설정하고 구성해야 한다.
  - 또한 메시징을 사용하여 데이터의 엄격한 상태 전환을 적용하는 경우, 메시지가 예외를 발생시키거나 에러가 순서대로 처리되지 않는 시나리오를 염두에 두고 애플리케이션 설계를 고려해야 한다. 메시지가 실패하면 에러 처리를 재시도하는가? 아니면 그냥 실패하도록 놔둘 것인가? 고객의 메시지 중 하나가 실패하면 해당 고객과 관련된 향후 메시지를 어떻게 처리하는가? 이는 중요하게 생각해 볼 질문이다.
- 메시지 가시성
  - 마이크로서비스에서 메시지를 사용한다는 것은 종종 동기식 서비스 호출과 비동기식 서비스 처리가 혼합됨을 의미한다. 메시지의 비동기적 특성으로 메시지가 발행되거나 소비되는 시점에 바로 수신 또는 처리되지 않을 수 있다. 또한 웹 서비스 호출 및 메시지 전반에 걸쳐 사용자의 트랜잭션을 추적하는 상관관계 ID 등을 사용하는 것은 애플리케이션에서 발생하는 일을 이해하고 디버깅하는 데 매우 중요하다. 앞서 설명했듯이, 상관관계 ID는 사용자 트랜잭션의 시작 시점에 생성되어 모든 서비스 호출에 전달되는 고유한 번호이며 발행 및 소비되는 모든 메시지에 포함되어 전달되어야 한다.
- 메시지 코레오그래피
  - ‘메시지 가시성’에서 언급했듯이, 메시지 기반 애플리케이션은 코드가 더 이상 단순한 요청- 응답 모델에 따라 선형적인 방식으로 처리되지 않기 때문에 비즈니스 로직을 추론하는 것이 더 어렵다. 메시징 기반 애플리케이션을 디버깅하려면 여러 다른 서비스의 로그(사용자 트랜잭션이 순서 없이 다양한 시간에 실행되어 있는 로그)를 모두 살펴보아야 한다는 것이다.

> 메시징은 복잡하지만 강력하다. 메시징의 긍정적인 측면은 비즈니스 자체가 비동기식으로 작동하므로 결국 비즈니스를 밀접하게 모델링한다는 것이다.
---

## 스프링 클라우드 스트림 소개

- 스프링 클라우드를 사용하면 스프링 기반 마이크로서비스에 메시징을 쉽게 통합할 수 있다. 이 통합은 스프링 클라우드 스트림(Spring Cloud Stream) 프로젝트(https://spring.io/projects/spring-cloud-stream)로 수행된다. 이 프로젝트는 애플리케이션의 메시지 발행자와 소비자를 쉽게 구축할 수 있는 애너테이션 기반 프레임워크다.
- 스프링 클라우드 스트림은 우리가 사용하는 메시징 플랫폼의 세부 구현도 추상화한다. 아파치 카프카 프로젝트나 RabbitMQ를 포함한 여러 메시지 플랫폼을 스프링 클라우드 스트림과 함께 사용할 수 있으며, 특정 플랫폼을 위한 세부 구현은 애플리케이션 코드에서 제외된다. 애플리케이션에서 메시지 발행과 소비에 대한 구현은 플랫폼 중립적인 스프링 인터페이스로 수행된다.

> 예제에서는 카프카(https://kafka.apache.org/)라는 메시지 버스(message bus)를 사용한다. 카프카는 비동기적으로 메시지 스트림을 전송할 수 있는 고성능 메시지 버스다. 자바로 작성된 카프카는 안정성과 확장성이 뛰어나 클라우드 기반 애플리케이션을 위해 가장 널리 사용되는 메시지 버스가 되었다. 스프링 클라우드 스트림은 메시지 버스로 RabbitMQ도 지원한다.

- 스프링 클라우드 스트림을 이해하려면 먼저 스프링 클라우드 스트림 아키텍처에 대한 논의를 시작하고 관련 용어에 익숙해지자. 메시지 기반 플랫폼을 사용한 경험이 없다면 새로운 용어가 다소 부담스러울 수 있기 때문에 메시징을 사용하여 통신하는 두 서비스 관점에서 스프링 클라우드 스트림 아키텍처를 살펴보고 논의를 시작하자. 한 서비스는 <code>메시지 발행자(publisher)</code>가 되고, 다른 서비스는 <code>메시지 소비자(consumer)</code>가 된다.

![image3](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/8.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%8A%A4%ED%8A%B8%EB%A6%BC%EC%9D%84%20%EC%82%AC%EC%9A%A9%ED%95%9C%20%EC%9D%B4%EB%B2%A4%ED%8A%B8%20%EA%B8%B0%EB%B0%98%20%EC%95%84%ED%82%A4%ED%85%8D%EC%B3%90/images/3.png)
> 메시지가 발행되고 소비될 때 하부 메시징 플랫폼을 추상화한 스프링 클라우드 스트림 컴포넌트들을 통과한다.

- 스프링 클라우드에서 메시지를 발행하고 소비하는 데 다음 네 개의 컴포넌트가 관련이 있다.
  - 소스(source)
  - 채널(channel)
  - 바인더(binder)
  - 싱크(sink)

- 서비스가 메시지를 발행할 준비가 되면 <code>소스(source)</code>를 사용하여 메시지를 발행한다. 소스는 발행할 메시지를 표현하는 POJO(Plain Old Java Object)를 전달받는 스프링의 애너테이션 인터페이스다. **소스는 메시지를 받아 직렬화하고(직렬화 기본 설정은 JSON) 메시지를 채널에 발행**한다.
- <code>채널(channel)</code>은 메시지 생산자가 발행하거나 메시지 소비자가 소비한 후 <coode>메시지를 보관할 큐(queue)를 추상화</code>한 것이다. 즉, 채널은 메시지를 보내고 받는 큐로 기술할 수 있다. 채널 이름은 항상 대상 큐의 이름과 연결되지만, 해당 큐 이름은 코드에 직접 노출되지 않는 대신 채널 이름이 코드에서 사용된다.
- <code>바인더(binder)</code>는 스프링 클라우드 스트림 프레임워크의 일부이며, <code>특정 메시지 플랫폼과 통신하는 스프링 코드</code>다. 스프링 클라우드 스트림 프레임워크의 바인더 부분은 **메시지를 발행하고 소비하는데, 특정 플랫폼 라이브러리 및 API를 노출하지 않고도 메시지 작업을 할 수 있게 해 준다.**

- 스프링 클라우드 스트림에서 서비스는 <code>싱크(sink)</code>를 사용하여 **큐에서 메시지를 받는다.** 싱크는 들어오는 메시지 채널을 수신 대기(listen)하고 메시지를 다시 POJO 객체로 역직렬화한다. 이 과정을 거친 메시지는 스프링 기반 서비스의 비즈니스 로직에서 처리된다.
---

## 간단한 메시지 생산자와 소비자 작성

- 지금까지 스프링 클라우드 스트림의 필수 구성 요소를 살펴보았는데, 이제 간단한 예제를 진행해 보자. 
- 첫 번째 예제에서는 게시판 서비스에서 회원 서비스로 메시지를 전달하고 로그 메시지를 콘솔에 출력한다. 그리고 이 예제에는 스프링 클라우드 스트림 <code>소스(메시지 생산자)</code>와 <code>싱크(메시지 소비자)</code>가 하나만 있기 때문에 몇 가지 간단한 스프링 클라우드 기본 기능을 사용하여 시작할 것이다. 이 기능을 사용하면 게시판 서비스의 소스 설정과 회원 서비스의 싱크 설정이 간단해진다. 다음 그림은 메시지 생산자 위주로, 일반적인 스프링 클라우드 스트림 아키텍처를 기반으로 한다.

![image4](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/8.%20%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C%20%EC%8A%A4%ED%8A%B8%EB%A6%BC%EC%9D%84%20%EC%82%AC%EC%9A%A9%ED%95%9C%20%EC%9D%B4%EB%B2%A4%ED%8A%B8%20%EA%B8%B0%EB%B0%98%20%EC%95%84%ED%82%A4%ED%85%8D%EC%B3%90/images/4.png)
> 게시판 서비스 데이터가 변경될 때 orgChangeTopic에 메시지가 발행된다.

### 아파치 카프카 및 레디스 도커 구성

### 게시판 서비스에서 메시지 생산자 작성

- 이 아키텍처에서 토픽을 사용하는 방법에 집중한다. 따라서 게시판 데이터가 추가, 수정, 삭제될 때마다 게시판 서비스가 카프카 토픽에 메시지를 발행하여 게시판 변경 이벤트가 발생했음을 알려 줄 수 있도록 게시판 서비스를 수정하는 것부터 시작할 것이다. 발행된 메시지에는 변경 이벤트와 관련된 조직 ID와 발생한 작업(추가, 수정, 삭제)이 포함된다.

- 먼저 해야 할 일은 게시판 서비스의 그래들 build.gradle 파일에서 의존성을 설정하는 것이다. 게시판 서비스의 루트 디렉터리에 build.gradle 파일이 있으며, <code>스프링 클라우드 스트림 라이브러리</code>와 <code>스프링 클라우드 카프카 라이브러리</code> 이 두 개의 의존성을 추가해야 한다.

> implementation 'org.springframework.cloud:spring-cloud-stream'
> 
> implementation 'org.springframework.cloud:spring-cloud-starter-stream-kafka'

> board-service: build.gradle 

```groovy

...

dependencies {
  
    ...
  
	implementation 'org.springframework.cloud:spring-cloud-stream'
	implementation 'org.springframework.cloud:spring-cloud-starter-stream-kafka'
	...
}

...

```

- 의존성을 정의했다면 애플리케이션 스프링 클라우드 스트림의 메시지 브로커와 바인딩하도록 지정해야 한다. 이를 수행하려면 게시판 서비스의 부트스트랩 클래스인 BoardServiceApplication에 <code>@EnableBinding</code> 애너테이션을 추가한다.

---

## 스프링 클라우드 스트림 사용 사례: 분산 캐싱
