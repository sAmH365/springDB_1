# JDBC


### 예제 스키마
<details>
<summary>트랜잭션의 이해</summary>

```
 drop table member if exists;
 create table member (
    member_id varchar(10),
    money integer not null default 0,
 primary key (member_id)
 );

-- 자동커밋모드
 set autocommit true; //자동 커밋 모드 설정
insert into member(member_id, money) values ('data1',10000); //자동 커밋
insert into member(member_id, money) values ('data2',10000); //자동 커밋

-- 수동커밋모드
set autocommit false; //수동 커밋 모드 설정
insert into member(member_id, money) values ('data3',10000);
 insert into member(member_id, money) values ('data4',10000);
 commit; //수동 커밋
 
 -- 데이터 초기화 SQL
  //데이터 초기화
set autocommit true;
 delete from member;
 insert into member(member_id, money) values ('oldId',10000);
 
 -- 계좌이체 예제 초기화
 set autocommit true;
 delete from member;
 insert into member(member_id, money) values ('memberA',10000);
 insert into member(member_id, money) values ('memberB',10000);
 
 -- 계좌이체 실행 SQL -성공
  set autocommit false;
 update member set money=10000 - 2000 where member_id = 'memberA';
 update member set money=10000 + 2000 where member_id = 'memberB';
 
 -- 계좌이체 실행 SQL - 오류
  set autocommit false;
 update member set money=10000 - 2000 where member_id = 'memberA'; //성공
update member set money=10000 + 2000 where member_iddd = 'memberB'; //쿼리 예외 발생
```
</details>

<details>
<summary>DB락 - 변경</summary>

```
-- 기본데이터
set autocommit true;
delete from member;
insert into member(member_id, money) values ('memberA',10000);


-- 세션1
set autocommit false;
update member set money=500 where member_id = 'memberA';

-- 세션2
SET LOCK_TIMEOUT 60000;
set autocommit false;
update member set money=1000 where member_id = 'memberA';

```
</details>

<details>
<summary>DB락 - 조회</summary>

```
-- 기본데이터
set autocommit true;
delete from member;
insert into member(member_id, money) values ('memberA',10000);

-- 세션1
set autocommit false;
select * from member where member_id='memberA' for update;

-- 세션2
set autocommit false;
update member set money=500 where member_id = 'memberA';

```
</details>
<details>
<summary>스프링 문제해결 - 트랜잭션 추상화</summary>

```
-- JPA 트랜잭션 코드 예시
 public static void main(String[] args) {
 //엔티티 매니저 팩토리 생성
EntityManagerFactory emf = 
Persistence.createEntityManagerFactory("jpabook");
 EntityManager em = emf.createEntityManager(); //엔티티 매니저 생성
EntityTransaction tx = em.getTransaction(); //트랜잭션 기능 획득
try {
        tx.begin(); //트랜잭션 시작
logic(em);  //비즈니스 로직
        tx.commit();//트랜잭션 커밋
    } 
    } 
}
 catch (Exception e) {
        tx.rollback(); //트랜잭션 롤백
finally {
        em.close(); //엔티티 매니저 종료
    }
    emf.close(); //엔티티 매니저 팩토리 종료
```
</details>

## JDBC 이해
<details>
<summary>JDBC 표준 인터페이스</summary>

* JDBC 표준 인터페이스
  * 연결 - java.sql.Connection
  * SQL을 담은 내용 - java.sql.Statement
  * SQL요청 응답 - java.sql.ResultSet
<br/>
* JDBC 드라이버
  * JDBC인터페이스들을 DB벤더들이 각자의 방식으로 구현해 놓음
  * Oracle 드라이버, MySql드라이버
</details>

<details>
<summary>커넥션 풀</summary>

* 문제점
  - 데이터 베이스는 커넥션을 매번 획득, TCP/IP 커넥션을 새로 생성하기 위한 리소스를 매번 사용해야 한다.
  - 고객이 서비스를 사용할 때 SQL 실행시간 + 커넥션 생성 시간 까지 추가됨
* 커넥션 풀
  - 애플리케이션 시작시점에 커넥션 풀을 필요한 만큼 미리 생성 (기본값은 보통 10)
  - 커넥션 반환시 커넥션을 종료하는 것이 아니라, 커넥션이 살아있는 상태로 반환
  - 직접 만들 수 도 있지만 성능, 사용성 고려하면 오픈 소스 커넥션풀을 사용
  - **대표 커넥션 풀**
    - HikariCP (이것만 거의 사용)
    - tomcat-jdbc pool

</details>

<details>
<summary>DataSource</summary>

* 커넥션 획득 방법
  * JDBC DriverManager사용
  * 커넥션 풀 사용
* 커넥션 획득 방법을 추상화 해서 사용
  * DataSoruce는 *커넥션 획득 방법*을 추상화 하는 인터페이스다 
  * 커넥션 얻어오는 방법을 바꿔도 코드의 수정이 발생하지 않음
  * 구현체마다 커넥션 얻어오는 방법 구현
