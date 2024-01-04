package com.team.routineconnect.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.routineconnect.dto.ItemUpdate;
import com.team.routineconnect.service.RoutineService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class RoutineControllerTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private RoutineService routineService;

    @Test
    @WithMockUser
    void 업데이트에러Test() throws Exception {
        List<ItemUpdate> itemUpdates = Arrays.asList(
                new ItemUpdate(null, null),
                new ItemUpdate(null, null)
        );

        ResultActions result = mockMvc.perform(patch("/api/page/{date}", "2023-09-25")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemUpdates))
        );

        result.andExpect(status().isBadRequest());
    }
}