package kr.online.routineconnect.controller;

import static kr.online.routineconnect.ApiDocumentUtils.getDocumentRequest;
import static kr.online.routineconnect.ApiDocumentUtils.getDocumentResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestBody;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import kr.online.routineconnect.config.ObjectMapperConfig;
import kr.online.routineconnect.domain.Routine;
import kr.online.routineconnect.dto.ItemResponse;
import kr.online.routineconnect.dto.ItemUpdate;
import kr.online.routineconnect.dto.RoutineRequest;
import kr.online.routineconnect.service.RoutineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WithMockUser
@ExtendWith(RestDocumentationExtension.class)
@WebMvcTest(
        controllers = RoutineController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ObjectMapperConfig.class)
)
@AutoConfigureRestDocs
public class RoutineControllerTest {

    private final String API_PREFIX = "/api";
    private final RoutineRequest request = RoutineRequest.builder()
            .title("기상")
            .hour("아침")
            .routineDay((byte) 0b1111111)
            .shared(false)
            .createdDate(LocalDate.now())
            .build();
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockBean
    private RoutineService routineService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    public void 메인페이지조회Test() throws Exception {
        List<ItemResponse> response = Arrays.asList(
                ItemResponse.builder()
                        .itemId(1L)
                        .itemOrderId(1L)
                        .title("기상")
                        .hour("아침")
                        .position(1D)
                        .accomplishment(false)
                        .retrospect("늦잠")
                        .build(),

                ItemResponse.builder()
                        .itemId(2L)
                        .itemOrderId(2L)
                        .title("운동")
                        .hour("점심")
                        .position(2D)
                        .accomplishment(true)
                        .retrospect("오운완")
                        .build(),

                ItemResponse.builder()
                        .itemId(3L)
                        .itemOrderId(3L)
                        .title("저녁식사")
                        .hour("저녁")
                        .position(3D)
                        .accomplishment(false)
                        .retrospect("뭐먹지")
                        .build()
        );

        given(routineService.findItemsByUserOnDate(any(UserDetails.class), any(LocalDate.class)))
                .willReturn(response);

        ResultActions result = this.mockMvc.perform(
                get(API_PREFIX + "/page/{date}", LocalDate.now()));

        result.andExpect(status().isOk())
                .andDo(document("메인페이지조회",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("date").description("날짜")
                        ),
                        responseFields(
                                fieldWithPath("[]").type(JsonFieldType.ARRAY).description("해당 날짜의 아이템 배열"),
                                fieldWithPath("[].item_id").type(JsonFieldType.NUMBER).description("아이템 아이디"),
                                fieldWithPath("[].item_order_id").type(JsonFieldType.NUMBER)
                                        .description("item order 아이디"),
                                fieldWithPath("[].hour").type(JsonFieldType.STRING).description("루틴 시간"),
                                fieldWithPath("[].title").type(JsonFieldType.STRING).description("루틴 제목"),
                                fieldWithPath("[].position").type(JsonFieldType.NUMBER)
                                        .description("루틴 위치, Double 타입"),
                                fieldWithPath("[].accomplishment").type(JsonFieldType.BOOLEAN)
                                        .description("달성 여부"),
                                fieldWithPath("[].retrospect").type(JsonFieldType.STRING)
                                        .description("회고")
                        )
                ));
    }

    @Test
    public void 달성도설정Test() throws Exception {
        doNothing().when(routineService).setAccomplishment(any(UserDetails.class), anyLong(), anyBoolean());

        var result = this.mockMvc.perform(
                patch(API_PREFIX + "/page")
                        .param("item_order_id", String.valueOf(1L))
                        .content(String.valueOf(true))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isOk())
                .andDo(document("달성도설정",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("item_order_id").description("Item Order 아이디")
                        ),
                        requestBody(),
                        responseFields(
                                fieldWithPath("is_success").type(JsonFieldType.BOOLEAN).description("성공 여부")
                        )
                ));
    }

    @Test
    public void 루틴추가Test() throws Exception {
        Routine routine = Routine.builder()
                .title("기상")
                .repeatingDays(EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SUNDAY))
                .createdDate(LocalDate.now())
                .build();

        given(routineService.addRoutine(any(UserDetails.class), any(RoutineRequest.class)))
                .willReturn(routine);

        ResultActions result = this.mockMvc.perform(
                post(API_PREFIX + "/routine").content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andDo(document("루틴추가",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("루틴 제목"),
                                fieldWithPath("hour").type(JsonFieldType.STRING).description("루틴 시간").optional(),
                                fieldWithPath("routine_day").type(JsonFieldType.NUMBER).description(
                                        """
                                                루틴 반복 요일을 문자열로 나타냄. 반복하는 요일은 1, 아니면 0으로 나타냄. 왼쪽부터 \
                                                오른쪽 문자열까지 순서대로 월~일
                                                """),
                                fieldWithPath("shared").type(JsonFieldType.BOOLEAN).description("루틴 공개 여부"),
                                fieldWithPath("created_date").type(JsonFieldType.STRING).description("생성 일시")
                        ),
                        responseFields(
                                fieldWithPath("is_success").type(JsonFieldType.BOOLEAN).description("성공 여부")
                        )
                ));
    }

    @Test
    public void 루틴수정Test() throws Exception {
        RoutineRequest request = RoutineRequest.builder()
                .title("취침")
                .hour("밤")
                .routineDay((byte) 0b1111111)
                .shared(false)
                .createdDate(LocalDate.now())
                .build();

        doNothing().when(routineService)
                .updateRoutine(any(UserDetails.class), anyLong(), any(RoutineRequest.class));

        ResultActions result = this.mockMvc.perform(
                put(API_PREFIX + "/routine")
                        .param("routine_id", String.valueOf(1L))
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isOk())
                .andDo(document("루틴수정",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("routine_id").description("루틴 아이디")
                        ),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("루틴 제목"),
                                fieldWithPath("hour").type(JsonFieldType.STRING).description("루틴 시간").optional(),
                                fieldWithPath("routine_day").type(JsonFieldType.NUMBER).description(
                                        """
                                                루틴 반복 요일을 문자열로 나타냄. 반복하는 요일은 1, 아니면 0으로 나타냄. 왼쪽부터 \
                                                오른쪽 문자열까지 순서대로 월~일
                                                """),
                                fieldWithPath("shared").type(JsonFieldType.BOOLEAN).description("루틴 공개 여부"),
                                fieldWithPath("created_date").type(JsonFieldType.STRING).description("생성 일시")
                        )
                ));
    }

    @Test
    public void 아이템순서변경Test() throws Exception {
        List<ItemUpdate> request = List.of(
                new ItemUpdate(1L, 2D),
                new ItemUpdate(2L, 3D),
                new ItemUpdate(3L, 1D)
        );

        doNothing().when(routineService)
                .updateItemOrder(any(UserDetails.class), any(LocalDate.class), anyList());

        ResultActions result = this.mockMvc.perform(
                patch(API_PREFIX + "/page/{date}", LocalDate.now())
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isOk())
                .andDo(document("아이템순서변경",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("date").description("날짜")
                        ),
                        requestFields(
                                fieldWithPath("[]").type(JsonFieldType.ARRAY).description("변경된 루틴 순서 배열"),
                                fieldWithPath("[].item_order_id").type(JsonFieldType.NUMBER)
                                        .description("변경할 item_order 아이디"),
                                fieldWithPath("[].position").type(JsonFieldType.NUMBER).description(
                                        """
                                                변경할 루틴 위치. 원래 위치에서 다른 위치로 옮길 때, 옮기려는 위치 위의 루틴과 아래 \
                                                루틴의 중간값.
                                                newPosition = (upperRoutine.position + lowerRoutine.position) / 2
                                                """)
                        )
                ));
    }

    @Test
    public void 달성도조회Test() throws Exception {
        List<Float> response = Arrays.asList(0.8f, 0.9f, 0.6f, 0.7f, 1f, 0.85f, 0f);
        given(routineService.getAchievementsForWeek(any(UserDetails.class), any(LocalDate.class)))
                .willReturn(response);

        ResultActions result = this.mockMvc.perform(
                get(API_PREFIX + "/achievement/{date}", LocalDate.now()));

        result.andExpect(status().isOk())
                .andDo(document("달성도조회",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("date").description("날짜")
                        ),
                        responseFields(
                                fieldWithPath("[]").type(JsonFieldType.ARRAY).description("일주일 간의 루틴 달성률")
                        )
                ));
    }

    @Test
    public void 유저시간목록조회Test() throws Exception {
        var hours = Set.of("아침", "점심", "저녁");
        given(routineService.getHours(any(UserDetails.class)))
                .willReturn(hours);

        var result = this.mockMvc.perform(
                get(API_PREFIX + "/hour")
        );

        result.andExpect(status().isOk())
                .andDo(document("유저시간목록조회",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("[]").type(JsonFieldType.ARRAY).description("사용자 시간 목록")
                        )
                ));
    }

    @Test
    public void 루틴종료Test() throws Exception {
        doNothing().when(routineService).endRoutine(any(UserDetails.class), anyLong(), any(LocalDate.class));

        var result = this.mockMvc.perform(
                patch(API_PREFIX + "/routine/{routine_id}", String.valueOf(1L))
                        .queryParam("date", LocalDate.now().toString())
        );

        result.andExpect(status().isOk())
                .andDo(document("루틴종료",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("routine_id").description("루틴 아이디")
                        ),
                        queryParameters(
                                parameterWithName("date").description("날짜")
                        ),
                        responseFields(
                                fieldWithPath("is_success").type(JsonFieldType.BOOLEAN).description("성공 여부")
                        )
                ));
    }

    @Test
    public void 루틴삭제Test() throws Exception {
        doNothing().when(routineService).removeRoutine(any(UserDetails.class), anyLong());

        var result = this.mockMvc.perform(
                delete(API_PREFIX + "/routine/{routine_id}", String.valueOf(1L))
        );

        result.andExpect(status().isOk())
                .andDo(document("루틴삭제",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("routine_id").description("루틴 아이디")
                        ),
                        responseFields(
                                fieldWithPath("is_success").type(JsonFieldType.BOOLEAN).description("성공 여부")
                        )
                ));
    }
}