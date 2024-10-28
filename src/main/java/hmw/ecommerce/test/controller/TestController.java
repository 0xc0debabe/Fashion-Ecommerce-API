package hmw.ecommerce.test.controller;

import hmw.ecommerce.entity.dto.SignUpForm;
import hmw.ecommerce.test.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @GetMapping("/admin")
    public String adminPage() {
        return "ok";
    }

    @PostMapping("/signUp")
    public String join(SignUpForm form) {
        testService.signUp(form);
        return "ok";
    }

    @PostMapping("/login")
    public String login(SignUpForm form) {
        testService.login(form);
        return "ok";
    }
}
