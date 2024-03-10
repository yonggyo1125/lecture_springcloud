# 서비스 디스커버리
> 분산 아키텍처에서는 호스트 이름과 머신이 위치한 IP 주소를 알아야 한다. 이 개념은 분산 컴퓨팅 초창기 때부터 존재했고 공식적으로 ‘서비스 디스커버리(service discovery)’로 알려져 있다. 서비스 디스커버리는 애플리케이션이 사용하는 모든 원격 서비스의 주소가 포함된 프로퍼티 파일을 관리하는 것처럼 단순하거나 UDDI(Universal Description, Discovery, and Integration) 저장소처럼 정형화된 것일 수 있다. 서비스 디스커버리는 다음 두 가지 핵심적인 이유로 마이크로서비스와 클라우드 기반 애플리케이션에 매우 중요하다.

- **수평 확장(horizontal scaling or scale out)**: 대개 이 패턴은 애플리케이션 아키텍처에서 클라우드 서비스 내 더 많은 서비스 인스턴스 및 컨테이너를 추가하는 것과 같은 조정을 요구한다.
- **회복성(resiliency)**: 이 패턴은 비즈니스에 영향을 주지 않고 아키텍처와 서비스 내부의 문제로 충격을 흡수하는 능력을 의미한다. 마이크로서비스 아키텍처에서는 한 서비스의 문제가 전체로 확산되어 서비스 소비자에게 전파되는 것을 막는데, 매우 신중해야 한다.

## 장점

- 첫째, **서비스 디스커버리를 사용하면 애플리케이션 팀은 해당 환경에서 실행 중인 서비스 인스턴스의 수를 빠르게 수평 확장할 수 있다는 장점**이 있다. 서비스 소비자에게 서비스의 물리적 위치는 추상화되어 있다. 즉, 서비스 소비자는 실제 서비스 인스턴스의 물리적 위치를 알지 못하기 때문에 새 서비스 인스턴스는 가용 서비스 풀(pool)에 추가되거나 제거될 수 있다.<br><br>서비스 소비자를 중단하지 않고 서비스를 확장하고 축소할 수 있는 이러한 능력은 매력적인 개념이다. 모놀리식과 싱글 테넌트(single-tenant)(예를 들어 한 고객을 위한) 애플리케이션을 구축하는 데 익숙한 개발 팀이 더 크고 좋은 하드웨어를 추가하는 수직 확장만이 유일한 확장 방식이라는 고정 관념에서 벗어나게 하고, 더 많은 서버를 추가하는 수평 확장을 더욱 강력한 접근 방식으로 인식하게 할 수 있다.<br><br>일반적으로 모놀리식 방식을 사용하면 개발 팀은 필요한 용량보다 초과 구매를 하게 된다. 이때 용량 증가는 큰 단위로 급증하지만 부드럽고 안정적으로 진행되는 경우는 드물다. 예를 들어 휴일 전 전자 상거래 사이트에서 증가하는 요청 수를 생각해 보자. 마이크로서비스를 사용하면 필요에 따라 새로운 서비스 인스턴스를 확장할 수 있다. 서비스 디스커버리(service discovery)를 사용하면 이러한 배포를 추상화하는 데 도움이 되며 서비스 소비자가 배포에 영향을 받지 않게 해 준다.
- 둘째, 서비스 디스커버리의 또 다른 장점은 **애플리케이션 회복성을 향상**시킨다는 것이다. 마이크로서비스 인스턴스가 비정상이거나 가용하지 못한 상태가 되면 대부분의 서비스 디스커버리 엔진은 그 인스턴스를 가용 서비스 목록에서 제거한다. 서비스 디스커버리 엔진은 사용이 불가한 서비스를 우회해서 라우팅하기 때문에 다운된 서비스로 입은 피해를 최소화한다.

## 서비스 위치 확인

