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
        Cookie cookie = new Cookie("VIEW_COUNT", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        Cookie cookie2 = new Cookie(Const.CART_ITEMS, "");
        cookie2.setMaxAge(0);
        cookie2.setPath("/");
        response.addCookie(cookie2);
        return "ok";
    }

}