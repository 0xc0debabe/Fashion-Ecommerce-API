package hmw.ecommerce.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import hmw.ecommerce.entity.dto.member.CustomUserDetails;
import hmw.ecommerce.entity.dto.member.LoginDto;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.exceptions.ParseException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final static long JWT_EXPIRATION_TIME = 60 * 60 * 24L * 1000;

    /**
     * 사용자가 로그인 요청을 할 때 호출되는 메서드입니다.
     * 요청 본문에서 로그인 ID와 비밀번호를 추출하고, 이를 통해 인증을 시도합니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 인증된 Authentication 객체
     * @throws AuthenticationException 인증 실패 시 발생
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LoginDto.Request loginRequest = objectMapper.readValue(request.getInputStream(), LoginDto.Request.class);
            String loginId = loginRequest.getLoginId();
            String password = loginRequest.getPassword();

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginId, password, null);
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new ParseException(ErrorCode.FAIL_TO_PARSE);
        }

    }

    /**
     * 인증에 성공하면 호출됩니다.
     * JWT 토큰을 생성하고, 이를 응답 헤더에 추가합니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param chain 필터 체인
     * @param authentication 인증된 사용자 정보
     * @throws IOException 입출력 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String loginId = customUserDetails.getLoginId();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();
        String token = jwtUtil.createJwt(loginId, role, JWT_EXPIRATION_TIME);

        response.addHeader(Const.AUTHORIZATION, Const.BEARER + token);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(loginId + " : 로그인 성공");
    }

    /**
     * 인증에 실패하면 호출됩니다.
     * 에러 메시지를 응답에 작성하고, HTTP 상태 코드를 401로 설정합니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param failed 인증 실패 시 예외
     * @throws IOException 입출력 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("아이디와 패스워드가 일치하지 않습니다.");
    }

}
