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
