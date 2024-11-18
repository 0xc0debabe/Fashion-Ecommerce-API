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

    /**
     * 회원가입 처리
     *
     * @param request 회원가입 정보
     * @param bindingResult 입력 값 검증 결과
     * @return 회원가입 결과
     */
    @PostMapping
    public ResponseEntity<?> signUp(
            @Valid @RequestBody SignUpDto.Request request,
            BindingResult bindingResult) {


        return ResponseEntity.ok(memberService.signUp(request));
    }

    /**
     * 로그인 ID 중복 체크
     *
     * @param loginId 중복 여부를 확인할 로그인 ID
     * @return 중복 여부 확인 결과
     */
    @GetMapping("/duplicate-check")
    public ResponseEntity<?> duplicateCheckLoginId(
            @RequestParam(name = "loginId") String loginId) {

        return ResponseEntity.ok(memberService.duplicateCheckLoginId(loginId));
    }

    /**
     * 이메일 인증 처리
     *
     * @param signUpVerificationDto 이메일 인증에 필요한 데이터
     * @param bindingResult 입력 값 검증 결과
     * @param code 인증 코드
     * @param token 사용자의 인증 토큰
     * @return 이메일 인증 결과
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(
            @Valid @RequestBody SignUpVerificationDto signUpVerificationDto,
            BindingResult bindingResult,
            @RequestParam(name = "code") String code,
            @RequestHeader(Const.AUTHORIZATION) String token) {

        return ResponseEntity.ok(memberService.verifyEmail(signUpVerificationDto.getEmail(), code, token));
    }

    /**
     * 로그아웃 처리
     *
     * @param token 로그아웃을 요청하는 사용자의 인증 토큰
     * @return 로그아웃 처리 결과
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(Const.AUTHORIZATION) String token) {
        return ResponseEntity.ok(memberService.logout(token));
    }


}
