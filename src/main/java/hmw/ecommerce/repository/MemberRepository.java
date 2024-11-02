package hmw.ecommerce.repository;

import hmw.ecommerce.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    boolean existsByLoginId(String loginId);

    Optional<Member> findByEmail(String email);
}
