package com.team.routineconnect.controller;

import com.team.routineconnect.dto.SignInResultDto;
import com.team.routineconnect.dto.SignUpResultDto;
import com.team.routineconnect.service.SignService;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// 예제 13.28
@RestController
@RequestMapping("/sign-api")
public class SignController {

    private final Logger LOGGER = LoggerFactory.getLogger(SignController.class);
    private final SignService signService;

    @Autowired
    public SignController(SignService signService) {
        this.signService = signService;
    }

    @PostMapping(value = "/sign-in")
    public SignInResultDto signIn(
            @ApiParam(value = "Email", required = true) @RequestParam String email,
            @ApiParam(value = "Password", required = true) @RequestParam String password)
            throws RuntimeException {
        LOGGER.info("[signIn] 로그인을 시도하고 있습니다. email : {}, pw : ****", email);
        SignInResultDto signInResultDto = signService.signIn(email, password);

        if (signInResultDto.getCode() == 0) {
            LOGGER.info("[signIn] 정상적으로 로그인되었습니다. email : {}, token : {}", email,
                    signInResultDto.getToken());
        }
        return signInResultDto;
    }

    @PostMapping(value = "/sign-up")
    public SignUpResultDto signUp(
            @ApiParam(value = "Email", required = true) @RequestParam String email,
            @ApiParam(value = "비밀번호", required = true) @RequestParam String password,
            @ApiParam(value = "이름", required = true) @RequestParam String name,
            @ApiParam(value = "권한", required = true) @RequestParam String role) {
        LOGGER.info("[signUp] 회원가입을 수행합니다. email : {}, password : ****, name : {}, role : {}", email,
                name, role);
        SignUpResultDto signUpResultDto = signService.signUp(email, password, name, role);

        LOGGER.info("[signUp] 회원가입을 완료했습니다. email : {}", email);
        return signUpResultDto;
    }

    @GetMapping(value = "/exception")
    public void exceptionTest() throws RuntimeException {
        throw new RuntimeException("접근이 금지되었습니다.");
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<Map<String, String>> ExceptionHandler(RuntimeException e) {
        HttpHeaders responseHeaders = new HttpHeaders();
        //responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        LOGGER.error("ExceptionHandler 호출, {}, {}", e.getCause(), e.getMessage());

        Map<String, String> map = new HashMap<>();
        map.put("error type", httpStatus.getReasonPhrase());
        map.put("code", "400");
        map.put("message", "에러 발생");

        return new ResponseEntity<>(map, responseHeaders, httpStatus);
    }

}
