package hello.jdbc.connection;

import static hello.jdbc.connection.ConnectionConst.*;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Slf4j
public class ConnectionTest {

  @Test
  void driverManager() throws SQLException {
    Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
    Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);

    log.info("connection={}, class={}", con1, con1.getClass());
    log.info("connection={}, class={}", con2, con2.getClass());
  }

  @Test
  void dataSourceDriverManager() throws SQLException {
    // DriverManagerDataSource - 항상 새로운 커넥션을 획득
    DriverManagerDataSource datasource = new DriverManagerDataSource(URL, USERNAME,
        PASSWORD);
    useDataSource(datasource);
  }

  @Test
  void dataSourceConnectionPool() throws SQLException {
    // 커넥션 풀링
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(URL);
    dataSource.setUsername(USERNAME);
    dataSource.setPassword(PASSWORD);
    dataSource.setMaximumPoolSize(10);
    dataSource.setPoolName("HelloPool");

    useDataSource(dataSource);
  }

  private void useDataSource(DataSource dataSource) throws SQLException {
    Connection con1 = dataSource.getConnection();
    Connection con2 = dataSource.getConnection();
//    설정한 pool개수 초과해서 생성한 뒤 커넥션이 가득차면 일정시간 기다린뒤(설정에 따라 다름) 예외 발생
//    Connection con3 = dataSource.getConnection();
//    Connection con4 = dataSource.getConnection();
//    Connection con5 = dataSource.getConnection();
//    Connection con6 = dataSource.getConnection();
//    Connection con7 = dataSource.getConnection();
//    Connection con8 = dataSource.getConnection();
//    Connection con9 = dataSource.getConnection();
//    Connection con10 = dataSource.getConnection();
//    Connection con11 = dataSource.getConnection();

    log.info("connection::{}, class={}", con1, con1.getClass());
    log.info("connection::{}, class={}", con2, con2.getClass());
  }
}
