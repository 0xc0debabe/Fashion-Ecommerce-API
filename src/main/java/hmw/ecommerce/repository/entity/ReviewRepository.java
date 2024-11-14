package hmw.ecommerce.repository.entity;

import hmw.ecommerce.entity.Review;
import hmw.ecommerce.repository.QueryDslRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long>, QueryDslRepository {
}
