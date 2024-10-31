package hmw.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hmw.ecommerce.entity.dto.SignUpForm;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.exception.MemberException;
import hmw.ecommerce.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, Object> redisTemplate;

    public String signUp(SignUpForm.Request request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new MemberException(ErrorCode.ALREADY_EXIST_EMAIL);
        }

        sendEmail(request);
        return "인증 이메일을 성공적으로 보냈습니다. 메일을 확인하고 인증 코드를 입력하세요.";
    }


    public SignUpForm.Response verification(String key) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            throw new MemberException(ErrorCode.NOT_EXIST_VERIFICATIONCODE);
        }

        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String requestJson = (String) valueOperations.get(key);
        SignUpForm.Request request = getRequest(requestJson);
        redisTemplate.delete(key);
        return SignUpForm.Response.fromEntity(
                memberRepository.save(request.toEntity()));
    }

    private SignUpForm.Request getRequest(String requestJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        SignUpForm.Request request;
        try {
            request = objectMapper.readValue(requestJson, SignUpForm.Request.class);
        } catch (Exception e) {
            log.error("Failed to parse requestJson: {}", requestJson, e);
            throw new MemberException(ErrorCode.INVALID_VERIFICATIONCODE);
        }
        return request;
    }

    private void sendEmail(SignUpForm.Request request) {
        MimeMessage message = mailSender.createMimeMessage();
        String uuid = UUID.randomUUID().toString();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String requestJson = objectMapper.writeValueAsString(request);
            redisTemplate.opsForValue().set(uuid, requestJson, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(request.getEmail());
            helper.setSubject("메일 인증코드");
            helper.setText("<h1>회원가입을 위한 메일 인증코드입니다.</h1><p>" + uuid + "</p>", true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}
