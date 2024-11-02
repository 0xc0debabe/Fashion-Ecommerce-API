package hmw.ecommerce.entity;

import hmw.ecommerce.entity.vo.Address;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String loginId;
    private String password;
    private String username;

    private String email;
    private boolean isVerified;

    @Embedded
    private Address address;

    private String role;

    public void verifySuccess() {
        this.isVerified = true;
    }
}
