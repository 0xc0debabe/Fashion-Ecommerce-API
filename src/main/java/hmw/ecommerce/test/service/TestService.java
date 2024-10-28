package hmw.ecommerce.test.service;

import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.dto.SignUpForm;
import hmw.ecommerce.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void signUp(SignUpForm form) {
        Member member = new Member();
        member.setEmail(form.getEmail());
        member.setPassword(bCryptPasswordEncoder.encode(form.getPassword()));
        member.setRole("ROLE_ADMIN");
        memberRepository.save(member);
    }

    public void login(SignUpForm form) {

    }

}
