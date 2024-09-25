# JDBC
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