* 정리
  * 자바는 DataSource를 통해 커넥션 획득방법을 추상화 했다.
  * 애플리케이션 로직은 DataSource 인터페이스에만 의존하면 됨 -> DriverManagerDataSource를 통해서 DriverManager를 사용하다가 커넥션 풀을 사용하도록 코드를 변경해도 애플리케이션 로직은 변경하지 않아도 된다.
  * 설정과 사용의 분리
    * 설정: DataSource를 만들고 필요한 속성을 사용해서 URL, USERNAME, PASSWORD같은 부분을 입력하는 것을 말함
    * 사용: 설정은 신경쓰지않고 DataSource의 getConnection()만 호출해서 사용
  * 설정과 사용을 분리함에따라 객체를 설정하는 부분과, 사용하는 부분을 명확하게 분리 가능
* 커넥션 풀 사용
  * 커넥션풀 크기 지정(setMaximumPoolSize)
  * 커넥션풀에서 커넥션 생성하는 작업은 애플리케이션에 영향을 주지 않기 위해 별도의 커넥션에서 실행된다. 별도의 쓰레드에서 동작하기 때문에 테스트가 먼저 종료되어 버린다. Thread.sleep을 통해 대기시간을 주어야 쓰레드 풀에 커넥션이 생성되는 로그를 확인 가능하다.
  * 스프링부트 3.1 이상에서의 HikariPool 사용
    * 3.1버전 이상부터 HikariCP가 기본 로그레벨을 INFO로 설정하기 때문에 간단한 로그만 표현된다.
    * DEBUG레벨의 로그를 보기위해 src/main/resources/logback.xml 파일 추가 필요
</details>

## 트랜잭션 이해
<details>
<summary>트랜잭션 개념</summary>

* ACID
  * 원자성
    * 트랜잭션내의 실행한 작업들은 하나의 작업을 실행한것 처럼 모두 성공하거나 모두 실패해야한다.
  * 일관성
    * 모든 트랜잭션은 일관성 있는 데이터베이스 상태를 유지해야한다.
      * ex: 데이터 베이스에서 정한 무결성 제약 조건을 항상 만족해야함
  * 격리성
    * 동시에 실행되는 트랜잭션은 서로의 연산에 영향을 주지 못한다.
    * 동시성과 관련된 성능이슈로인해 격리레벨(Isolation level)을 설정할 수 있음
  * 지속성
    * 한번 반영된 결과는 영구적으로 기록되어야 한다.
    * 중간에 시스템에 문제가 발생해도 데이터베이스 로그 등을 사용해서 성공한 트랜잭션 내용을 복구해야 한다.
* 트랜잭션 결리 수준 - Isolation level
  * READ UNCOMMITED(커밋되지 않은 읽기)
  * READ COMMITED(커밋된 읽기) - 기본적으로 많이 사용
  * REPETABLE READ(반복가능한 읽기)
  * SERIALIZABLE(직렬화 가능)
  * 참고
    * JPA 책 16.1(트랜잭션과 락)
</details>

<details>
<summary>데이터베이스 연결 구조와 DB세션</summary>

* 데이터 베이스 연결구조
  * 사용자는 WAS나 DB접근 툴 같은 클라이언트를 사용해서 접근가능
  * 클라이언트는 데이터베이스 서버에 연결을 요청하고 커넥션을 맺음
  * 커넥션이 맺어지고 데이터베이스 서버는 내부적으로 세션을 생성, 만들어진 세션으로 커넥션을 통한 모든 요청 실행
    * 커넥션 풀이 10개의 커넥션을 생성하면, 세션도 10개 만들어짐
  * 흐름
    * 클라이언트로 SQL전달 -> 커넥션에 연결된 세션이 SQL실행 -> 세션은 트랜잭션을 시작하고 커밋 또는 롤백 -> 트랜잭션 종료
    * 사용자가 커넥션을 닫거나, DB관리자가 세션을 강제로 종료하면 세션은 종료
</details>

<details>
<summary>트랜잭션 DB예제</summary>

* Commit, Rollback
  * 변경사항을 DB에 반영하려면 Commit, 복구하려면 Rollback
* 흐름
  * 사용자1이 세션1에서 트랜잭션시작 하고 신규데이터 추가하고 Commit하지 않음
  * 사용자2는 세션2에서 신규데이터를 조회할 수 없음
    * 세션2에서 세션1의 신규데이터를 조회할 수있다면 정합성에서 문제 발생(세션1이 Commit할지 Rollback 할지 모르니깐)
    * READ UNCOMMITED 격리수준일때면 조회는 가능하지만 데이터정합성에 문제있을수 있음
  * 세션1에서 Commit하면 세션2에서 조회가능, 세션1에서 Rollback하면 트랜잭션내의 모든 작업들이 rollback됨
