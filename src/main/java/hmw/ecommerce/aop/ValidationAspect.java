package hmw.ecommerce.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 컨트롤러에서 요청 데이터의 유효성을 검사하는 Aspect 클래스.
 * BindingResult에 에러가 있는 경우 에러 메시지를 클라이언트에 반환.
 */
@Component
@Aspect
public class ValidationAspect {

    /**
     * 모든 컨트롤러 메서드에서 BindingResult를 포함한 메서드를 대상으로 유효성 검사를 수행.
     *
     * @param joinPoint 를 이용해 bindingResult를 가져옴
     * @return 유효성 검사 성공 시 원래 메서드의 결과, 실패 시 에러 메시지를 포함한 응답
     * @throws Throwable 메서드 실행 도중 발생할 수 있는 예외
     */
    @Around("execution(* hmw.ecommerce.controller..*(.., org.springframework.validation.BindingResult))")
    public Object validationCheck(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        BindingResult bindingResult = null;

        for (Object arg : args) {
            if (arg instanceof BindingResult) {
                bindingResult = (BindingResult) arg;
                break;
            }
        }

        if (bindingResult != null && bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + " : " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }

        return joinPoint.proceed();
    }

}
