package hello.jdbc.repository;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class MemberRepositoryV1Test {

  MemberRepositoryV1 repository;

  @BeforeEach
  void setUp() throws SQLException {
    // 기본 DriverManager - 항상 새로운 커넥션을 획득
//    DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

    // 커넥션 풀링
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(URL);
    dataSource.setUsername(USERNAME);
    dataSource.setPassword(PASSWORD);

    this.repository = new MemberRepositoryV1(dataSource);
  }

  @Test
  void crud() throws SQLException, InterruptedException {
    // save
    Member member = new Member("memberV7", 10000);
    repository.save(member);

    // findById
    member = repository.findById(member.getMemberId());
    assertThat(member.getMemberId()).isEqualTo(member.getMemberId());

    // update: money: 10000 -> 20000
    repository.update(member.getMemberId(), 20000);
    Member updatedMember = repository.findById(member.getMemberId());
    assertThat(updatedMember.getMoney()).isEqualTo(20000);

    // delete
    repository.delete(member.getMemberId());
    Member finalMember = member;
    Assertions.assertThatThrownBy(() -> repository.findById(finalMember.getMemberId())).isInstanceOf(
        NoSuchElementException.class);

    Thread.sleep(1000);
  }
}