* 수동커밋설정
  * 자동 커밋으로 설정하면 쿼리 실행 직후에 자동으로 커밋 호출
  * 트랜잭션기능을 사용하려면 수동커밋으로 설정하고 진행
  * 트랜잭션 수행시간 타임아웃
    * DB마다 설정시간이 다름
    * 일정시간이 지나도록 commit되지 않으면 자동 rollback
  * 수동 커밋 모드로 설정하는 것을 '트랜잭션 시작' 한다라고 표현함
  * * 자동커밋 모드도 내부적으로는 짧은 트랜잭션이 발생하긴한다
</details>

<details>
<summary>DB락</summary>

* 해당 로우의 락을 획득해야 데이터 변경가능
* 락 대기 시간을 넘어가면 락 타임아웃 오류가 발생
* 커밋으로 트랜잭션이 종료되면 락을 반납하고, 다른 세션이 해당 로우의 데이터 변경이 가능해짐
* 실습 - DB락 변경
  * 세션1
    * 세션1이 트랜잭션을 시작하고 돈을 500원으로 업데이트, 아직 커밋 전
    * `memberA` row의 락은 세션1이 가짐
  * 세션2
    * 세션2는 `memberA`의 데이터를 1000원으로 수정하려함
    * 세션1이 트랜잭션을 커밋하거나 롤백하지 않았음으로, 세션2는 락을 획득하지 못해 대기하기된다
    * `SET LOCK_TIMEOUT 60000` : 락 획득 시간을 60초로 설정한다. 60초 안에 락을 얻지 못하면 예외가 발생한
      다.
  * 세션2 락 획득
    * 세션1이 커밋하게되면서 락을 반납하게되고, 대기중이던 세션2가 락을 획득하여 데이터변경이 가능해지게 된다
* DB락 -조회
  * 일반적으로 조회할때는 락을 걸지 않음
    * 세션1에서 수정을 하고 있어도 세션2에서는 세션1이 데이터 수정하기 전의 row들을 전부 조회 가능
  * 조회시에도 락이 필요할 경우가 있음
    * 변경이 일어나는 동안 다른 세션에서 조회가 되면 안되는경우
    * 이럴 경우 `select for update` 구문 사용
      * 조회할때 락을 걸게 되면 변경 때와 마찬가지로 다른세션에서 해당row의 데이터를 변경할 수 없고, 락을 반납해야지 데이터의 변경이 가능하다
</details>

<details>
<summary>트랜잭션 적용</summary>

```
-- 예외 발생 했을때 검증
assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000)).isInstanceOf(
  IllegalStateException.class);
```
* 트랜잭션 적용시점
  * 비즈니스 로직이 있는 서비스계층에서 시작
  * 트랜잭션을 시작하려면 커넥션이 필요, 서비스 계층에서 커넥션을 만들고, 트랜잭션 커밋후에 커넥션을 종료해야함
  * 애플리케이션에서 DB트랜잭션을 사용하려면 트랜잭션을 사용하는 동안 같은 커넥션을 유지해야함 -> 같은 세션사용하기위해
* 같은 커넥션을 유지하기 위한 방법 1
  * 커넥션을 파라미터로 전달해서 같은 커넥션이 유지되도록 함
  * 커넥션 유지가 필요한 메서드는 리포지토리에서 커넥션을 닫으면 안된다. 리포지토리 뿐만아니라 이후에도 커넥션을 계속 이어서 사용하기 때문에, 이후 서비스 로직이 끝날 때 트랜잭션을 종료하고 닫아야 한다.
  * 커넥션 풀 사용시
    * 비즈니스 로직을 시작하기 전 setAutoCommit을 false로 해서 트랜잭션을 시작, 서비스로직에서 커넥션을 닫고 커넥션 풀에 반납하기전에 setAutoCommit을 true로 돌려놓고 반환
</details>

## 스프링과 문제 해결 - 트랜잭션
<details>
<summary>애플리케이션 구조</summary>

* 프레젠테이션 기술
  * 웹 요청/응답을 담당
  * 주 사용 기술: 서블릿과 HTTP 같은 웹 기술, 스프링 MVC
* 서비스 계층
  * 비즈니스 로직을 담당
  * 특정 기술에 의존하지 않고 순수 자바코드로 작성
* 데이터 접근 계층
  * 실제 db에 접근하는 계층
  * 주 사용 기술: JDBC, JPA, File, Redis, Mongo ...
</details>

<details>
<summary>순수한 서비스 계층</summary>

* 여기서 가장 중요한 곳은 어디일까? 바로 핵심 비즈니스 로직이 들어있는 서비스 계층이다. 시간이 흘러서 UI(웹)
  와 관련된 부분이 변하고, 데이터 저장 기술을 다른 기술로 변경해도, 비즈니스 로직은 최대한 변경없이 유지되어
  야 한다.
