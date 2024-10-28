package hmw.ecommerce.service;

import hmw.ecommerce.entity.dto.SignUpForm;
import hmw.ecommerce.entity.vo.Address;
import hmw.ecommerce.exception.CustomException;
import hmw.ecommerce.exception.ErrorCode;
import hmw.ecommerce.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    SignUpForm.Request formRequest;
    SignUpForm.Response formResponse;

    @BeforeEach
    void init() {
        formRequest = SignUpForm.Request.builder()
                .username("name")
                .email("email@naver.com")
                .password("1234567890")
                .address(new Address("city", "street", "zipcode"))
                .role("USER")
                .build();

        formResponse = SignUpForm.Response.builder()
                .username("name")
                .email("email@naver.com")
                .address(new Address("city", "street", "zipcode"))
                .build();

    }

    @Test
    void signUp_success() throws Exception {
        //given
        given(memberRepository.existsByEmail(any()))
                .willReturn(false);

        given(memberRepository.save(any()))
                .willReturn(formRequest.toEntity());

        //when
        SignUpForm.Response response = memberService.signUp(formRequest);

        //then
        Assertions.assertThat(response.getUsername()).isEqualTo("name");
        Assertions.assertThat(response.getEmail()).isEqualTo("email@naver.com");
        Assertions.assertThat(response.getAddress().getZipcode()).isEqualTo("zipcode");
     }

     @Test
     void signUp_ALEADY_EXISTS_EMAIL() throws Exception {
         //given
         given(memberRepository.existsByEmail(any()))
                 .willReturn(true);
         //when
         CustomException exception = assertThrows(CustomException.class,
                 () -> memberService.signUp(formRequest));

         //then
         Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ALREADY_EXIST_EMAIL);
      }

}