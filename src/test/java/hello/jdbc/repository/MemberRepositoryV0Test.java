package hello.jdbc.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import hello.jdbc.domain.Member;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class MemberRepositoryV0Test {

  MemberRepositoryV0 repository = new MemberRepositoryV0();

  @Test
  void crud() throws SQLException {
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
  }
}
