package hmw.ecommerce.entity;

import hmw.ecommerce.entity.vo.Address;
import jakarta.persistence.*;
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
    private String phone;
    private String nickName;

    private String email;
    private boolean isVerified;

    @Embedded
    private Address address;

    private boolean seller;

    private String role;

    public void verifySuccess(String email) {
        this.isVerified = true;
        this.email = email;
    }

}
