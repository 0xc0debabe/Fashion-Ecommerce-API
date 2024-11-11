package hmw.ecommerce.controller;

import hmw.ecommerce.entity.dto.member.SignUpDto;
import hmw.ecommerce.entity.dto.member.SignUpVerificationDto;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/member")
@RestController
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<?> signUp(
            @Valid @RequestBody SignUpDto.Request request,
            BindingResult bindingResult) {


        return ResponseEntity.ok(memberService.signUp(request));
    }

    @GetMapping("/duplicate-check")
    public ResponseEntity<?> duplicateCheckLoginId(
            @RequestParam(name = "loginId") String loginId) {

        return ResponseEntity.ok(memberService.duplicateCheckLoginId(loginId));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(
            @Valid @RequestBody SignUpVerificationDto signUpVerificationDto,
            BindingResult bindingResult,
            @RequestParam(name = "code") String code,
            @RequestHeader(Const.AUTHORIZATION) String token) {

        return ResponseEntity.ok(memberService.verifyEmail(signUpVerificationDto.getEmail(), code, token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(Const.AUTHORIZATION) String token) {
        return ResponseEntity.ok(memberService.logout(token));
    }


}
