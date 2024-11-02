package hmw.ecommerce.controller;

import hmw.ecommerce.entity.dto.SignUpVerificationDto;
import hmw.ecommerce.exception.Validation;
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

    @PostMapping("send-code")
    public ResponseEntity<?> sendVerificationCode(
            @Valid @RequestBody SignUpVerificationDto verificationDto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return Validation.validateDtoErrors(bindingResult);
        }

        return ResponseEntity.ok(emailService.sendEmail(verificationDto.getEmail()));
    }

}
