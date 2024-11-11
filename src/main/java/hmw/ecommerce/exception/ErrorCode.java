package hmw.ecommerce.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 회원 예외
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    ALREADY_EXIST_LOGIN_ID(HttpStatus.BAD_REQUEST, "이미 존재하는 로그인 아이디입니다."),
    ACCOUNT_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "계정이 인증되지 않았습니다."),
    NOT_EXIST_LOGIN_ID(HttpStatus.BAD_REQUEST, "존재하지 않는 아이디입니다."),
    NOT_EXIST_EMAIL(HttpStatus.BAD_REQUEST, "존재하지 않는 이메일입니다."),

    // 아이템 예외
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),
    NOT_EXISTS_ITEM(HttpStatus.BAD_REQUEST, "아이템이 존재하지 않습니다."),

    // 카테고리 예외
    NOT_EXISTS_CATEGORY_TYPE(HttpStatus.BAD_REQUEST, "카테고리타입이 존재하지 않습니다."),

    // 장바구니 예외
    ALREADY_EXIST_ITEM_TO_CART(HttpStatus.BAD_REQUEST, "장바구니에 존재하는 아이템입니다."),
    CAN_NOT_ADD_TO_CART(HttpStatus.BAD_REQUEST, "장바구니에 추가할 수 없습니다."),
    CANNOT_FOUND_CART_ITEM(HttpStatus.BAD_REQUEST, "장바구니 아이템을 찾을 수 없습니다."),
    CANNOT_EDIT_CART_ITEM(HttpStatus.BAD_REQUEST, "장바구니 아이템을 수정할 수 없습니다."),
    CANNOT_DELETE_CART_ITEM(HttpStatus.BAD_REQUEST, "장바구니 아이템을 삭제할 수 없습니다."),

    // 인증, 인가 예외
    SESSION_EXPIRED(HttpStatus.BAD_REQUEST, "세션이 만료되었습니다."),
    INVALID_ACCESS(HttpStatus.BAD_REQUEST, "접근이 불가능합니다."),
    INVALID_CODE(HttpStatus.BAD_REQUEST, "코드가 일치하지 않습니다."),

    // 기타 예외
    FAIL_TO_PARSE(HttpStatus.BAD_REQUEST, "파싱하는데 실패하였습니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "역직렬화 오류"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");

    private final HttpStatus httpStatus;
    private final String description;
}
