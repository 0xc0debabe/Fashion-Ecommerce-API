package hmw.ecommerce.controller;

import hmw.ecommerce.entity.dto.SignUpForm;
import hmw.ecommerce.entity.dto.SignUpVerificationDto;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.MemberException;
import hmw.ecommerce.entity.vo.ConstJWT;
import hmw.ecommerce.jwt.JWTUtil;
import hmw.ecommerce.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
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
            @Valid @RequestBody SignUpForm.Request request,
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
            @RequestHeader(ConstJWT.AUTHORIZATION) String token) {
        if (token == null || !token.startsWith(ConstJWT.BEARER)) {
            throw new MemberException(ErrorCode.INVALID_ACCESS);
        }
        String jwtToken = token.replace(ConstJWT.BEARER, "");
        return ResponseEntity.ok(memberService.verifyEmail(signUpVerificationDto.getEmail(), code, jwtToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(ConstJWT.AUTHORIZATION) String token) {

        if (token == null || !token.startsWith(ConstJWT.BEARER)) {
            throw new MemberException(ErrorCode.INVALID_ACCESS);
        }

        String jwtToken = token.replace(ConstJWT.BEARER, "");
        return ResponseEntity.ok(memberService.logout(jwtToken));
    }

}
