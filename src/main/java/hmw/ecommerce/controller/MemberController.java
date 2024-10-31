package hmw.ecommerce.controller;

import hmw.ecommerce.entity.dto.SignUpForm;
import hmw.ecommerce.entity.dto.SignUpVerification;
import hmw.ecommerce.exception.Validation;
import hmw.ecommerce.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("member")
@RestController
public class MemberController {

    private final MemberService memberService;

    @PostMapping("signUp")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpForm.Request request,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Validation.validateDtoErrors(bindingResult);
        }

        return ResponseEntity.ok(memberService.signUp(request));
    }

    @PostMapping("valid")
    public ResponseEntity<?> verification(@RequestBody SignUpVerification key,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Validation.validateDtoErrors(bindingResult);
        }

        return ResponseEntity.ok(memberService.verification(key.getVerificationKey()));
    }

}
