package hmw.ecommerce.repository.entity;

import hmw.ecommerce.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByLoginId(String loginId);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByLoginId(String loginId);
}
