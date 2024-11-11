package hmw.ecommerce.entity.dto.member;


import lombok.Getter;

public class LoginDto {

    @Getter
    public static class Request {
        private String loginId;
        private String password;
    }

    public static class Response {
        private String memberId;
    }

}
