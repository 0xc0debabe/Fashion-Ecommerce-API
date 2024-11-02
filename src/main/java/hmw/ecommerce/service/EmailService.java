package hmw.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, Object> redisTemplate;

    public String sendEmail(String to) {
        MimeMessage message = mailSender.createMimeMessage();
        String code = UUID.randomUUID().toString().substring(0, 6);
        redisTemplate.opsForValue().set(to, code, 10, TimeUnit.MINUTES);

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("메일 인증코드");
            String emailContent = "<br>회원가입을 위한 메일 인증코드입니다.<p>" +
                    "<p>" + code + "</p>" +
                    "<p>만료시간은 10분입니다.</p>";

            helper.setText(emailContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return to;
    }


}
