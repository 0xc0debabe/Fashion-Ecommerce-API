package hmw.ecommerce.service;

import hmw.ecommerce.entity.dto.SignUpForm;
import hmw.ecommerce.entity.vo.Address;
import hmw.ecommerce.exception.MemberException;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.repository.MemberRepository;
import jakarta.mail.internet.MimeMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MemberService memberService;

    SignUpForm.Request formRequest;
    SignUpForm.Response formResponse;

    @BeforeEach
    void init() {
        formRequest = SignUpForm.Request.builder()
                .username("name")
                .email("kongminoo@naver.com")
                .password("1234567890")
                .address(new Address("city", "street", "zipcode"))
                .role("USER")
                .build();

        formResponse = SignUpForm.Response.builder()
                .username("name")
                .email("kongminoo@naver.com")
                .address(new Address("city", "street", "zipcode"))
                .build();

    }

    @Test
    void signUp_success() throws Exception {
        //given
        MimeMessage mimeMessage = mock(MimeMessage.class);

        given(memberRepository.existsByEmail(any())).willReturn(false);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        //when
        String response = memberService.signUp(formRequest);

        //then
        Assertions.assertThat(response).isEqualTo("인증 이메일을 성공적으로 보냈습니다. 메일을 확인하고 인증 코드를 입력하세요.");
     }

     @Test
     void signUp_ALEADY_EXISTS_EMAIL() throws Exception {

         //given
         given(memberRepository.existsByEmail(any())).willReturn(true);

         //when
         MemberException exception = assertThrows(MemberException.class,
                 () -> memberService.signUp(formRequest));

         //then
         Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ALREADY_EXIST_EMAIL);
      }

}