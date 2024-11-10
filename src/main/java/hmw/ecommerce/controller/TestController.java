package hmw.ecommerce.controller;

import hmw.ecommerce.entity.vo.Const;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @PreAuthorize(Const.HAS_ROLE_MEMBER_OR_SELLER)
    @GetMapping("/test")
    public String test() {
        return "ok";
    }

    @GetMapping("/test/remove")
    public String remove(HttpServletResponse response) {
        // 쿠키 삭제: 이름이 "VIEW_COUNT"인 쿠키를 삭제
        Cookie cookie = new Cookie("VIEW_COUNT", "");  // 쿠키의 값을 빈 문자열로 설정
        cookie.setMaxAge(0);  // 쿠키의 만료 시간을 0으로 설정하여 삭제
        cookie.setPath("/");  // 쿠키의 경로를 설정 (일반적으로 "/"로 설정하면 모든 경로에서 유효)
        response.addCookie(cookie);  // 응답에 쿠키 추가

        return "ok";
    }

}