package kr.online.routineconnect.controller;

import static kr.online.routineconnect.ApiDocumentUtils.getDocumentRequest;
import static kr.online.routineconnect.ApiDocumentUtils.getDocumentResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultJwsHeader;
import java.lang.reflect.Constructor;
import java.util.Collections;
import kr.online.routineconnect.config.ObjectMapperConfig;
import kr.online.routineconnect.dto.CheckDuplicatedResponse;
import kr.online.routineconnect.dto.RefreshAccessTokenRequest;
import kr.online.routineconnect.dto.Response;
import kr.online.routineconnect.dto.SignInRequest;
import kr.online.routineconnect.dto.SignInResponse;
import kr.online.routineconnect.dto.SignUpRequest;
import kr.online.routineconnect.service.SignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(RestDocumentationExtension.class)
@WebMvcTest(
        controllers = SignController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ObjectMapperConfig.class)
)
@AutoConfigureRestDocs
class SignControllerTest {

    protected final String API_PREFIX = "/sign-api";
    protected final String email = "gdhong@routineconnect.com";
    protected final String password = "password";
    protected final String name = "홍길동";
    protected final SignUpRequest signUpRequest = SignUpRequest.builder()
            .email(email)
            .password(password)
            .name(name)
            .build();
    protected final SignInRequest signInRequest = SignInRequest.builder()
            .email(email)
            .password(password)
            .build();
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockBean
    protected SignService signService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    public void 이메일미중복Test() throws Exception {
        given(signService.checkUserEmailDuplicated(any(String.class)))
                .willReturn(CheckDuplicatedResponse.SUCCESS);

        var result = this.mockMvc.perform(
                get(API_PREFIX + "/check-user-email")
                        .param("email", email)
        );

        result.andExpect(status().isOk())
                .andDo(document("이메일미중복",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("email").description("이메일")
                        ),
                        responseFields(
                                fieldWithPath("is_duplicated").type(JsonFieldType.BOOLEAN).description("중복 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("중복 여부 메시지")
                        )
                ));
    }

    @Test
    public void 이메일중복Test() throws Exception {
        given(signService.checkUserEmailDuplicated(any(String.class)))
                .willReturn(CheckDuplicatedResponse.ERROR);

        var result = this.mockMvc.perform(
                get(API_PREFIX + "/check-user-email")
                        .param("email", email)
        );

        result.andExpect(status().isOk())
                .andDo(document("이메일중복",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("email").description("이메일")
                        ),
                        responseFields(
                                fieldWithPath("is_duplicated").type(JsonFieldType.BOOLEAN).description("중복 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("중복 여부 메시지")
                        )
                ));
    }

    @Test
    public void 이름미중복Test() throws Exception {
        given(signService.checkUserNameDuplicated(any(String.class)))
                .willReturn(CheckDuplicatedResponse.SUCCESS);

        var result = this.mockMvc.perform(
                get(API_PREFIX + "/check-user-name")
                        .param("name", name)
        );

        result.andExpect(status().isOk())
                .andDo(document("이름미중복",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("name").description("이름")
                        ),
                        responseFields(
                                fieldWithPath("is_duplicated").type(JsonFieldType.BOOLEAN).description("중복 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("중복 여부 메시지")
                        )
                ));
    }

    @Test
    public void 이름중복Test() throws Exception {
        given(signService.checkUserNameDuplicated(any(String.class)))
                .willReturn(CheckDuplicatedResponse.ERROR);

        var result = this.mockMvc.perform(
                get(API_PREFIX + "/check-user-name")
                        .param("name", name)
        );

        result.andExpect(status().isOk())
                .andDo(document("이름중복",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("name").description("이름")
                        ),
                        responseFields(
                                fieldWithPath("is_duplicated").type(JsonFieldType.BOOLEAN).description("중복 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("중복 여부 메시지")
                        )
                ));
    }

