package hmw.ecommerce.exception.exceptions;

import hmw.ecommerce.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ItemException extends RuntimeException{

    private final ErrorCode errorCode;

    public ItemException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
