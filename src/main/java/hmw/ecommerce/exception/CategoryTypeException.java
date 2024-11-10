package hmw.ecommerce.exception;

import lombok.Getter;

@Getter
public class CategoryTypeException extends RuntimeException{

    private final ErrorCode errorCode;

    public CategoryTypeException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
