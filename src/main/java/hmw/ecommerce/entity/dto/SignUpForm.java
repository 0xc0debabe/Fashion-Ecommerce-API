package hmw.ecommerce.entity.dto;

import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.vo.Address;
import hmw.ecommerce.entity.vo.ConstRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
public class SignUpForm {

    @Getter
    @Setter
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

        @NotEmpty
        @Pattern(regexp = "^\\+?\\d{1,4}?[-.\\s]?\\(?\\d{1,3}?\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,4}$",
                message = "휴대폰 번호 형식이 올바르지 않습니다.")
        private String phone;

        @NotEmpty
        private String nickName;

        @Valid
        @NotNull
        private Address address;

        private boolean seller;

        private String role;

        public Member toEntity(boolean isSeller) {
            String assignedRole;
            if (isSeller) {
                assignedRole = ConstRole.ROLE_SELLER;
            } else {
                assignedRole = ConstRole.ROLE_MEMBER;
            }

            return Member.builder()
                    .loginId(this.loginId)
                    .password(this.password)
                    .username(this.username)
                    .isVerified(false)
                    .nickName(this.nickName)
                    .phone(this.phone)
                    .address(this.address)
                    .role(assignedRole)
                    .seller(isSeller)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Response {
        private String username;
        private String loginId;
        private Address address;
        private String phone;
        private String nickName;
        private boolean isSeller;

        public static Response fromEntity(Member member) {
            return Response.builder()
                    .loginId(member.getLoginId())
                    .username(member.getUsername())
                    .address(member.getAddress())
                    .isSeller(member.isSeller())
                    .nickName(member.getNickName())
                    .phone(member.getPhone())
                    .build();
        }

    }

}
