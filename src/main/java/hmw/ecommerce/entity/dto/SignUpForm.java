package hmw.ecommerce.entity.dto;

import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.vo.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
        @Size(min = 4, max = 20)
        private String loginId;

        @NotEmpty
        @Size(min = 10, max = 20)
        private String password;

        @NotEmpty
        @Size(min = 1, max = 8)
        private String username;

        @Valid
        @NotNull
        private Address address;

        private String role;

        public Member toEntity() {
            return Member.builder()
                    .loginId(this.loginId)
                    .password(this.password)
                    .username(this.username)
                    .isVerified(false)
                    .address(this.address)
                    .role(this.role)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Response {
        private String username;
        private Address address;

        public static Response fromEntity(Member member) {
            return Response.builder()
                    .username(member.getUsername())
                    .address(member.getAddress())
                    .build();
        }
    }

}
