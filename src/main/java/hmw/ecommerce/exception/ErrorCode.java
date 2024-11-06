package hmw.ecommerce.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    ALREADY_EXIST_LOGIN_ID(HttpStatus.BAD_REQUEST, "이미 존재하는 로그인 아이디입니다."),
    SESSION_EXPIRED(HttpStatus.BAD_REQUEST, "세션이 만료되었습니다."),
    NOT_FOUND_BRAND(HttpStatus.BAD_REQUEST, "해당 브랜드를 찾을 수 없습니다."),
    ACCOUNT_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "계정이 인증되지 않았습니다."),
    INVALID_ACCESS(HttpStatus.BAD_REQUEST, "접근이 불가능합니다."),
    FAIL_TO_PARSE(HttpStatus.BAD_REQUEST, "파싱하는데 실패하였습니다."),
    NOT_EXIST_LOGIN_ID(HttpStatus.BAD_REQUEST, "존재하지 않는 아이디입니다."),
    INVALID_CODE(HttpStatus.BAD_REQUEST, "코드가 일치하지 않습니다."),
    NOT_EXIST_EMAIL(HttpStatus.BAD_REQUEST, "존재하지 않는 이메일입니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "역직렬화 오류"),
    NOT_EXIST_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "존재하지 않는 코드입니다.");

    private final HttpStatus httpStatus;
    private final String description;
}
