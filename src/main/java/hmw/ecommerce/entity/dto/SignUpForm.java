package hmw.ecommerce.entity.dto;

import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.vo.Address;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class SignUpForm {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotEmpty
        @Size(min = 1, max = 8)
        private String username;
        @Email
        private String email;
        @Size(min = 10, max = 20)
        private String password;

        private Address address;

        private String role;

        public Member toEntity() {
            return Member.builder()
                    .username(this.username)
                    .email(this.email)
                    .password(this.password)
                    .address(this.address)
                    .role(this.role)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Response {
        private String username;
        private String email;
        private Address address;

        public static Response fromEntity(Member member) {
            return Response.builder()
                    .username(member.getUsername())
                    .email(member.getEmail())
                    .address(member.getAddress())
                    .build();
        }
    }

}
