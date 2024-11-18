package hmw.ecommerce.jwt;

import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.dto.member.CustomUserDetails;
import hmw.ecommerce.entity.vo.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * HTTP 요청을 필터링하여 유효한 JWT 토큰을 가진 사용자인지 확인하는 메서드.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException 입출력 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(Const.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith(Const.BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.split(" ")[1];

        if (jwtUtil.isExpired(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isTokenBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("로그아웃된 사용자입니다.");
            return;
        }

        String findLoginId = jwtUtil.getLoginId(token);
        String findPassword = jwtUtil.getPassword(token);
        String findRole = jwtUtil.getRole(token);

        Member findMember = Member.builder()
                .loginId(findLoginId)
                .password(findPassword)
                .role(findRole)
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(findMember);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    /**
     * JWT 토큰이 블랙리스트에 포함되어 있는지 확인하는 메서드.
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰이 블랙리스트에 포함되어 있으면 true, 아니면 false
     */
    private boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }

}
