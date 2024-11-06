package hmw.ecommerce.service;

import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.dto.SignUpForm;
import hmw.ecommerce.exception.EmailException;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.MemberException;
import hmw.ecommerce.repository.MemberRepository;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SignUpForm.Response signUp(SignUpForm.Request request) {
        if (memberRepository.existsByLoginId(request.getLoginId())) {
            throw new MemberException(ErrorCode.ALREADY_EXIST_LOGIN_ID);
        }

        request.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        return SignUpForm.Response.fromEntity(memberRepository.save(request.toEntity(request.isSeller())));
    }

    public boolean duplicateCheckLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    public SignUpForm.Response verifyEmail(String email, String code, String loginId) {
        Member findMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(ErrorCode.INVALID_ACCESS));

        if (Boolean.FALSE.equals(redisTemplate.hasKey(email))) {
            throw new EmailException(ErrorCode.NOT_EXIST_EMAIL);
        }

        if (!Objects.equals(redisTemplate.opsForValue().get(email), code)) {
            throw new MemberException(ErrorCode.INVALID_CODE);
        }

        findMember.verifySuccess(email);
        redisTemplate.delete(findMember.getEmail());

        return SignUpForm.Response.fromEntity(findMember);
    }

    public boolean matchesLoginIdAndPassword(String loginId, String password) {
        Member findMember = memberRepository.findByLoginId(loginId).orElseThrow(
                () -> new MemberException(ErrorCode.NOT_EXIST_LOGIN_ID));

        return bCryptPasswordEncoder.matches(findMember.getPassword(), password);
    }

}