* 이렇게 하려면 서비스 계층을 특정 기술에 종속적이지 않게 개발해야함
* 서비스 계층이 특정 기술에 종속되지 않기 때문에 비즈니스 로직을 유지보수 하기도 쉽고, 테스트 하기도 쉽다.
* 정리하자면 서비스 계층은 가급적 비즈니스 로직만 구현하고 특정 구현 기술에 직접 의존해서는 안된다. 이렇게
  하면 향후 구현 기술이 변경될 때 변경의 영향 범위를 최소화 할 수 있다.
</details>

<details>
<summary>문제점들</summary>

* 트랜잭션 문제
  * JDBC 구현 기술이 서비스 계층에 누수되는 문제
    트랜잭션을 적용하기 위해 JDBC 구현 기술이 서비스 계층에 누수되었다.
    서비스 계층은 순수해야 한다. 구현 기술을 변경해도 서비스 계층 코드는 최대한 유지할 수 있어야 한다.
    (변화에 대응)
    그래서 데이터 접근 계층에 JDBC 코드를 다 몰아두는 것이다.
    물론 데이터 접근 계층의 구현 기술이 변경될 수도 있으니 데이터 접근 계층은 인터페이스를 제공하는
    것이 좋다.
  * 서비스 계층은 특정 기술에 종속되지 않아야 한다. 지금까지 그렇게 노력해서 데이터 접근 계층으로 JDBC
    관련 코드를 모았는데, 트랜잭션을 적용하면서 결국 서비스 계층에 JDBC 구현 기술의 누수가 발생했다.
  * 같은 트랜잭션을 유지하기 위해 커넥션을 파라미터로 넘겨야 한다.<br>
    이때 파생되는 문제들도 있다. 똑같은 기능도 트랜잭션용 기능과 트랜잭션을 유지하지 않아도 되는 기능으
    로 분리해야 한다.
* 예외누수 문제
  * 데이터 접근 계층의 JDBC 구현 기술 예외가 서비스 계층으로 전파된다.
  * `SQLException` 은 체크 예외이기 때문에 데이터 접근 계층을 호출한 서비스 계층에서 해당 예외를 잡아서 처리
    하거나 명시적으로 `throws` 를 통해서 다시 밖으로 던져야한다.<br>
    `SQLException` 은 JDBC 전용 기술이다. 향후 JPA나 다른 데이터 접근 기술을 사용하면, 그에 맞는 다른 예외
    로 변경해야 하고, 결국 서비스 코드도 수정해야 한다.
* JDBC반복 문제
  * 지금까지 작성한 `MemberRepository` 코드는 순수한 JDBC를 사용했다.<br>
    이 코드들은 유사한 코드의 반복이 너무 많다.<br>
    `try` , `catch` , `finally` ...
* **스프링과 문제 해결**<br>
  스프링은 서비스 계층을 순수하게 유지하면서, 지금까지 이야기한 문제들을 해결할 수 있는 다양한 방법과 기술들을 제
  공한다
</details>

<details>
<summary>트랜잭션 추상화</summary>

* JDBC기술에 의존하고있는 서비스를 JPA나 다른 데이터 접근 기술로 변경하면, 서비스 계층의 트랜잭션 코드들도 모두 수정해야한다.
* 구현 기술에 따른 트랜잭션 사용법
  * JDBC: `con.setAutoCommit(false)`
  * JPA: `transaction.begin()`
* 어떤 기술을 사용하던지 코드의 변경을 최소화 할려면 트랜잭션을 추상화해야한다.
  * 단일 책임 원칙
* 스프링이 제공하는 트랜잭션 추상화 기술을 사용하면, 데이터 접근기술에 따른 트랜잭션 구현체도 대부분 만들어져있다.
</details>

<details>
<summary>트랜잭션 동기화</summary>

* 리소스 동기화
  * 트랜잭션을 유지하려면 트랜잭션의 시작부터 끝까지 같은 데이터베이스 커넥션을 유지해아한다. 결국 같은 커넥션을 동기화(맞추어 사용)하기 위해서 이전에는 파라미터로 커넥션을 전달하는 방법을 사용했다.<br>
    파라미터로 커넥션을 전달하는 방법은 코드가 지저분해지는 것은 물론이고, 커넥션을 넘기는 메서드와 넘기지 않는 메서드를 중복해서 만들어야 하는 등 여러가지 단점들이 많다
* 스프링은 **트랜잭션 동기화 매니저**를 제공한다. 이것은 쓰레드 로컬(`ThreadLocal`)을 사용해서 커넥션을 동기화 해준다. 트랜잭션 매니저는 내부에서 이 트랜잭션 동기화 매니저를 사용한다.
* 트랜잭션 동기화 매니저는 쓰레드 로컬을 사용하기 때문에 멀티쓰레드 상황에 안전하게 커넥션을 동기화 할 수 있
  다.
  * 커넥션이 필요하면 트랜잭션 동기화 매니저를 통해 커넥션을 획득
