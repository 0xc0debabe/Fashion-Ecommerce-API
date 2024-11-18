package hmw.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 주어진 이메일 주소로 인증 메일을 전송합니다.
     * 인증 코드는 10분 동안 유효하며 Redis에 저장됩니다.
     *
     * @param to 인증 메일을 보낼 이메일 주소
     * @return 인증 메일을 보낸 이메일 주소
     */
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
