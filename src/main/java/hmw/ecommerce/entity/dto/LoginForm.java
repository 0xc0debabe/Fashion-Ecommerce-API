package hmw.ecommerce.entity.dto;


import lombok.Getter;

public class LoginForm {

    @Getter
    public static class Request {
        private String loginId;
        private String password;
    }

    public static class Response {
        private String memberId;
    }

}