- 애플리케이션이 여러 서버에 분산된 자원을 호출할 경우 이 자원들의 물리적 위치를 알고 있어야 한다. 클라우드가 아닌 환경에서 서비스 위치 확인(location resolution)은 대개 DNS와 네트워크 로드 밸런서의 조합으로 해결되었다. 이러한 전통적인 시나리오에서 애플리케이션은 조직의 다른 부분에 위치한 서비스를 호출해야 할 때 서비스의 고유 경로와 일반적인 DNS 이름을 사용하여 호출을 시도했다. DNS 이름은 F5 로드 밸런서(http://f5.com) 등 상용 로드 밸런서나 HAProxy(http://haproxy.org) 등 오픈 소스 로드 밸런서 위치로 정의된다.

![image1](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/4.%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%94%94%EC%8A%A4%EC%BB%A4%EB%B2%84%EB%A6%AC/images/1.png)
> DNS와 로드 밸런서를 사용하는 전통적인 서비스 위치 확인 모델

- 전통적 시나리오에서 서비스 소비자에게서 요청받은 로드 밸런서 라우팅 테이블 항목에는 서비스를 호스팅하는 한 개 이상의 서버 목록이 있다. 로드 밸런서는 이 목록에서 서버 하나를 골라 요청을 전달한다.
- 이러한 기존 방식에서 서비스 인스턴스는 한 개 이상의 애플리케이션 서버에 배포되었다. 애플리케이션 서버의 수는 대개 고정적이었고(예를 들어 서비스를 호스팅하는 애플리케이션 서버의 수가 늘거나 줄지 않았음) 영속적이었다(즉, 애플리케이션을 실행 중인 서버가 고장 나면 사용하던 것과 동일한 IP 주소와 구성으로 복구된다). 고가용성을 위해 유휴 상태의 보조 로드 밸런서는 핑(ping) 신호를 보내 주 로드 밸런서가 살아 있는지 확인했다. 주 로드 밸런서가 살아 있지 않다면 보조 로드 밸런서는 활성화되고, 주 로드 밸런서의 IP를 이어받아 요청을 처리했다.
- 이러한 모델은 사방이 벽으로 둘러싸인 회사 데이터 센터 안에서 실행되는 애플리케이션과 고정적인 서버에서 실행되는 비교적 적은 수의 서비스에서 잘 동작하지만 클라우드 기반의 마이크로서비스 애플리케이션에서는 잘 동작하지 않는다. 그 이유는 다음과 같다.
    - 고가용성 로드 밸런서를 만들 수 있더라도 전체 인프라스트럭처에서 보면 단일 장애 지점(single point of failure)이다: 로드 밸런서가 다운되면 이것에 의존하는 모든 애플리케이션도 다운된다. 로드 밸런서를 고가용성 있게 만들더라도 애플리케이션 인프라스트럭처 안에서는 중앙 집중식 관문이 될 가능성이 높다.
    - 서비스를 하나의 로드 밸런서 클러스터에 집중시키면 여러 서버에 부하를 분산하는 인프라스트럭처의 수평 확장 능력이 제한된다: 상용 로드 밸런서 다수는 이중화(redundancy) 모델과 라이선싱 비용이라는 두 가지 요소에 제약을 받는다.<br><br>대부분의 상용 로드 밸런서는 이중화를 위해 핫스왑(hot-swap) 모델을 사용하므로 로드를 처리할 서버 하나만 동작하고, 보조 로드 밸런서는 주 로드 밸런서가 다운된 경우 페일오버(failover)만을 위해 존재한다. 본질적으로 하드웨어 제약을 받는다. 또한 상용 로드 밸런서는 좀 더 가변적인 모델이 아닌 고정된 용량에 맞추어져 제한적인 라이선싱 모델을 갖는다.
    -  전통적인 로드 밸런서 대부분은 고정적으로 관리된다: 이 로드 밸런서들은 서비스를 신속히 등록하고 취소하도록 설계되지 않았고, 중앙 집중식 데이터베이스를 사용하여 경로 규칙을 저장한다. 대개 공급업체의 독점적인 API를 사용해야만 새로운 경로를 저장할 수 있다.
    - 로드 밸런서가 프록시(proxy) 역할을 하므로 서비스 소비자 요청은 물리적 서비스에 매핑되어야 한다: 이 변환 계층은 수동으로 서비스 매핑 규칙을 정의하고 배포해야 하므로 서비스 인프라스트럭처의 복잡성을 가중시킨다. 또한 전통적 로드 밸런서 시나리오에서는 새로운 서비스 인스턴스가 시작할 때 로드 밸런서에 등록되지 않는다.
- 이러한 네 가지 이유로 로드 밸런서를 비난하는 것은 아니다. 로드 밸런서는 중앙 집중식 네트워크 인프라스트럭처로 처리할 수 있는 대부분의 애플리케이션 크기와 규모를 가진 기업 환경에서 잘 작동한다. 그리고 로드 밸런서는 SSL 종료(SSL termination)를 처리하고 서비스 포트 보안을 관리하는 데 여전히 중요한 역할을 한다. 로드 밸런서는 뒷단의 모든 서버에 대한 들어오고 나가는(ingress and egress) 포트의 접근을 제한할 수 있다. 이러한 ‘최소 네트워크 접근(least network access)’ 개념은 종종 PCI(Payment Card Industry) 규정 준수처럼 산업 표준 인증에 대한 요구 사항을 충족하려고 할 때 중요한 요소가 된다.    
- 하지만 대규모 트랜잭션과 이중화를 처리해야 하는 클라우드에서는 중앙 집중식 네트워크 인프라스트럭처는 효율적으로 확장되지도 않고 비용 효율도 낮아서 제대로 작동하지 않는다. 이제 클라우드 기반 애플리케이션을 위해 견고한 서비스 디스커버리 메커니즘을 구현하는 방법을 살펴보자.

## 클라우드에서 서비스 디스커버리
> 클라우드 기반 마이크로서비스 환경에서 해결책은 다음과 같은 특성을 가진 서비스 디스커버리(service discovery) 메커니즘을 사용하는 것이다.

- **고가용성(highly available)**: 서비스 디스커버리는 서비스 디스커버리 클러스터의 노드 간 서비스 검색 정보가 공유되는 ‘핫(hot)’ 클러스터링 환경을 지원할 수 있어야 한다. 한 노드가 가용하지 않으면 클러스터 내 다른 노드가 그 역할을 대신해야 한다.<br><br>클러스터는 서버 인스턴스들의 그룹으로 정의할 수 있다. 이 경우 모든 인스턴스는 고가용성, 안정성, 확장성을 제공하고자 동일한 구성을 갖고 협업한다. 로드 밸런서와 통합된 클러스터는 서비스 중단을 방지하는 페일오버와 세션 데이터를 저장하는 세션 복제(replication) 기능을 제공할 수 있다.
- **P2P(Peer-to-Peer)**: 서비스 디스커버리 클러스터의 모든 노드는 서비스 인스턴스의 상태를 상호 공유한다.
- **부하 분산(load balanced)**: 서비스 디스커버리는 요청을 동적으로 분산시켜 관리하고 있는 모든 서비스 인스턴스에 분배해야 한다. 여러 면에서 서비스 디스커버리는 많은 초창기 웹 애플리케이션 구현에 사용되었던, 더 고정적이며 수동으로 관리되는 로드 밸런서를 대체한다.
- **회복성(resilient)**: 서비스 디스커버리 클라이언트는 서비스 정보를 로컬에 캐싱(caching)해야 한다. 로컬 캐싱은 서비스 디스커버리 기능이 점진적으로 저하되는 것을 고려했기 때문에 서비스 디스커버리 서비스가 가용하지 않아도 애플리케이션은 여전히 작동할 수 있고 로컬 캐시에 저장된 정보를 기반으로 서비스를 찾을 수 있다.
- **결함 내성(fault tolerant)**: 서비스 디스커버리가 비정상 서비스 인스턴스를 탐지하면 클라이언트 요청을 처리하는 가용 서비스 목록에서 해당 인스턴스를 제거해야 한다. 서비스를 이용하여 이러한 결함을 탐지하고 사람의 개입 없이 조치되어야 한다.

### 서비스 디스커버리 아키텍처

- 서비스 디스커버리를 논의하려면 다음 네 가지 개념을 이해해야 한다. 대개 이러한 일반적인 개념은 모든 서비스 디스커버리 구현체에 적용된다.
    - **서비스 등록(service registration)**: 서비스가 디스커버리 에이전트에 등록하는 방법이다.
    - **클라이언트의 서비스 주소 검색(client lookup of service address)**: 서비스 클라이언트가 서비스 정보를 검색하는 방법이다.
    - **정보 공유(information sharing)**: 노드 간 서비스 정보를 공유하는 방법이다.
    - **상태 모니터링(health monitoring)**: 서비스가 서비스 디스커버리에 상태를 전달하는 방법이다.
  
- 서비스 디스커버리의 주요 목표는 서비스의 물리적 위치를 수동으로 구성할 필요 없이 위치를 알려 줄 수 있는 아키텍처를 구축하는 것이다.
- 하기 그림에서는 앞서 설명한 네 가지 개념(서비스 등록, 서비스 주소 검색, 정보 공유, 상태 모니터링)의 흐름과 서비스 디스커버리 패턴을 구현할 때 흔히 발생하는 일을 볼 수 있다. 하기 그림에서는 한 개 이상의 서비스 디스커버리 노드가 시작되었다. 일반적으로 서비스 디스커버리 인스턴스 앞에는 로드 밸런서를 두지 않는다.

![image2](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/4.%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%94%94%EC%8A%A4%EC%BB%A4%EB%B2%84%EB%A6%AC/images/2.png)
> 서비스 인스턴스가 추가 제거될 때 서비스 디스커버리 에이전트를 업데이트해서 사용자 요청을 처리하도록 한다.

- 서비스 인스턴스는 시작할 때 서비스 검색 인스턴스가 자신을 액세스하는 데 사용할 물리적 위치, 경로, 포트를 등록한다. 서비스의 각 인스턴스에는 고유 IP 주소와 포트가 있지만 동일한 서비스 ID로 등록된다. 이때 서비스 ID는 동일한 서비스 인스턴스 그룹을 고유하게 식별하는 키일 뿐이다.
- 서비스는 일반적으로 하나의 서비스 디스커버리 인스턴스에만 등록된다. 대부분의 서비스 디스커버리 구현체는 서비스 인스턴스 관련 데이터를 클러스터 내 다른 노드에 전달하는 데이터 전파 방법으로 P2P(Peer-to-Peer) 모델을 사용한다. 전파 메커니즘은 서비스 구현체에 따라 하드코딩된 서비스 목록을 사용해서 전파하거나, gossip 같은 멀티캐스팅 프로토콜이나 전염식(infection-style) 프로토콜을 사용하여 클러스터에서 발생한 변경을 다른 노드가 ‘발견(discovery)’할 수 있게 한다.
- 마지막으로 각 서비스 인스턴스는 자기 상태를 서비스 디스커버리 서비스에 푸시(push)하거나 가져온다(pull). 정상 상태를 전달하지 못한 서비스는 가용 서비스 인스턴스 풀에서 제거된다. 서비스 디스커버리에 등록되고 나면 서비스는 자신의 기능을 이용해야 하는 애플리케이션이나 서비스를 사용할 준비가 된 것이다. 물론 클라이언트가 서비스를 발견할 수 있는 다른 모델도 있다.
- 맨 처음 접근 방법은 클라이언트가 오로지 서비스 디스커버리 엔진에만 완전히 의존하여 서비스를 호출할 때마다 서비스 위치를 확인하는 것이다. 이 방법을 사용하면 서비스 디스커버리 엔진은 등록된 마이크로서비스 인스턴스를 호출할 때마다 호출된다. 불행히도 이것은 서비스 클라이언트가 서비스를 찾고 호출하는 데 서비스 디스커버리 엔진에 완전히 의존하기 때문에 취약한 방법이다.
- 더욱 견고한 접근 방법은 <code>클라이언트 측 로드 밸런싱(client-side load balancing)</code>으로 알려진 방법을 사용하는 것이다. 이 메커니즘은 존(zone)별 또는 라운드 로빈(round robin) 같은 알고리즘을 사용하여 호출할 서비스의 인스턴스를 호출한다. ‘라운드 로빈 알고리즘식 로드 밸런싱’을 이야기할 때 우리는 클라이언트 요청을 여러 서버에 분산시키는 방법을 의미한다. 이 방법은 클라이언트 요청을 차례로 각 서버에 전달하는 것이다. 유레카(Eureka)와 함께 클라이언트 측 로드 밸런싱을 사용하는 장점은 서비스 인스턴스가 다운되면, 인스턴스가 레지스트리에서 제거된다는 것이다. 이 작업이 완료되면 클라이언트 측 로드 밸런서는 레지스트리 서비스와 지속적으로 통신하여 자동으로 레지스트리를 업데이트한다.

![image3](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/4.%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%94%94%EC%8A%A4%EC%BB%A4%EB%B2%84%EB%A6%AC/images/3.png)
> 클라이언트 측 로드 밸런싱은 서비스 위치를 캐싱하므로 서비스 클라이언트가 매 호출마다 서비스 디스커버리에 물어보지 않아도 된다.

- 이 모델에서 서비스를 소비하는 클라이언트는 서비스를 호출해야 할 때 다음 작업을 한다.
- 1. 서비스 소비자(클라이언트)가 요청하는 모든 인스턴스를 위해 디스커버리 서비스와 소통한 후 데이터를 서비스 소비자의 머신 로컬에 저장한다.
- 2. 클라이언트가 서비스를 호출할 때마다 서비스 소비자는 캐시에서 서비스 위치 정보를 검색한다. 일반적으로 클라이언트 측 캐싱은 서비스 호출이 여러 서비스 인스턴스에 분배되도록 라운드 로빈 부하 분산 알고리즘처럼 단순한 알고리즘을 사용한다.
- 3. 클라이언트는 주기적으로 서비스 디스커버리 서비스와 소통해서 서비스 인스턴스에 대한 캐시를 갱신한다. 클라이언트 캐시는 궁극적으로 일관적(eventually consistent)이지만 클라이언트가 서비스 디스커버리 인스턴스에 접속할 때 비정상 서비스 인스턴스를 호출할 위험은 항상 존재한다.

- 서비스를 호출하는 과정에서 서비스 호출이 실패하면 로컬에 있는 서비스 디스커버리 캐시가 무효화되고 서비스 디스커버리 클라이언트는 서비스 검색 에이전트에서 항목 갱신을 시도한다. 

### 스프링과 넷플릭스 유레카를 사용한 서비스 디스커버리

- 스프링 클라우드와 넷플릭스 유레카의 서비스 디스커버리 엔진을 사용하여 서비스 디스커버리 패턴을 구현한다. 클라이언트 측 로드 밸런싱을 위해 스프링 클라우드 로드 밸런서(Spring Cloud Load Balancer)를 사용한다.

![image4](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/4.%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%94%94%EC%8A%A4%EC%BB%A4%EB%B2%84%EB%A6%AC/images/4.png)
> 회원 서비스 및 게시판 서비스에 클라이언트 측 캐싱과 유레카 기능을 구현하면 유레카 서버에 대한 부하를 줄이고 유레카 서버가 가용하지 못할 때도 클라이언트 안정성을 높일 수 있다.

- 조직 서비스의 실제 위치는 서비스 디스커버리 레지스트리에 보관된다. 이 예에서 게시판 서비스의 두 인스턴스를 서비스 디스커버리 레지스트리에 등록한 후 클라이언트 측 로드 밸런싱을 사용하여 각 서비스 인스턴스에서 레지스트리를 검색하고 캐시한다.
  - 1. 서비스 부트스트래핑 시점에 회원 및 게시판 서비스는 유레카 서비스에 등록한다. 이 등록 과정에서 시작하는 서비스의 서비스 ID와 해당 서비스 인스턴스의 물리적 위치 및 포트 번호를 유레카에 알려 준다.
  - 2. 회원 서비스가 게시판 서비스를 호출할 때 스프링 클라우드 로드 밸런서를 사용하여 클라이언트 측 로드 밸런싱을 제공한다. 이 로드 밸런서는 유레카 서비스에 접속하여 서비스 위치 정보를 검색하고 로컬에 캐시한다.
  - 3. 스프링 클라우드 로드 밸런서는 유레카 서비스를 주기적으로 핑(ping)해서 로컬 캐시의 서비스 위치를 갱신한다.

- 이제 새로운 회원 서비스 인스턴스는 게시판 서비스의 로컬에서 볼 수 있고 비정상 인스턴스는 로컬 캐시에서 제거된다. 

## 스프링 유레카 서비스 구축

> 스프링 클라우드 컨피그 서비스처럼 스프링 클라우드 유레카 서비스를 설정하려면 스프링 부트 프로젝트를 생성하고 애너테이션과 구성을 적용해야 한다. Spring Initializr(https://start.spring.io/)에서 프로젝트를 생성하고 시작해 보자.

![image5](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/4.%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%94%94%EC%8A%A4%EC%BB%A4%EB%B2%84%EB%A6%AC/images/5.png)

> 추가 의존성 Spring Cloud Starter Bootstrap 을 build.gradle에 다음과 같이 추가한다.
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
	implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-server'
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

- 다음 단계는 앞서 생성된 스프링 컨피그 서버에서 구성을 검색하는 데 필요한 설정이며 src/main/resources/bootstrap.yml 파일에 구성한다. 또한 기본 클라이언트 측 로드 밸런서에 리본(Ribbon)을 비활성화하는 구성을 추가해야 한다. 다음 코드는 bootstrap.yml 파일의 내용을 보여 준다.

> src/main/resources/bootstrap.yml

```yaml
spring:
  application:
    name: eureka-server  # 스프링 클라우드 컨피그 클라이언트가 찾고 있는 서비스를 일 수 있도록 유레카 서비스의 이름을 지정한다.
  cloud:
    config:
      uri: http://localhost:8071  # 스프링 클라우드 컨피그 서버의 위치를 지정한다.
  loadbalancer:   #  여전히 리본이 클라이언트 측 기본 로드 밸런서이므로 loadbalancer.ribbon.enabled를 사용하여 리본을 비활성화한다.
    ribbon:
      enabled: false
```

- 유레카 서버의 bootstrap.yml 파일에 스프링 컨피그 서버 정보를 추가하고 리본을 로드 밸런서에서 비활성화하면 다음 단계로 이동할 수 있다.
- 다음 단계에서는 스프링 컨피그 서버에서 유레카 서비스를 독립형 모드(클러스터에 다른 노드들이 없는)로 실행되도록 설정하는 데 필요한 구성을 추가한다.
- 이를 위해 스프링 컨피그 서비스의 저장소에 유레카 서버 구성 파일을 생성해야 한다. 이 저장소에는 클래스패스(classpath), 파일 시스템, 깃, 볼트(Vault)를 지정할 수 있다는 것을 기억하자. 구성 파일 이름을 이전에 유레카 서비스의 bootstrap.yml 파일에 정의된 spring.application.name 프로퍼티로 지정해야 한다. 이 예제의 목적에 따라 classpath/configserver/src/main/resources/config/eureka-server.yml 파일을 생성한다.

> 스프링 클라우드 컨피그 서버 : src/main/resources/config/eureka-server.yml 

```yaml
server:
  port: 8070   # 유레카 서버의 수신 포트를 설정한다.
eureka:
  instance:
    hostname: eurekaserver   # 유레카 인스턴스의 호스트 이름을 설정한다.
  client:
    registerWithEureka: false   # 컨피그 서버가 유레카 서비스에 등록하지 않도록 지시한다.
    fetchRegistry: false   # 컨피그 서버가 캐시 레지스트리 정보를 로컬에 캐시하지 않도록 지시한다.
    serviceUrl:
      defaultZone:   # 서비스 URL을 제공한다.
        http://${eureka.instance.hostname}:${server.port}/eureka/
    server:
      waitTimeInMsWhenSyncEmpty: 30000   # 서비스가 요청을 받기 전 초기 대기 시간을 설정한다.
```

- **server.port**: 기본 포트를 설정한다.
- **eureka.instance.hostname**: 유레카 서비스의 인스턴스 호스트 이름을 설정한다.
- **eureka.client.registerWithEureka**: 스프링 부트로 된 유레카 애플리케이션이 시작할 때 컨피그 서버를 유레카에 등록하지 않도록 설정한다.
- **eureka.client.fetchRegistry**: 이 값을 false로 지정하면 유레카 서비스가 시작할 때 레지스트리 정보를 로컬에 캐싱하지 않도록 설정한다. 유레카 클라이언트를 실행할 때 유레카에 등록할 스프링 부트 서비스를 위한 이 값을 변경할 수 있다.
- **eureka.client.serviceUrl.defaultZone**: 모든 클라이언트에 대한 서비스 URL을 제공한다. URL은 eureka.instance.hostname과 server.port 프로퍼티 두 개의 조합으로 되어 있다.
- **eureka.server.waitTimeInMsWhenSyncEmpty**: 서버가 요청을 받기 전 대기 시간을 설정한다.

- 마지막 프로퍼티인 <code>eureka.server.waitTimeInMsWhenSyncEmpty</code>는 시작하기 전 대기할 시간을 밀리초로 나타낸다. 서비스를 로컬에서 테스트할 때 유레카가 등록된 서비스를 바로 알리지 않기 때문에 이 프로퍼티를 사용해야 한다. 기본적으로 모든 서비스에 등록할 기회를 주고자 유레카가 알리기 전에 5분을 기다린다. 로컬 테스트에서 이 프로퍼티를 사용하면 유레카 서비스를 시작하고 등록된 서비스를 표시하는 데 걸리는 시간을 단축하기에 유용하다.

> 유레카에 등록된 서비스가 표시되는 데 최대 30초가 소요된다. 유레카는 서비스를 사용할 준비가 되었다고 알리기 전에 10초 간격으로 연속 3회 ping을 보내서 상태 정보를 확인해야 하기 때문이다. 서비스를 배포하고 테스트할 때 이 점을 고려하기 바란다.

- 유레카 서비스를 위한 마지막 설정 작업은 서비스를 시작하는 데 사용되는 애플리케이션의 부트스트랩 클래스에 애너테이션을(@EnableEurekaServ) 추가하는 것이다.

> src/main/java/.../EurekaServerApplication.java

```java
package org.choongang.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer  // 스프링 서비스에서 유레카 서버를 활성화한다.
public class EurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
	}

}
```

- 여기에서는 새로운 @EnableEurekaServer 애너테이션만 사용하여 해당 서비스에서 유레카 서비스를 활성화한다. 이제
- 유레카 서비스를 시작한다. 시작 명령을 실행하면 등록된 서비스가 없는 유레카 서비스가 실행될 것이다. 따라서 유레카 애플리케이션 구성이 설정된 스프링 컨피그 서비스를 먼저 실행해야 한다. 컨피그 서비스를 먼저 실행하지 않으면 다음 에러가 발생한다.

```
Connect Timeout Exception on Url - http://localhost:8071. 
Will be trying the next url if available.
    com.sun.jersey.api.client.ClientHandlerException:
    java.net.ConnectException: Connection refused (Connection refused)
```

## 게시판 서비스 구성

- Spring Initializr(https://start.spring.io/)에서 프로젝트를 생성하고 시작해 보자.

![image6](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/4.%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%94%94%EC%8A%A4%EC%BB%A4%EB%B2%84%EB%A6%AC/images/6.png)

> build.gradle에 다음 의존성을 및 queryDSL 설정을 추가한다.
> implementation 'org.springframework.cloud:spring-cloud-starter-bootstrap'
> implementation 'com.querydsl:querydsl-jpa:5.1.0:jakarta'
> annotationProcessor 'com.querydsl:querydsl-apt:5.1.0:jakarta'
> annotationProcessor 'jakarta.persistence:jakarta.persistence-api:3.1.0'
> annotationProcessor 'jakarta.annotation:jakarta.annotation-api:2.1.1'

```groovy
plugins {
  id 'java'
  id 'org.springframework.boot' version '3.2.3'
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
  implementation 'org.springframework.boot:spring-boot-starter-validation'
  implementation 'org.springframework.boot:spring-boot-starter-web'
  implementation 'org.springframework.cloud:spring-cloud-starter-config'
  implementation 'org.springframework.cloud:spring-cloud-starter-bootstrap'
  implementation 'com.querydsl:querydsl-jpa:5.1.0:jakarta'
  annotationProcessor 'com.querydsl:querydsl-apt:5.1.0:jakarta'
  annotationProcessor 'jakarta.persistence:jakarta.persistence-api:3.1.0'
  annotationProcessor 'jakarta.annotation:jakarta.annotation-api:2.1.1'

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

> src/main/resources/bootstrap.yml 

```yaml
spring:
  application:
    name: board-service
  profiles:
    active: dev
  cloud:
    config:
      uri: http://localhost:8071
```

> 스프링 클라우드 컨피그 서버 : src/main/resources/config/board-service.yml

```yaml
server:
  port: 8081

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

> 스프링 클라우드 컨피그 서버 : src/main/resources/config/board-service-dev.yml

```yaml
spring:
  datasource:
    driverClassName: oracle.jdbc.driver.OracleDriver
    url: jdbc:oracle:thin:@localhost:1521:orcl
    username: PROJECT
    password: '{cipher}4b93b6c7060f269410654622559659db1e9049bd829a15521df43b825e6a860d'
```

> 스프링 클라우드 컨피그 서버 : src/main/resources/config/board-service-test.yml

```yaml
spring:
  datasource:
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:test
    username: sa
    password:
```

- 스프링 클라우드 컨피그 서버 구성 서버를 재시작합니다.
- 웹 브라우저 주소창에 http://localhost:8071/board-service/default 를 입력하면 board-service.yml 파일에 포함된 모든 속성과 함께 반환된 JSON으로 표시된다.

![image7](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/4.%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%94%94%EC%8A%A4%EC%BB%A4%EB%B2%84%EB%A6%AC/images/7.png)


- 게시판 서비스 소스 구성

> src/main/java/.../entity/Base.java

```java
package org.choongang.board.entity;

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

> src/main/java/.../entity/Board.java

```java
package org.choongang.board.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@NoArgsConstructor @AllArgsConstructor
public class Board extends Base {
    @Id
    @Column(length=40)
    private String bid; // 게시판 아이디

    @Column(length=90, nullable = false)
    private String bName; // 게시판명
    private boolean active; // 사용 여부

    @Lob
    private String category; // 게시판 분류
}
```

> src/main/java/.../repository/BoardRepository.java

```java
package org.choongang.board.repository;

import org.choongang.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface BoardRepository extends JpaRepository<Board, String>, QuerydslPredicateExecutor<Board> {
}
```

> src/main/java/.../config/MvcConfig.java

```java
package org.choongang.board.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableJpaAuditing
public class MvcConfig implements WebMvcConfigurer {
    
}
```

> src/main/java/.../controller/BoardController.java

- 유레카 서비스 등록을 위한 임시 데이터 생성

```java 
package org.choongang.board.controller;

import org.choongang.board.entity.Board;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/v1/board")
public class BoardController {

    @GetMapping
    public List<Board> list() {
        List<Board> items = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> Board.builder()
                        .bid("board" + i)
                        .bName("게시판" + i)
                        .build())
                .toList();

        return items;
    }
}

```

## 스프링 유레카에 서비스 등록

- 회원 및 게시판 서비스가 유레카 서버에 등록할 수 있도록 구성한다. 이 작업은 서비스 클라이언트가 유레카 레지스트리에서 서비스를 검색하려고 수행한다.

- 회원 및 게시판 서비스의 build.gradle에 스프링 유레카 의존성을 추가하는 것이다. 
> src/main/resources/build.gradle 
>
> implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'

```yaml
...
  
dependencies {
    ...
  
	implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
  
    ...
}
  
...
```

- spring-cloud-starter-netflix-eureka-client 산출물에는 스프링 클라우드가 유레카 서비스와 상호 작용하는 데 필요한 JAR 파일들이 있다. build.gradle 파일을 설정한 후 등록하려는 서비스의 bootstrap.yml 파일에 spring.application.name을 설정했는지 확인해야 한다.

- 유레카에 등록된 모든 서비스는 애플리케이션 ID와 인스턴스 ID라는 두 가지 구성 요소와 연관되어 있다. 애플리케이션 ID는 서비스 인스턴스의 그룹을 나타낸다. 스프링 부트 마이크로서비스에서 애플리케이션 ID는 항상 spring.application.name 프로퍼티에서 설정된 값이다. 게시판 서비스는 이 프로퍼티 값을 board-service로 지정했고, 회원 서비스는 member-service로 지정했다. 인스턴스 ID는 각 서비스 인스턴스를 나타내고자 무작위로 자동 생성된 숫자다.

- 다음으로 스프링 부트가 조직 서비스와 라이선싱 서비스를 유레카에 등록하도록 만들어야 한다. 등록을 위해서는 스프링 컨피그 서비스에서 관리하는 서비스의 구성 파일에 구성 정보를 추가한다. 이 구성 파일은 스프링 컨피그 서버 프로젝트의 다음 파일 두 개다.

> src/main/resources/config/board-service.yml

```yaml

...

eureka:
  instance:
    preferIpAddress: true
  client:
    registerWithEureka: true
    fetchRegistry: true
    serviceUrl:
      defaultZone: http://localhost:8070/eureka/
```
 
> src/main/resources/config/member-service.yml

```yaml
...

eureka:
  instance:
    preferIpAddress: true
  client:
    registerWithEureka: true
    fetchRegistry: true
    serviceUrl:
      defaultZone: http://localhost:8070/eureka/
```

>  IP 주소를 선호하는 이유
>
> 기본적으로 유레카는 호스트 이름(hostname)을 사용하여 접속하는 서비스를 등록한다. 이것은 서비스가 DNS 기반의 호스트 이름으로 할당되는 서버 기반 환경에서 잘 작동한다. 그러나 컨테이너 기반의 배포 환경(예 도커)에서 컨테이너는 DNS 엔트리가 임의로 생성한 호스트 이름을 할당해서 시작된다. eureka.instance.preferIpAddress를 true로 설정하지 않는다면 클라이언트 애플리케이션은 해당 컨테이너에 대한 DNS 엔트리가 없어 호스트 이름의 위치를 제대로 얻지 못한다. preferIpAddress 프로퍼티를 설정하면 클라이언트가 IP 주소로 전달받도록 유레카에 알려 준다.
> 
> 개인적으로 이 프로퍼티를 항상 true로 설정한다. 클라우드 기반의 마이크로서비스는 일시적(ephemeral)이고 무상태형(stateless)이므로 자유롭게 시작하고 종료할 수 있다. 따라서 IP 주소가 이러한 유형의 서비스에는 더 적합하다.

- <code>eureka.client.registerWithEureka</code> 프로퍼티는 조직 및 라이선싱 서비스가 유레카에 등록하도록 지시한다. <code>eureka.client.fetchRegistry</code> 프로퍼티는 스프링 유레카 클라이언트에 레지스트리의 로컬 복사본을 가져오도록 지시한다. 이 값을 true로 설정하면 레지스트리를 검색할 때마다 유레카 서비스를 호출하는 대신 레지스트리를 로컬에 캐싱하고, 클라이언트 소프트웨어는 30초마다 유레카 서비스에 레지스트리 변경 사항을 확인한다.

- 마지막 프로퍼티인 eureka.client.serviceUrl.defaultZone은 클라이언트가 서비스 위치를 확인하는 데 사용하는 유레카 서비스 목록으로, 쉼표(,)로 구분해서 추가할 수 있다. 각 서비스의 부트스트랩 파일에서 이전에 정의한 모든 프로퍼티의 키-값을 선언할 수 있지만, 목표는 구성 설정을 스프링 컨피그 서비스에 위임하는 것이다. 그래서 스프링 컨피그 서비스 저장소의 서비스 구성 파일에 모든 구성 정보를 등록하는 것이다. 지금까지 이 서비스들의 부트스트랩 파일에는 애플리케이션 이름, 프로파일(필요한 경우)과 스프링 클라우드 컨피그 URI만 포함되었다.


### 유레카 REST API

- 현재 유레카 서비스에 두 개의 서비스가 등록되어 있다. 유레카의 REST API 또는 유레카 대시보드를 사용하여 레지스트리 내용을 볼 수 있다.
- REST API로 서비스의 모든 인스턴스를 보려면 다음 GET 엔드포인트를 호출하라.

```
http://<eureka service>:8070/eureka/apps/<APPID>
```

- 예를 들어 레지스트리의 조직 서비스를 보려면 http://localhost:8070/eureka/apps/member-service 엔드포인트를 호출할 수 있다.

![image8](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/4.%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%94%94%EC%8A%A4%EC%BB%A4%EB%B2%84%EB%A6%AC/images/8.png)

- 유레카에서 반환되는 기본 데이터 형식은 XML이다. 따라서 유레카는 그림 6-6의 데이터를 JSON 페이로드로 반환받으려면 Accept HTTP 헤더를 application/json으로 설정해야 한다.

![image9](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/4.%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%94%94%EC%8A%A4%EC%BB%A4%EB%B2%84%EB%A6%AC/images/9.png)

### 유레카 대시보드 

- 유레카 서비스가 시작되면 브라우저에서 http://localhost:8070으로 이동하여 유레카 대시보드를 볼 수 있다. 유레카 대시보드를 통해 서비스 등록 상태를 볼 수 있다.

![image10](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/4.%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%94%94%EC%8A%A4%EC%BB%A4%EB%B2%84%EB%A6%AC/images/10.png)

> 등록된 회원 및 게시판 서비스를 보여주는 유레카 대시보드

> 서비스가 유레카에 등록될 때 유레카는 서비스가 사용 가능해질 때까지 30초 동안 연속 세 번의 상태를 확인하며 대기한다. 이 워밍업 대기 시간 때문에 개발자가 서비스가 시작된 직후 서비스를 호출하면 유레카는 그 서비스가 등록되지 않은 것으로 혼동할 수 있었다.
> 
> 이러한 현상은 유레카 서비스와 애플리케이션 서비스(라이선싱 및 조직 서비스)가 모두 동시에 시작하기 때문에 도커 환경에서 실행되는 코드 예제에서도 확인할 수 있다. 따라서 애플리케이션이 시작한 후 서비스 자체가 시작되었음에도 서비스를 찾을 수 없다는 404 에러를 수신할 수 있다는 점을 인지해야 한다. 이때는 서비스를 호출하기 전에 30초를 기다려야 한다.
>
> 하지만 운영 환경에서는 유레카 서비스가 이미 실행 중일 것이므로 기존 서비스를 배포하더라도 이전 서비스가 요청을 받을 수 있도록 유지된다.


## 서비스 디스커버리를 이용한 서비스 검색

- 회원 서비스가 게시판 서비스 위치를 직접적으로 알지 못해도 게시판 서비스를 호출할 수 있는 방법을 설명한다. 회원 서비스는 유레카를 이용하여 회원 서비스의 물리적 위치를 검색한다.

- 서비스 디스커버리를 위해 서비스 소비자가 <code>스프링 클라우드 로드 밸런서(Spring Cloud Load Balancer)</code>와 상호 작용할 수 있는 세 가지 다른 스프링/넷플릭스 클라이언트 라이브러리를 살펴볼 것이다. 이를 위해 로드 밸런서와 상호 작용하고자 추상화 수준이 가장 낮은 단계에서 높은 단계의 라이브러리로 이동할 것이다. 검토할 라이브러리는 다음과 같다.
  - 스프링 Discovery Client
  - REST 템플릿을 사용한 스프링 Discovery Client

### 스프링 Discovery Client로 서비스 인스턴스 검색

- 스프링 <code>Discovery Client</code>는 <code>로드 밸런서(Spring Cloud Load Balancer)</code>와 그 안에 등록된 서비스에 대해 가장 낮은 수준으로 접근할 수 있다. 즉, Discovery Client를 사용하면 스프링 클라우드 로드 밸런서 클라이언트에 등록된 모든 서비스와 해당 URL을 쿼리할 수 있다.
- 다음으로 Discovery Client를 통해 로드 밸런서에서 게시판 서비스 URL 중 하나를 검색한 후 표준 RestTemplate 클래스를 사용하여 서비스를 호출하는 간단한 예제를 만들 것이다.
- 디스커버리 클라이언트를 사용하려면 먼저 다음 코드처럼 디스커버리 클라이언트를 사용하려면 먼저 다음 코드처럼 <code>@EnableDiscoveryClient</code> 애너테이션을 추가해야 한다.

> member-service : src/main/java/.../MemberServiceApplication.java

```java
package org.choongang.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@SpringBootApplication
@RefreshScope
@EnableDiscoveryClient  // 유레카 Discovery Client를 활성화한다.
public class MemberServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MemberServiceApplication.class, args);
	}

}
```

- <code>@EnableDiscoveryClient</code>는 스프링 클라우드에서 애플리케이션이 Discovery Client 및 스프링 클라우드 로드 밸런서 라이브러리를 사용할 수 있게 한다.
- Discovery Client를 사용한 정보 검색하기

> member-service : src/main/java/.../service/client/Board.java

```java 
package org.choongang.member.service.client;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Board {
    private String bid; // 게시판 아이디

    private String bName; // 게시판명
    private boolean active; // 사용 여부

    private String category; // 게시판 분류

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
```

> member-service : src/main/java/.../service/client/BoardDiscoveryClient.java

```java
package org.choongang.member.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
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

    public List<Board> getBoards() {
        RestTemplate restTemplate = new RestTemplate();
        List<ServiceInstance> instances = discoveryClient.getInstances("board-service"); // 조직 서비스의 모든 인스턴스 리스트를 얻는다.

        if (instances == null || instances.isEmpty()) {
            return null;
        }

        String serviceUri = String.format("%s/api/v1/board", instances.get(0).getUri().toString());

        ResponseEntity<String> exchange = restTemplate.exchange(
                serviceUri,
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
```

- 코드에서 먼저 살펴볼 것은 <code>DiscoveryClient</code> 클래스다. 이 클래스를 사용하여 스프링 클라우드 로드 밸런서와 상호 작용한다. 그다음 유레카에 등록된 조직 서비스의 모든 인스턴스를 검색하려면 <code>getInstances()</code> 메서드를 사용하고, ServiceInstance 객체 리스트를 얻어 오기 위해 찾으려는 서비스 키를 전달한다. <code>ServiceInstance</code> 클래스는 호스트 이름, 포트, URI 같은 서비스의 인스턴스 정보를 보관한다.
- 리스트에서 첫 번째 <code>ServiceInstance</code> 클래스를 사용하여 서비스를 호출하는 데 쓸 수 있는 대상 URL을 만든다. 대상 URL이 만들어지면 표준 스프링 RestTemplate으로 조직 서비스를 호출하고 데이터를 조회할 수 있다.

### Discovery Client와 현실

- 어떤 서비스와 서비스 인스턴스가 등록되어 있는지 확인하기 위해 로드 밸런서에 쿼리해야 할 때만 Discovery Client를 사용해야 한다. 코드에는 다음 몇 가지 문제가 있다.
  - **스프링 클라우드 클라이언트 측 로드 밸런서를 이용하지 못한다**: Discovery Client를 직접 호출하면 서비스 리스트를 얻게 되지만, 호출할 서비스 인스턴스를 선정할 책임은 사용자에게 있다.
  - **너무 많은 일을 한다**: 코드에서 서비스를 호출하는 데 사용될 URL을 생성해야 한다. 이것은 작은 일이지만 코드를 적게 작성하면 디버그할 코드가 줄어든다.

- 눈치 빠른 스프링 개발자는 코드에서 RestTemplate 클래스를 직접 인스턴스화했다는 것을 알아챘을 것이다. 이것은 일반적인 스프링 REST 호출과 대립되는데, 보통 스프링 프레임워크는 @Autowired 애너테이션을 통해 RestTemplate을 주입하기 때문이다.
- 코드에서 볼 수 있듯이 우리는 RestTemplate 클래스의 인스턴스를 생성했다. @EnableDiscoveryClient를 통해 애플리케이션 클래스에서 스프링 Discovery Client를 활성화했다면, 스프링 프레임워크가 관리하는 모든 REST 템플릿(template)은 해당 인스턴스에 로드 밸런서가 활성화된 인터셉터(interceptor)를 주입한다. 이렇게 되면 RestTemplate 클래스로 URL을 생성하는 방식이 변경되는데, 직접적으로 RestTemplate을 인스턴스로 만들면 이 변경을 피할 수 있다.

### 로드 밸런서를 지원하는 스프링 REST 템플릿으로 서비스 호출

-  로드 밸런서를 지원하는 RestTemplate 클래스를 사용하려면 스프링 클라우드의 <code>@LoadBalanced</code> 애너테이션으로 RestTemplate 빈(bean)을 정의해야 한다.

> member-service : src/main/java/config/BeanConfig.java

```java
...
@Configuration
public class BeanConfig {
  ...

  @Bean
  @LoadBalanced
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
```

- 로드 밸런서를 지원하는 RestTemplate을 사용한 서비스 호출하기

> member-service : src/main/java/.../service/client/BoardDiscoveryClient.java

```java
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
                "http://board-service/api/v1/board",  // 로드 밸런서 지원 RestTemplate를 사용할 때 유레카 서비스 ID로 대상 URL을 생성한다.
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
```

- 이 코드는 이전 예제 코드와 다소 비슷하게 보이지만 두 가지 큰 차이점이 있다. 첫째, 스프링 클라우드 Discovery Client가 없어졌다. 둘째, restTemplate.exchange() 호출에 사용된 URL이 이상하게 보일 것이다.

```java
 ResponseEntity<String> exchange = restTemplate.exchange(  // 서비스 호출을 위해 표준 스프링 RestTemplate 클래스를 사용한다.
                "http://board-service/api/v1/board",  // 로드 밸런서 지원 RestTemplate를 사용할 때 유레카 서비스 ID로 대상 URL을 생성한다.
                HttpMethod.GET,
                null,
                String.class);
```

- URL에서 서버 이름은 유레카에 조직 서비스를 등록할 때 사용된 조직 서비스 키의 애플리케이션 ID와 일치한다.

```
http://board-service/api/v1/board
```

- 로드 밸런서를 지원하는 RestTemplate 클래스는 전달된 URL을 파싱하고 서버 이름으로 전달된 것을 키로 사용하여 서비스의 인스턴스를 로드 밸런서에 쿼리한다. **실제 서비스 위치와 포트는 개발자에게 완전히 추상화된다.** 게다가 RestTemplate 클래스를 사용하면 **스프링 클라우드 로드 밸런서는 서비스 인스턴스에 대한 모든 요청을 라운드 로빈 방식으로 부하 분산**한다.


> member-service : src/main/java/.../controller/MemberController.java

```java
...

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {
  ...

  private final BoardDiscoveryClient boardDiscoveryClient;
  
  ...

  @GetMapping("/board")
  public List<Board> getBoards() {
    return boardDiscoveryClient.getBoards();
  }

  ...
  
}
```

![image11](https://raw.githubusercontent.com/yonggyo1125/lecture_springcloud/master/4.%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EB%94%94%EC%8A%A4%EC%BB%A4%EB%B2%84%EB%A6%AC/images/11.png)