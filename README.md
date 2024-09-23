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
