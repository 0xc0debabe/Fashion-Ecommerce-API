package hmw.ecommerce.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    ALREADY_EXIST_LOGIN_ID(HttpStatus.BAD_REQUEST, "이미 존재하는 로그인 아이디입니다."),
    INVALID_CODE(HttpStatus.BAD_REQUEST, "코드가 일치하지 않습니다."),
    NOT_EXIST_EMAIL(HttpStatus.BAD_REQUEST, "존재하지 않는 이메일입니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "역직렬화 오류"),
    NOT_EXIST_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "존재하지 않는 코드입니다.");

    private final HttpStatus httpStatus;
    private final String description;
}
