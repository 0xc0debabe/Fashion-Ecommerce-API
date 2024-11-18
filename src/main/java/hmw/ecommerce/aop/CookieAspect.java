package hmw.ecommerce.aop;

import hmw.ecommerce.entity.Item;
import hmw.ecommerce.entity.vo.Const;
import hmw.ecommerce.service.ItemService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;


/**
 * 쿠키를 기반으로 상품의 조회수를 업데이트하는 Aspect 클래스.
 * 동일 사용자가 동일 상품을 여러 번 조회해도 중복 카운트가 되지 않도록 처리함.
 */
@Component
@RequiredArgsConstructor
@Aspect
@Slf4j
public class CookieAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ItemService itemService;


    /**
     * 특정 컨트롤러 메서드 호출 전에 실행되어 조회수와 랭킹 정보를 갱신.
     * @param joinPoint 조인포인트를 이용해 getItemDetail메서드의 첫번째 인자인 itemId를 가져옴
     */
    @Before("execution(* hmw.ecommerce.controller.ItemController.getItemDetail(..))")
    public void checkAndUpdateViewCount(JoinPoint joinPoint){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String itemIdStringType = String.valueOf(joinPoint.getArgs()[0]);
        Long itemId = (Long) joinPoint.getArgs()[0];
        Item findItem = itemService.findByItemId(itemId);
        Cookie[] cookies = request.getCookies();
        Cookie cookie = findCookie(cookies);

        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();


        if (cookie != null) {
            if (!cookie.getValue().contains("[" + itemIdStringType + "]")) {
                cookie.setValue(cookie.getValue() + "[" + itemIdStringType + "]");
                findItem.addViewCount();
                redisTemplate.opsForZSet().incrementScore(Const.RANKING_KEY, itemId, 1);
            }
            cookie.setPath("/");
            Objects.requireNonNull(response).addCookie(cookie);
        } else {
            Cookie newCookie = new Cookie(Const.VIEW_COUNT, "[" + itemIdStringType + "]");
            newCookie.setPath("/");
            findItem.addViewCount();
            redisTemplate.opsForZSet().incrementScore(Const.RANKING_KEY, itemId, 1);
            Objects.requireNonNull(response).addCookie(newCookie);
        }
    }

    /**
     * 쿠키 배열에서 특정 이름(Const.VIEW_COUNT)에 해당하는 쿠키를 찾아 반환.
     *
     * @param cookies 쿠키 배열
     * @return 찾은 쿠키 또는 null
     */
    private  Cookie findCookie(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (Const.VIEW_COUNT.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }

        return null;
    }
}
