package hmw.ecommerce.entity;

import hmw.ecommerce.entity.vo.Address;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Member extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String username;
    private String email;
    private String password;

    @Embedded
    private Address address;

}