* 동작방식
  1. 트랜잭션을 시작하려면 커넥션이 필요, 트랜잭션 매니저는 데이터소스를 통해 커넥션을 만들고 트랜잭션을
     시작한다.
  2. 트랜잭션 매니저는 트랜잭션이 시작된 커넥션을 트랜잭션 동기화 매니저에 보관하낟.
  3. 리포지토리는 트랜잭션 동기화 매니저에 보관된 커넥션을 꺼내서 사용한다. (파라미터로 커넥션을 전달 안해도 됨)
  4. 트랜잭션이 종료되면 트랜잭션 매니저는 트랜잭션 동기화 매니저에 보관된 커넥션을 통해 트랜잭션을 종료하고, 커넥션도 닫는다.
* ThreadLocal 참고
  * 쓰레드 로컬을 사용하면 각각의 쓰레드마다 별도의 저장소가 부여된다. 따라서 해당 쓰레드만 해당 데이터에 접근할 수 있다.
    * `스프링 핵심 원리 - 고급편` 강의 참고
</details>

<details>
<summary>트랜잭션 매니저 정리</summary>

**트랜잭션 매지너1 - 트랜잭션 시작**
* 클라이언트의 요청으로 서비스 로직을 실행
1. 서비스 계층에서 `transactionManager.getTransaction()` 호출해서 트랜잭션 시작
2. 트랜잭션을 시작하려면 먼저 데이터베이스 커넥션이 필요, 트랜잭션 매니저는 내부에서 데이터소스를 사용해서 커넥션을 생성
3. 커넥션을 수동 커밋 모드로 변경해서 실제 데이터베이스 트랜잭션을 시작
4. 커넥션을 트랜잭션 동기화 매니저에 보관
5. 트랜잭션 동기화 매니저는 쓰레드 로컬에 커넥션을 보관한다. 따라서 멀티 쓰레드 환경에 안전하게 커넥션을 보관할 수 있다.
<br><br>**트랜잭션 매니저2 - 로직실행**
6. 서비스는 비즈니스 로직을 실행하면서 리포지토리의 메서드들을 호출한다. 이때 커넥션을 파라미터로 전달하지 않는다.
7. 리포지토리 메서드들은 트랜잭션이 시작된 커넥션이 필요하다. 리포지토리는 `DataSourceUtils.getConnection()`을 사용해서 트랜잭션 동기화 매니저에 보관된 커넥션을 꺼내 사용한다.
<br>이 과정을 통해서 자연스럽게 같은 커넥션을 사용하고, 트랜잭션도 유지된다.
8. 획득한 커넥션을 사용해서 SQL을 데이터베이스에 전달해서 실행한다.
<br><br>**트랜잭션 매니저3 - 트랜잭션 종료**
9. 비즈니스 로직이 끝나고 트랜잭션을 종료한다. 트랜잭션은 커밋하거나 롤백하면 된다.
10. 트랜잭션을 종료하려면 동기화된 커넥션이 필요하다. 트랜잭션 동기화 매니저를 통해 동기화된 커넥션을 획득한다.
11. 획득한 커넥션을 통해 데이터베이스에 트랜잭션을 커밋하거나 롤백한다.
12. 전체 리소스를 정리한다.
    * 트랜잭션 동기화 매니저를 정리한다. 쓰레드 로컬은 사용후 꼭 정리해야 한다.
    * `con.setAutoCommit(true)`로 되돌린다. 커넥션 풀을 고려해야 한다.
    * `con.close()`를 호출해서 커넥션을 종료한다. 커넥션 풀을 사용하는 경우 `con.close()`를 호출하면 커넥션 풀에 반환된다.

</details>

<details>
<summary>트랜잭션 템플릿</summary>

* 템플릿 콜백 패턴을 적용하려면 템플릿을 제공하는 클래스를 작성해야 하는데, 스프링은 `TransactionTemplate`라는 템플릿 클래스를 제공한다.
* `execute()`: 응답값이 있을 때 사용
* `executeWithResult()`: 응답값 없을 때 사용
* 기본 동작
  * 비즈니스 로직이 정상 수행되면 커밋한다.
  * 언체크 예외가 발생하면 롤백한다. 그 외의 경우 커밋한다.

</details>

## 스프링과 문제 해결 - 트랜잭션 AOP 이해
<details>
<summary>프록시 도입</summary>

* 프록시를 사용하면 트랜잭션을 처리하는 객체와 비즈니스 로직을 처리하는 서비스 객체를 명확하게 분리 가능
  * 프록시는 대리인,대리자 라는뜻
  * 프록시가 트랜잭션의 시작과 끝을 맡아서 처리해줌
* 프록시 도입 전: 서비스에 비즈니스 로직과 트랜잭션 처리 로직이 함께 섞여있다.
* 프록시 도입 후: 트랜잭션 프록시가 트랜잭션 처리 로직을 모두 가져간다.<br>
  트랜잭션을 시작한 후에 실제 서비스를 대신 호출한다. 트랜잭션 프록시 덕분에 서비스 계층에는 순수한 비즈니스 로직만 남길 수 있다.
