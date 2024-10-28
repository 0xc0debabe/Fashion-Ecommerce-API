package hmw.ecommerce.service;

import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.dto.SignUpForm;
import hmw.ecommerce.exception.CustomException;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public SignUpForm.Response signUp(SignUpForm.Request request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.ALREADY_EXIST_EMAIL);
        }

        return SignUpForm.Response.fromEntity(
                memberRepository.save(request.toEntity()));
    }

}
