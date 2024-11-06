package hmw.ecommerce.service;

import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.dto.CustomUserDetails;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.MemberException;
import hmw.ecommerce.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member findMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_LOGIN_ID));

        return new CustomUserDetails(findMember);
    }

}