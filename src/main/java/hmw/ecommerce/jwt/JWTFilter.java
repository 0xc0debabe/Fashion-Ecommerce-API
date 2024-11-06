package hmw.ecommerce.jwt;

import hmw.ecommerce.entity.Member;
import hmw.ecommerce.entity.dto.CustomUserDetails;
import hmw.ecommerce.entity.vo.ConstJWT;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.MemberException;
import hmw.ecommerce.service.MemberService;
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
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(ConstJWT.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith(ConstJWT.BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.split(" ")[1];

        if (jwtUtil.isExpired(token)) {
//            filterChain.doFilter(request, response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (isTokenBlacklisted(token)) {
//            filterChain.doFilter(request, response);
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


    private boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }

}
