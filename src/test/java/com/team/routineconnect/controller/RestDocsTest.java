//package com.team.routineconnect.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.team.routineconnect.domain.Accomplishment;
//import com.team.routineconnect.dto.RoutineRequest;
//import com.team.routineconnect.dto.RoutineUpdate;
//import com.team.routineconnect.dto.RoutineWithAccomplishment;
//import com.team.routineconnect.service.RoutineService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.restdocs.RestDocumentationExtension;
//import org.springframework.restdocs.payload.JsonFieldType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//
//import static com.team.routineconnect.ApiDocumentUtils.getDocumentRequest;
//import static com.team.routineconnect.ApiDocumentUtils.getDocumentResponse;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
//import static org.springframework.restdocs.payload.PayloadDocumentation.*;
//import static org.springframework.restdocs.request.RequestDocumentation.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ExtendWith(RestDocumentationExtension.class)
//@WebMvcTest(RoutineController.class)
//@AutoConfigureRestDocs
//public class RoutineControllerTest {
//
//    private static final String API_PREFIX = "/api/member/{user_id}";
//    private final RoutineRequest request = RoutineRequest.builder()
//            .title("기상")
//            .hour("아침")
//            .routine_day((byte) 0b11111110)
//            .shared(false)
//            .created_date(LocalDateTime.parse("2023-08-22T22:55:00"))
//            .ended_date(null)
//            .build();
//    @Autowired
//    protected MockMvc mockMvc;
//    @Autowired
//    protected ObjectMapper objectMapper;
//    @MockBean
//    private RoutineService routineService;
//
//    @Test
//    public void 메인페이지조회Test() throws Exception {
//        List<RoutineWithAccomplishment> response = Arrays.asList(
//                RoutineWithAccomplishment.builder()
//                        .routine_id(1L)
//                        .title("기상")
//                        .hour("아침")
//                        .repeating_days((byte) 0b11111110)
//                        .shared(false)
//                        .created_date(LocalDateTime.parse("2023-08-22T22:55:00"))
//                        .ended_date(null)
//                        .position(1f)
//                        .accomplishment(Accomplishment.IN_PROGRESS)
//                        .build(),
//
//                RoutineWithAccomplishment.builder()
//                        .routine_id(2L)
//                        .title("운동")
//                        .hour("아침")
//                        .repeating_days((byte) 0b11111110)
//                        .shared(true)
//                        .created_date(LocalDateTime.parse("2023-08-23T07:30:00"))
//                        .ended_date(LocalDateTime.parse("2023-08-23T08:30:00"))
//                        .position(2f)
//                        .accomplishment(Accomplishment.CLEAR)
//                        .build(),
//
//                RoutineWithAccomplishment.builder()
//                        .routine_id(3L)
//                        .title("저녁식사")
//                        .hour("저녁")
//                        .repeating_days((byte) 0b1111110)
//                        .shared(true)
//                        .created_date(LocalDateTime.parse("2023-08-24T18:00:00"))
//                        .ended_date(LocalDateTime.parse("2023-08-24T19:00:00"))
//                        .position(3f)
//                        .accomplishment(Accomplishment.INCOMPLETE)
//                        .build()
//        );
//
//        given(routineService.findRoutinesByUserOnDate(eq(1L), any(LocalDate.class)))
//                .willReturn(response);
//
//        ResultActions result = this.mockMvc.perform(
//                get(API_PREFIX + "/page/{date}", 1L, LocalDate.now()));
//
//        result.andExpect(status().isOk())
//                .andDo(document("retrieve-person's-routine",
//                        getDocumentRequest(),
//                        getDocumentResponse(),
//                        pathParameters(
//                                parameterWithName("user_id").description("유저 아이디"),
//                                parameterWithName("date").description("날짜")
//                        ),
//                        responseFields(
//                                fieldWithPath("[]").description("해당 날짜의 루틴 배열"),
//                                fieldWithPath("[].routine_id").type(JsonFieldType.NUMBER).description("루틴 아이디"),
//                                fieldWithPath("[].title").type(JsonFieldType.STRING).description("루틴 제목"),
//                                fieldWithPath("[].hour").type(JsonFieldType.STRING).description("루틴 시간"),
//                                fieldWithPath("[].repeating_days").type(JsonFieldType.NUMBER).description(
//                                        """
//                                                루틴 반복 요일을 비트로 나타냄. 예를 들어 월요일 ~ 금요일 주중 반복되는 루틴일 경우 \
//                                                0b00111110이 되고, 토요일과 일요일 주말에 반복되는 루틴일 경우 0b11000000이 됨. \
//                                                맨 오른쪽 비트를 제외하고 오른쪽부터 제일 왼쪽 비트까지 순서대로 월~일
//                                                """),
//                                fieldWithPath("[].shared").type(JsonFieldType.BOOLEAN).description("루틴 공개 여부"),
//                                fieldWithPath("[].created_date").type(JsonFieldType.STRING).description("생성 일시"),
//                                fieldWithPath("[].ended_date").type(JsonFieldType.STRING)
//                                        .description("종료 일시. Nullable").optional(),
//                                fieldWithPath("[].position").type(JsonFieldType.NUMBER)
//                                        .description("루틴 위치, Float 타입"),
//                                fieldWithPath("[].accomplishment").type(JsonFieldType.STRING)
//                                        .description("달성 여부. CLEAR, INCOMPLETE, FAIL, IN_PROGRESS 4가지가 있음")
//                        )
//                ));
//    }
//
//    @Test
//    public void 달성도조회Test() throws Exception {
//        List<Float> response = Arrays.asList(0.8f, 0.9f, 0.6f, 0.7f, 1f, 0.85f, 0f);
//        given(routineService.getAchievementsForWeek(eq(1L), any(LocalDate.class)))
//                .willReturn(response);
//
//        ResultActions result = this.mockMvc.perform(
//                get(API_PREFIX + "/achievement/{date}", 1L, LocalDate.now()));
//
//        result.andExpect(status().isOk())
//                .andDo(document("retrieve-person's-achievements",
//                        getDocumentRequest(),
//                        getDocumentResponse(),
//                        pathParameters(
//                                parameterWithName("user_id").description("유저 아이디"),
//                                parameterWithName("date").description("날짜")
//                        ),
//                        responseFields(
//                                fieldWithPath("[]").description("일주일 간의 루틴 달성률")
//                        )
//                ));
//    }
//
//    @Test
//    public void 루틴추가Test() throws Exception {
//        ResultActions result = this.mockMvc.perform(
//                post(API_PREFIX + "/routine", 1L).content(objectMapper.writeValueAsString(request))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//        );
//
//        result.andExpect(status().isNoContent())
//                .andDo(document("add-routine",
//                        getDocumentRequest(),
//                        getDocumentResponse(),
//                        pathParameters(
//                                parameterWithName("user_id").description("유저 아이디")
//                        ),
//                        requestFields(
//                                fieldWithPath("title").type(JsonFieldType.STRING).description("루틴 제목"),
//                                fieldWithPath("hour").type(JsonFieldType.STRING).description("루틴 시간"),
//                                fieldWithPath("routine_day").type(JsonFieldType.NUMBER).description(
//                                        """
//                                                루틴 반복 요일을 비트로 나타냄. 예를 들어 월요일 ~ 금요일 주중 반복되는 루틴일 경우 \
//                                                0b00111110이 되고, 토요일과 일요일 주말에 반복되는 루틴일 경우 0b11000000이 됨.\
//                                                맨 오른쪽 비트를 제외하고 오른쪽부터 제일 왼쪽 비트까지 순서대로 월~일
//                                                """),
//                                fieldWithPath("shared").type(JsonFieldType.BOOLEAN).description("루틴 공개 여부"),
//                                fieldWithPath("created_date").type(JsonFieldType.STRING).description("생성 일시"),
//                                fieldWithPath("ended_date").type(JsonFieldType.NULL)
//                                        .description("종료 일시. 생성 시에는 null")
//                        )
//                ));
//    }
//
//    @Test
//    public void 루틴수정Test() throws Exception {
//        RoutineRequest request = RoutineRequest.builder()
//                .title("취침")
//                .hour("밤")
//                .routine_day((byte) 0b11111110)
//                .shared(false)
//                .created_date(LocalDateTime.now())
//                .ended_date(null)
//                .build();
//
//        ResultActions result = this.mockMvc.perform(
//                patch(API_PREFIX + "/routine", 1L)
//                        .param("routine_id", String.valueOf(1L))
//                        .content(objectMapper.writeValueAsString(request))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//        );
//
//        result.andExpect(status().isNoContent())
//                .andDo(document("update-routine",
//                        getDocumentRequest(),
//                        getDocumentResponse(),
//                        pathParameters(
//                                parameterWithName("user_id").description("유저 아이디")
//                        ),
//                        queryParameters(
//                                parameterWithName("routine_id").description("루틴 아이디")
//                        ),
//                        requestFields(
//                                fieldWithPath("title").type(JsonFieldType.STRING).description("루틴 제목"),
//                                fieldWithPath("hour").type(JsonFieldType.STRING).description("루틴 시간"),
//                                fieldWithPath("routine_day").type(JsonFieldType.NUMBER).description(
//                                        """
//                                                루틴 반복 요일을 비트로 나타냄. 예를 들어 월요일 ~ 금요일 주중 반복되는 루틴일 경우 \
//                                                0b00111110이 되고, 토요일과 일요일 주말에 반복되는 루틴일 경우 0b11000000이 됨.\
//                                                맨 오른쪽 비트를 제외하고 오른쪽부터 제일 왼쪽 비트까지 순서대로 월~일
//                                                """),
//                                fieldWithPath("shared").type(JsonFieldType.BOOLEAN).description("루틴 공개 여부"),
//                                fieldWithPath("created_date").type(JsonFieldType.STRING).description("생성 일시"),
//                                fieldWithPath("ended_date").type(JsonFieldType.STRING).description("종료 일시").optional()
//                        )
//                ));
//    }
//
//    @Test
//    public void 루틴순서변경Test() throws Exception {
//        List<RoutineUpdate> request = Arrays.asList(new RoutineUpdate(1L, 2f));
//
//        ResultActions result = this.mockMvc.perform(
//                patch(API_PREFIX + "/page/{date}", 1L, LocalDate.now())
//                        .content(objectMapper.writeValueAsString(request))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//        );
//
//        result.andExpect(status().isNoContent())
//                .andDo(document("update-routine-order",
//                        getDocumentRequest(),
//                        getDocumentResponse(),
//                        pathParameters(
//                                parameterWithName("user_id").description("유저 아이디"),
//                                parameterWithName("date").description("날짜")
//                        ),
//                        requestFields(
//                                fieldWithPath("[]").description("변경된 루틴 순서 배열"),
//                                fieldWithPath("[].routine_id").description("변경할 루틴 아이디"),
//                                fieldWithPath("[].position").description(
//                                        """
//                                                변경할 루틴 위치. 원래 위치에서 다른 위치로 옮길 때, 옮기려는 위치 위의 루틴과 아래 \
//                                                루틴의 중간값.
//                                                newPosition = (upperRoutine.position + lowerRoutine.position) / 2
//                                                """)
//                        )
//                ));
//
//    }
//}