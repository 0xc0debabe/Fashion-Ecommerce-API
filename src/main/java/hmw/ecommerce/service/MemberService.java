package hmw.ecommerce.service;

import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.dto.member.SignUpDto;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.exception.EmailException;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.MemberException;
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

    public SignUpDto.Response signUp(SignUpDto.Request request) {
        if (memberRepository.existsByLoginId(request.getLoginId())) {
            throw new MemberException(ErrorCode.ALREADY_EXIST_LOGIN_ID);
        }

        request.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        return SignUpDto.Response.fromEntity(memberRepository.save(request.toEntity(request.isSeller())));
    }

    public boolean duplicateCheckLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

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

    public String logout(String token) {
        if (token == null || !token.startsWith(Const.BEARER)) {
            throw new MemberException(ErrorCode.INVALID_ACCESS);
        }
        String jwtToken = token.replace(Const.BEARER, "");
        addToBlacklist(jwtToken);
        return jwtUtil.getLoginId(jwtToken);
    }

    private void addToBlacklist(String token) {
        redisTemplate.opsForValue().set(token, "blacklisted", 1, TimeUnit.DAYS);
    }

}
