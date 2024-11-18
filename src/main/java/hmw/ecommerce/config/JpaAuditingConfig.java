package hmw.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing을 활성화하기 위한 설정 클래스.
 * 이 설정을 통해 엔티티에서 생성 및 수정 시간과 같은 감사 정보를 자동으로 관리할 수 있음.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
