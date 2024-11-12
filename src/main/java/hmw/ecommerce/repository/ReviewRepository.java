package hmw.ecommerce.repository;

import hmw.ecommerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long>, QueryDslRepository {
}
