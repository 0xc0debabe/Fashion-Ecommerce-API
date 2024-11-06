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
    private final JWTUtil jwtUtil;

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
            HttpServletRequest request) {

        String jwtToken = request.getHeader(ConstJWT.AUTHORIZATION);
        if (jwtToken == null || !jwtToken.startsWith(ConstJWT.BEARER)) {
            throw new MemberException(ErrorCode.INVALID_ACCESS);
        }

        String token = jwtToken.replace(ConstJWT.BEARER, "");
        String loginId = jwtUtil.getLoginId(token);

        return ResponseEntity.ok(memberService.verifyEmail(signUpVerificationDto.getEmail(), code, loginId));
    }

}
