package hmw.ecommerce.service;

import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.dto.member.CustomUserDetails;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.exceptions.MemberException;
import hmw.ecommerce.repository.entity.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * 주어진 로그인 ID에 해당하는 사용자의 정보를 로드하여 UserDetails 객체로 반환합니다.
     * 로그인 ID가 존재하지 않으면 MemberException 예외를 던집니다.
     *
     * @param loginId 사용자 로그인 ID
     * @return 사용자의 UserDetails
     * @throws UsernameNotFoundException 로그인 ID에 해당하는 사용자가 없을 경우 예외 발생
     */
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member findMember = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_LOGIN_ID));

        return new CustomUserDetails(findMember);
    }

}