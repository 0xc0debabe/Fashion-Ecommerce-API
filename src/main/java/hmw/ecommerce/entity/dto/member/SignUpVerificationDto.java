package hmw.ecommerce.entity.dto.member;


import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class SignUpVerificationDto {

    @Email
    private String email;

}
