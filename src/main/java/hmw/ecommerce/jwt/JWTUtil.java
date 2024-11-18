package hmw.ecommerce.jwt;

import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.exceptions.AuthException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JWTUtil {

    private SecretKey secretKey;

    /**
     * 생성자: JWT 서명을 위한 비밀 키를 초기화합니다.
     *
     * @param secret 비밀 키
     */
    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /**
     * JWT 토큰에서 로그인 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 로그인 ID
     * @throws AuthException 토큰이 유효하지 않으면 예외 발생
     */
    public String extractLoginIdFromToken(String token) {
        if (token == null || !token.startsWith(Const.BEARER)) {
            throw new AuthException(ErrorCode.INVALID_ACCESS);
        }
        String jwtToken = token.replace(Const.BEARER, "");
        return getLoginId(jwtToken);
    }


    /**
     * JWT 토큰에서 로그인 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 로그인 ID
     */
    public String getLoginId(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("loginId", String.class);
    }

    /**
     * JWT 토큰에서 비밀번호를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 비밀번호
     */
    public String getPassword(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("password", String.class);
    }

    /**
     * JWT 토큰에서 사용자 역할을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 역할
     */
    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    /**
     * JWT 토큰이 만료되었는지 확인합니다.
     *
     * @param token JWT 토큰
     * @return 토큰이 만료되었으면 true, 그렇지 않으면 false
     */
    public Boolean isExpired(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    /**
     * JWT 토큰을 생성합니다.
     *
     * @param loginId 사용자 로그인 ID
     * @param role 사용자 역할
     * @param expiredMs 만료 시간 (밀리초)
     * @return 생성된 JWT 토큰
     */
    public String createJwt(String loginId, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("loginId", loginId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

}
