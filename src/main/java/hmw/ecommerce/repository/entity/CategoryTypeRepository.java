package hmw.ecommerce.repository.entity;

import hmw.ecommerce.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryTypeRepository extends JpaRepository<CategoryType, Long> {

    Optional<CategoryType> findByTypeName(String type);

    Optional<CategoryType> findByCategoryId(Long id);

    boolean existsByTypeName(String type);
}
