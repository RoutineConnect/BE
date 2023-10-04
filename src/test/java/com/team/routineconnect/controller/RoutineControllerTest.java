package com.team.routineconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.routineconnect.config.security.SecurityConfiguration;
import com.team.routineconnect.dto.RoutineUpdate;
import com.team.routineconnect.service.RoutineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
        RoutineUpdate emptyUpdate = new RoutineUpdate(null, null);
        List<RoutineUpdate> updateList = Collections.singletonList(emptyUpdate);

        ResultActions result = mockMvc.perform(patch("/api/page/{date}", "2023-09-25")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateList))
        );

        result.andExpect(status().isBadRequest());
    }
}