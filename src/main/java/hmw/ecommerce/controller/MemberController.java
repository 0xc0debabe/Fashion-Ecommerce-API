package hmw.ecommerce.controller;

import hmw.ecommerce.entity.dto.SignUpForm;
import hmw.ecommerce.entity.dto.SignUpVerificationDto;
import hmw.ecommerce.exception.Validation;
import hmw.ecommerce.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("member")
@RestController
public class MemberController {

    private final MemberService memberService;

    @PostMapping("signUp")
    public ResponseEntity<?> signUp(
            @Valid @RequestBody SignUpForm.Request request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Validation.validateDtoErrors(bindingResult);
        }

        return ResponseEntity.ok(memberService.signUp(request));
    }

    @PostMapping("duplicateCheck")
    public ResponseEntity<?> duplicateCheckLoginId(
            @RequestParam(name = "loginId")
            String loginId) {

        return ResponseEntity.ok(memberService.duplicateCheckLoginId(loginId));
    }

    @PostMapping("verify/{memberId}")
    public ResponseEntity<?> verifyEmail(
            @Valid @RequestBody SignUpVerificationDto signUpVerificationDto,
            @RequestParam(name = "code") String code,
            @PathVariable(name = "memberId") Long memberId,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return Validation.validateDtoErrors(bindingResult);
        }

        return ResponseEntity.ok(memberService.verifyEmail(signUpVerificationDto.getEmail(), code, memberId));
    }


}
