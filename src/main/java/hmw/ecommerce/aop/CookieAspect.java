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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Aspect
@Slf4j
public class CookieAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ItemService itemService;

    @Before("execution(* hmw.ecommerce.controller.ItemController.getItemDetail(..))")
    public void checkAndUpdateViewCount(JoinPoint joinPoint) throws Throwable, InvocationTargetException {
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