</details>
<details>
<summary>스프링이 제공하는 트랜잭션 AOP</summary>

* 스프링이 제공하는 AOP기능을 사용하면 프록시를 매우 편리하게 적용할 수 있다.
* 직접 스프링AOP를 사용해서 트랜잭션을 처리해도 되지만, 스프링은 트랜잭션 AOP를 처리하기 위한 모든 기능을 제공되고, AOP를 처리하기 위해 필요한 스프링 빈들도 자동으로 등록시켜준다.
  * AOP처리위한 클래스
    * 어드바이저: `BeanFacttoryTransactionAttributeSourceAdvisor`
    * 포인트컷: `TransactionAttributeSourcePointcut`
    * 어드바이스: `TransactionInterceptor`
* 개발자는 `@Transactional` 애노테이션만 붙여주면된다.
</details>
<details>
<summary>선언적 트랜잭션 관리 vs 프로그래밍 방식 트랜잭션 관리</summary>

* 선언적 트랜잭션 관리
  * `@Transactional`애노테이션 하나만 선언해서 트랜잭션 관리하는 것을 선언적 트랜잭션 관리라고 한다.
  * 선언적 트랜잭션 관리는 과거 XML에 설정하기도 했다.
    * 해당 로직에 트랜잭션을 적용하겠다라고 어딘가에 선언하기만 하면 트랜잭션이 적용되는 방식
* 프로그래밍 방식의 트랜잭션관리
  * 트랜잭션 매니저 또는 트랜잭션 템플릿 등을 사용해서 트랜잭션 관련 코드를 직접 작성하는 것을 프로그래밍 방식의 트랜잭션 관리라고 한다.
* 선언적 트랜잭션 관리가 프로그래밍 방식에 비해 훨씬 간편하고 실용적이여서 대부분 선언적 트랜잭션 관리를 사용한다.
* 프로그래밍 방식 트랜잭션 관리는 테스트 시에 가끔 사용될 때도 있다.
</details>
<details>
<summary>스프링 부트의 자동 리소스 등록</summary>

* 스프링 부트 등장 이전에는 데이터소스와 트랜잭션 매니저를 개발자가 직접 스프링 빈으로 등록해서 사용
  * `@Bean`을 사용한 자바코드로 등록하는 방법도 있고
  * 더 예전으로 올라가면 XML로 관리하는 방법도 있다
* 데이터소스 - 자동 등록
  * 자동으로 등록되는 스프링 빈 이름: `dataSoruce`
  * 개발자가 직접 데이터소스를 빈으로 등록하면 스프링 부트는 데이터소스를 자동으로 등록하지 않는다
  * 스프링 부트는 `application.properties` or `application.yml` 에 있는 속성을 사용해서 `DataSource` 를 생성한다.
  * 스프링 붙으가 기본으로 생성하는 데이터소스는 커넥션풀을 제공하는 `HikariDataSource`다
  * `spring.datasource.url`속성이 없으면 내장 데이터베이스(메모리DB)를 생성하려고 시도한다.
* 트랜잭션 매니저 - 자동등록
  * 스프링 부트는 적절한 트랜잭션 매니저 `PlatformTransactionManager`를 자동으로 스프링 빈에 등록한다 
  * 자동으로 등록되는 스프링 빈 이름: `transactionManager`
  * 개발자가 직접 트랜잭션 매니저를 빈드로 등록하면 스프링 부트는 트랜잭션 매니저를 자동으록 등록하지 않는다
  * 어떤 트랜잭션 매니저를 선택할지는 현재 등록된 라이브러리를 보고 판단
    * JDBC기술을 사용하면 -> `DataSourceTransactionManager`
    * JPA기술을 사용하면 -> `JpaTranscationManager`, `JpaTransactionManager`는 `DataSourceTransactionManager`가 제공하는 기능도 대부분 지원 
</details>

## 자바 예외 이해
<details>
<summary>예외 계층</summary>

* `Object`: 최상위 객체, 자바에서는 예외도 객체로 취급
* `Throwable`: 최상위 예외, `Exception`, `Error`로 나뉨
  * `Error`는 시스템 오류에 관한 오류이기 때문에 발생한 시점에 이미 복구 불가 -> 잡으려고 하면 안됨
  * `catch`로 `Throwable`을 잡으려하면 `Error`도 같이 잡히기 때문에 개발자는 `Excepction` 부터 필요한 예외로 생각하면 잡으면 된다.
* `Exception`: 체크예외
  * 애플리케이션 로직에서 사용할 수 있는 실질적인 최상위 예외
  * `Exception`과 그 하위 예외는 모두 컴파일러가 체크하는 체크 예외이다. 단 `RuntimeException`은 예외로 한다.
  * 예외처리를 하지 않으면 컴파일 에러가 발생
