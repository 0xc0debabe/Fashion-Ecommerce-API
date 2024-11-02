package hmw.ecommerce.service;

import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.dto.SignUpForm;
import hmw.ecommerce.exception.EmailException;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.MemberException;
import hmw.ecommerce.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public SignUpForm.Response signUp(SignUpForm.Request request) {
        if (memberRepository.existsByLoginId(request.getLoginId())) {
            throw new MemberException(ErrorCode.ALREADY_EXIST_LOGIN_ID);
        }

        return SignUpForm.Response.fromEntity(
                memberRepository.save(request.toEntity())
        );
    }

    public boolean duplicateCheckLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    public String verifyEmail(String email, String code, Long memberId) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(email))) {
            throw new EmailException(ErrorCode.NOT_EXIST_EMAIL);
        }

        if (!Objects.equals(redisTemplate.opsForValue().get(email), code)) {
            throw new MemberException(ErrorCode.INVALID_CODE);
        }

        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_EMAIL));
        findMember.verifySuccess();
        redisTemplate.delete(email);

        return email;
    }

}
