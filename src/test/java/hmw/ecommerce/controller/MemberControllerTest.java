package hmw.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hmw.ecommerce.entity.dto.SignUpForm;
import hmw.ecommerce.entity.vo.Address;
import hmw.ecommerce.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @MockBean
    private MemberService memberService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void signUp_success() throws Exception{
        //given
        given(memberService.signUp(any()))
                .willReturn(formResponse);

        //when //then
        mockMvc.perform(post("/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("name"))
                .andExpect(jsonPath("$.email").value("email@naver.com"));
     }

     @Test
     void signUp_validation() throws Exception {
         //given

         //when

         //then
         mockMvc.perform(post("/signUp")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(objectMapper.writeValueAsString(SignUpForm.Request.builder()
                                         .username(null)
                                         .password("123")
                                         .email("aa")
                                 .build())))
                 .andExpect(status().isBadRequest())
                 .andExpect(result -> {
                     String content = result.getResponse().getContentAsString();
                     System.out.println(content);
                 });

     }


}