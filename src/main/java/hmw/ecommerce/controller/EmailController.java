package hmw.ecommerce.controller;

import hmw.ecommerce.entity.dto.member.SignUpVerificationDto;
import hmw.ecommerce.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("email")
@RestController
public class EmailController {

    private final EmailService emailService;

    /**
     * 이메일 인증 코드를 전송합니다.
     *
     * @param verificationDto 이메일 주소를 포함한 인증 요청 데이터
     * @param bindingResult 유효성 검사 결과
     * @return 인증 코드 전송 성공 여부를 담은 ResponseEntity
     */
    @PostMapping
    public ResponseEntity<?> sendVerificationCode(
            @Valid @RequestBody SignUpVerificationDto verificationDto,
            BindingResult bindingResult) {

        return ResponseEntity.ok(emailService.sendEmail(verificationDto.getEmail()));
    }

}