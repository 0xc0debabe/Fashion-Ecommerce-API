package hmw.ecommerce.service;

import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.dto.member.SignUpDto;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.exception.exceptions.EmailException;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.exceptions.MemberException;
import hmw.ecommerce.jwt.JWTUtil;
import hmw.ecommerce.repository.entity.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JWTUtil jwtUtil;

    /**
     * 회원가입 처리
     *
     * @param request 회원가입 정보
     * @return 회원가입 응답 DTO
     */
    public SignUpDto.Response signUp(SignUpDto.Request request) {
        if (memberRepository.existsByLoginId(request.getLoginId())) {
            throw new MemberException(ErrorCode.ALREADY_EXIST_LOGIN_ID);
        }

        request.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        return SignUpDto.Response.fromEntity(memberRepository.save(request.toEntity(request.isSeller())));
    }

    /**
     * 로그인 ID 중복 여부 확인
     *
     * @param loginId 확인할 로그인 ID
     * @return 중복 여부
     */
    public boolean duplicateCheckLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    /**
     * 이메일 인증 처리
     *
     * @param email 사용자 이메일
     * @param code 인증 코드
     * @param token 사용자의 JWT 토큰
     * @return 인증 성공 후 응답 DTO
     */
    public SignUpDto.Response verifyEmail(String email, String code, String token) {
        String loginId = jwtUtil.extractLoginIdFromToken(token);

        Member findMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_LOGIN_ID));

        if (Boolean.FALSE.equals(redisTemplate.hasKey(email))) {
            throw new EmailException(ErrorCode.INVALID_CODE);
        }

        if (!Objects.equals(redisTemplate.opsForValue().get(email), code)) {
            throw new MemberException(ErrorCode.INVALID_CODE);
        }

        findMember.verifySuccess(email);
        redisTemplate.delete(findMember.getEmail());

        return SignUpDto.Response.fromEntity(findMember);
    }

    /**
     * 로그아웃 처리
     *
     * @param token 사용자의 인증 토큰
     * @return 로그아웃 후 사용자 ID
     */
    public String logout(String token) {
        if (token == null || !token.startsWith(Const.BEARER)) {
            throw new MemberException(ErrorCode.INVALID_ACCESS);
        }
        String jwtToken = token.replace(Const.BEARER, "");
        addToBlacklist(jwtToken);
        return jwtUtil.getLoginId(jwtToken);
    }

    /**
     * 토큰을 블랙리스트에 추가
     *
     * @param token 로그아웃된 사용자의 JWT 토큰
     */
    private void addToBlacklist(String token) {
        redisTemplate.opsForValue().set(token, "blacklisted", 1, TimeUnit.DAYS);
    }

}