* `RuntimeException`: 언체크 예외, 런타임 예외
  * 컴파일러가 체크 하지 않는 언체크 예외
  * `RuntimeException`과 그 자식 예외는 모두 언체크 예외, 대표적으로 `NullPointerException`, `IllegalArgumentException`
    * 예외 처리를 강제하지 않음 -> 예외처리를 하지 않아도 컴파일에러가 발생하지 않는다.
    * 주로 프로그래밍 오류로 인해 발생
</details>
<details>
<summary>예외 기본 규칙</summary>
1. 예외는 잡아서 처리하거나 던져야 한다.
2. 예외를 잡거나 던질 때 지정한 예외뿐만 아니라 그 예외의 자식들도 함께 처리된다.

* 참고: 예외를 처리하지 못하고 계속 던지면?
  * 자바 `main()`쓰레드의 경우 예외로그를 출력하면서 시스템이 종료
  * 웹 애플리케이션의 경우 여러 사용자의 요청을 처리하기 때문에 하나의 예외때문에 시스템이 종료되면 안된다.<br>
    WAS가 해당 예외를 받아서 처리하는데, 주로 사용자에게 개발자가 지정한, 오류 페이지를 보여준다.
</details>
<details>
<summary>체크 예외 활용</summary>

* 어느상황에서 체크 예외를 사용하고 언제 런타임 예외를 사용?
  * 기본적으로 런타임 예외 사용
  * 체크 예외는 비즈니스 로직상 의도적으로 던지는 예외에만 사용
    * 이 경우 해당 예외를 잡아서 반드시 처리해야 하는 문제일 때만 체크 예외를 사용해야 한다.
    * 체크 예외 예:
      * 계좌 이체 실패예외
      * 결제시 포인트 부족예외
      * 로그인 ID, PW 불일치 예외
    * 이 경우에도 100% 체크 예외로 만들어야 한느건 아니지만, 계좌 이체 실패처럼 매우 심각한 문제는 개발자가 실수로 예외를 놓치면 안된다고 판단할 수 있다.
* 체크예외의 문제점
  * 체크 예외는 명시적으로 예외를 잡아서 처리해야하니 안전하긴하지만
  * 외부 네트워크에 접속해서 어떤 기능을 처리한다고 가정하고 체크예외를 던진다고 가정
  * 그곳에서 올라오는 체크예외는 서비스에서 처리할 방법을 모른다.
  * 서비스는 외부의 체크예외들을 처리할 수 없으므로 둘다 밖으로 던진다.
  * 컨트롤러에서도 외부의 체크예외들을 처리할 수 없으므로 둘다 밖으로 던진다.
  * 웹 애플리케이션이라면 서블릿의 오류페이지나, 또는 스프링 MVC가 제공하는 `ControllerAdvice`에서 이런 예외를 공통으로 처리한다.
    * 이런 문제들은 사용자에게 어떤 문제가 발생했는지 자세히 설명하기가 어렵고, '서비스에 문제가 있습니다' 라는 일반적인 메시지를 보여준다.
    * API라면 보통 HTTP상태코드500(내부 서버 오류)를 사용해서 응답을 내려준다.
    * 이렇게 해결이 불가능한 공통코드 예외는 별도의 오류 로그를 남기고, 개발자가 오류를 빨리 인지할 수 있또록 메일, 알람등을 통해서 전달받아야한다.<br>
    예를들어서 `SQLException`이 잘못된 SQL을 작성해서 발생했다면, 개발자가 해당 SQL을 수정해서 배포하기 전까지 사용자는 같은 문제를 겪게 된다.
  * 결론
    1. 복구 불가능한 예외
    2. 의존 관계에 대한 문제
       * 복구 불가능한 예외에 대해서, 체크예외이기 때문에 컨트롤러나 서비스 입장에서는 본인이 처리할 수 없어도 어쩔 수 없이 `thorw`를 통해 던지는 예외를 선언해야 한다.
       * 서비스, 컨트롤러에서 `java.sql.SQLException`을 의존하기 때문에 문제
       * 향후 리포지토리를 JDBC기술이 아닌 다른 기술로 변경한다면 `SQLException`에 의존하던 모든 서비스, 컨트롤러의 코드를 변경해야한다
       * 모든 체크예외를 `Exception`으로 처리하면 의존관계 문제는 해결가능?
         * 다른 체크 예외를 체크할 수 있는 기능이 무효화되고, 중요한 체크 예외를 다 놓치게 된다.
    * 내가 서비스에서 처리할 수도 없는 예외들도 강제로(일반적인 메시지나, 500상태코드 등) 처리할 수 밖에 없고, 코드에 구현기술을 명시하는 의존관계 문제가 생긴다.
    * 그렇다면 대안은?
      * uncheck 예외 활용
</details>
<details>
<summary>언체크 예외 활용</summary>