    @Test
    public void 회원가입Test() throws Exception {
        given(signService.signUp(any(SignUpRequest.class)))
                .willReturn(Response.SIGN_UP_SUCCESS);

        var result = this.mockMvc.perform(
                post(API_PREFIX + "/sign-up").content(objectMapper.writeValueAsString(signUpRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andDo(document("회원가입",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("is_success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("성공 여부 메시지")
                        )
                ));
    }

    @Test
    public void 회원가입실패_이메일중복Test() throws Exception {
        given(signService.signUp(any(SignUpRequest.class)))
                .willThrow(new DuplicateKeyException("해당 이메일은 이미 사용 중 입니다."));

        var result = this.mockMvc.perform(
                post(API_PREFIX + "/sign-up").content(objectMapper.writeValueAsString(signUpRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isConflict())
                .andDo(document("회원가입실패_이메일중복",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("is_success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("성공 여부 메시지"))
                ));
    }

    @Test
    public void 회원가입실패_이름중복Test() throws Exception {
        given(signService.signUp(any(SignUpRequest.class)))
                .willThrow(new DuplicateKeyException("해당 이름은 이미 사용 중 입니다."));

        var result = this.mockMvc.perform(
                post(API_PREFIX + "/sign-up").content(objectMapper.writeValueAsString(signUpRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isConflict())
                .andDo(document("회원가입실패_이름중복",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("is_success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("성공 여부 메시지"))
                ));
    }

    @Test
    public void 로그인Test() throws Exception {
        SignInResponse signInResponse = SignInResponse.builder()
                .accessToken("it.is.access-token")
                .refreshToken("it.is.refresh-token")
                .build();

        given(signService.signIn(any(SignInRequest.class)))
                .willReturn(signInResponse);

        var result = this.mockMvc.perform(
                post(API_PREFIX + "/sign-in").content(objectMapper.writeValueAsString(signInRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andDo(document("로그인",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("access_token").type(JsonFieldType.STRING).description("액세스 토큰"),
                                fieldWithPath("refresh_token").type(JsonFieldType.STRING).description("리프레시 토큰"),
                                fieldWithPath("type").type(JsonFieldType.STRING).description("액세스 토큰 타입")
                        )
                ));
    }

    @Test
    public void 로그인실패Test() throws Exception {
        given(signService.signIn(any(SignInRequest.class)))
                .willThrow(new BadCredentialsException("잘못된 아이디 혹은 비밀번호입니다."));

        var result = this.mockMvc.perform(
                post(API_PREFIX + "/sign-in").content(objectMapper.writeValueAsString(signInRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isUnauthorized())
                .andDo(document("로그인실패",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("is_success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("성공 여부 메시지")
                        )
                ));
    }

    @Test
    public void 액세스토큰재발급성공Test() throws Exception {
        RefreshAccessTokenRequest request = RefreshAccessTokenRequest.builder()
                .grantType("refresh-token")
                .refreshToken("refresh-token.stored.in-client")
                .build();

        SignInResponse response = SignInResponse.builder()
                .accessToken("reissued.access-token.with-refresh-token")
                .build();

        given(signService.refreshAccessToken(any(RefreshAccessTokenRequest.class)))
                .willReturn(response);

        var result = this.mockMvc.perform(
                post(API_PREFIX + "/access-token").content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andDo(document("액세스토큰재발급성공",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("grant_type").type(JsonFieldType.STRING).description("그랜트 타입"),
                                fieldWithPath("refresh_token").type(JsonFieldType.STRING).description("리프레시 토큰")
                        ),
                        responseFields(
                                fieldWithPath("access_token").type(JsonFieldType.STRING)
                                        .description("재발급된 액세스 토큰"),
                                fieldWithPath("type").type(JsonFieldType.STRING).description("액세스 토큰 타입")
                        )
                ));
    }

    @Test
    public void 액세스토큰재발급실패Test() throws Exception {
        // 생성자에 접근하기 위해 Constructor 객체를 얻음
        Constructor<DefaultClaims> constructor = DefaultClaims.class.getDeclaredConstructor();
        // 생성자 접근 가능하도록 설정
        constructor.setAccessible(true);
        // 생성자 호출하여 인스턴스 생성
        DefaultClaims defaultClaims = constructor.newInstance();

        RefreshAccessTokenRequest request = RefreshAccessTokenRequest.builder()
                .grantType("refresh-token")
                .refreshToken("refresh-token.stored.in-client")
                .build();

        given(signService.refreshAccessToken(any(RefreshAccessTokenRequest.class)))
                .willThrow(
                        new ExpiredJwtException(new DefaultJwsHeader(Collections.emptyMap()), defaultClaims,
                                "만료된 리프레시 토큰입니다."
                        ));

        var result = this.mockMvc.perform(
                post(API_PREFIX + "/access-token").content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isUnauthorized())
                .andDo(document("액세스토큰재발급실패",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("grant_type").type(JsonFieldType.STRING).description("그랜트 타입"),
                                fieldWithPath("refresh_token").type(JsonFieldType.STRING).description("리프레시 토큰")
                        ),
                        responseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("만료된 토큰 에러 설명 메시지")
                        )
                ));
    }
}