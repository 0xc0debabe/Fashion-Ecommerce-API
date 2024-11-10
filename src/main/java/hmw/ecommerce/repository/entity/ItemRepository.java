package hmw.ecommerce.repository.entity;

import hmw.ecommerce.entity.Item;
import hmw.ecommerce.repository.QueryDslRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, QueryDslRepository {

    Page<Item> findByOrderByCreatedAtDesc(Pageable pageable);

}