* `SQLException`을 런타임 예외인 `RuntimeSQLException`으로 변환
* 런타임 예외이기 때문에 서비스, 컨트롤러는 해당 예외들을 처리할 수 없다면 별도의 선언 없이 그냥 두면 된다.
</details>
<details>
<summary>예외 포함과 스택 트레이스</summary>

* 예외를 전환할 때는 꼭 기존 예외를 포함해야한다. 그렇지 않으면 스택 트레이스를 확인할 때 심각한 문제가 발생
* 로그를 출력할 때 마지막 파라미터에 예외를 넣어주면 로그에 스택트레이스를 출력할 수 있다.
  * `log.info("message={}", message, e)`
</details>

## 스프링과 문제해결 - 예외처리, 반복
<details>
<summary>체크예외와 인터페이스</summary>

* 서비스 계층은 가급적 특정 구현 기술에 의존하지 않고, 순수하게 유지하는 것이 좋다. 이렇게 하려면 예외에 대한 의존도 함께 해결해야한다.
* 예를 들어서 서비스가 처리할 수 없는 `SQLException`의 경우 리포지토리가 던지는 `SQLException` 체크 예외를 런타임 예외로 전환해서 서비스 계층에 던지자.<br>
이렇게 하면 서비스 계층이 해당 예외를 무시할 수 있기 때문에, 특정 구현 기술에 의존하는 부분을 제거하고 서비스 계층을 순수하게 유지할 수 있다.
* 남은 문제
  * 리포지토리에서 넘오는 특정한 예외의 경우 복구를 시도할 수도 있다. 그런데 지금 방식은 항상 `MyDbException`이라는 예외만 넘어오기 때문에 예외를 구분할 수 없는 단점이 있다.
  * 만약 특정 상황에는 예외를 잡아서 복구하고 싶으면 예외를 어떻게 구분해서 처리할 수 있을까?
</details>
<details>
<summary>데이터 접근 예외 직접 만들기</summary>

* 데이터베이스 오류에 따라서 특정 예외는 복구하고 싶을 수 있다.
  * DB에 같은 ID가 있으면 뒤에 숫자를 붙여 새로운 ID를 만드는 경우
  * 데이터를 DB에 저장할 때 같은 ID가 있으면 데이터 베이스는 오류를 반환하고, 오류코드를 받은 JDBC 드라이버는 `SQLException`을 던진다.<br>
  그리고 `SQLException`에는 데이터베이스가 제공하는 `errorCode`라는 것이 들어있다.
* 오류코드를 활용하기 위해 `SQLException`을 서비스 계층으로 다시 던지게 되면, 서비스 계층이 `SQLException`이라는 JDBC기술에 의존하게 되면서, 지금까지 고민했던 서비스 계층의 순수성이 무너진다.
  * 이 문제를 해결하려면 앞서 배운 것처럼 리포지토리에서 예외를 변환해서 던지면 된다.
    * `SQLException` -> `MyDuplicateKeyException`
* 남은 문제
  * `SQL ErrorCode`는 각각의 데이터베이스 마다 다르다. 결과적으로 데이터베이스가 변경될 때마다 ErrorCode도 모두 변경해야 한다.
  * 데이터베이스가 전달하는 오류는 키 중복 뿐만 아니라 락이 걸린경우, SQL문법에 오류가 있는 경우 등등 수십가지 오류 코드가 있다.<br>
  이 모든 상황에 맞는 예외를 지금 처럼 다 만들수는 없다.
  * (스프링이 다 해결해줌...)
</details>
<details>
<summary>스프링 에외 추상화 이해</summary>

* 스프링은 데이터 접근 계층에 대한 수십가지 예외를 정리해서 일관된 예외 계층을 제공
* 각각의 예외는 특정 기술에 종속적이지 않게 설계되어 있다. 따라서 서비스 계층에서도 스프링이 제공하는 예외를 사용한면 된다.
* 스프링 예외의 최상위는 `org.springframework.dao.DataAccessException`이다. 때문에 스프링이 제공하는 데이터 접근 계층의 모든 예외는 런타임 예외이다.
* `DataAccessException`은 크게 2가지로 구분하는데 `NonTransient`예외와 `Trasient`예외이다.
  * `Trasient(일시적인)`: `Trasient` 하위 예외는 동일한SQL을 다시 시도했을 때 성공 가능이 있는 예외
    * 쿼리타임아웃, 락과관련된 오류들처럼 데이터 베이스 상태가 좋아지거나, 락이 풀렸을 때 다시 시도하면 성공할 수도 있다.
  * `NonTrasient`: 일시적이지 않다는 뜻, 같은 SQL을 그대로 반복해서 실행하면 실패한다.
    * SQL문법오류, 데이터베이스 제약조건 위배등
* 스프링이 제공하는 예외 변환기
  * 스프링은 데이터베이스에서 발생하는 오류 코드를 스프링이 정의한 예외로 자동으로 변환해주는 변환기를 제공한다.
</details>

