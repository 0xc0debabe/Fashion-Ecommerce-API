package hmw.ecommerce.exception.exceptions;

import hmw.ecommerce.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MemberException extends RuntimeException{

    private final ErrorCode errorCode;

    public MemberException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
