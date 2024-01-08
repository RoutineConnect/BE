package com.team.routineconnect.controller;

import com.team.routineconnect.domain.Hour;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.ItemResponse;
import com.team.routineconnect.dto.ItemUpdate;
import com.team.routineconnect.dto.RoutineRequest;
import com.team.routineconnect.service.RoutineService;
import io.swagger.annotations.ApiOperation;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class RoutineController {

    private final RoutineService routineService;

    // 메인페이지 (개인 루틴) 조회
    @ApiOperation("날짜별 루틴 조회")
    @GetMapping("/page/{date}")
    public ResponseEntity<List<ItemResponse>> getMemberRoutinesOnDate(
            @AuthenticationPrincipal User user,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        List<ItemResponse> items = routineService.findRoutinesByUserOnDate(user, date);
        return ResponseEntity.ok(items);
    }

    // 달성도 설정
    @ApiOperation("달성도 설정")
    @PatchMapping("/page")
    public ResponseEntity<Void> setAccomplishment(
            @AuthenticationPrincipal User user,
            @RequestParam Long routine_item_id,
            @RequestParam Boolean accomplishment) {
        routineService.setAccomplishment(user, routine_item_id, accomplishment);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 루틴 추가
    @ApiOperation("루틴 추가")
    @PostMapping("/routine")
    public ResponseEntity<Void> addRoutine(
            @AuthenticationPrincipal User user, @Valid @RequestBody RoutineRequest request) {
        routineService.addRoutine(user, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 루틴 수정
    @ApiOperation("루틴 수정")
    @PatchMapping("/routine")
    public ResponseEntity<Void> updateRoutine(
            @AuthenticationPrincipal User user,
            @RequestParam Long routine_id,
            @Valid @RequestBody RoutineRequest request) {
        routineService.updateRoutine(user, routine_id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 루틴 순서 변경
    @ApiOperation("루틴 순서 변경")
    @PatchMapping("/page/{date}")
    public ResponseEntity<Void> updateRoutineOrder(
            @AuthenticationPrincipal User user,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @Valid @RequestBody List<ItemUpdate> itemUpdates) {
        routineService.updateItemOrder(user, date, itemUpdates);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 일자 별 달성도 표시 조회
    @ApiOperation("일자별 달성도")
    @GetMapping("/achievement/{date}")
    public ResponseEntity<List<Float>> getAchievementsForWeek(
            @AuthenticationPrincipal User user,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        List<Float> achievements = routineService.getAchievementsForWeek(user, date);
        return ResponseEntity.ok(achievements);
    }

    @ApiOperation("시간목록 불러오기")
    @GetMapping("/hour")
    public ResponseEntity<Set<Hour>> getUserHours(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(routineService.getHours(user));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<?> onConstraintValidationException(ConstraintViolationException e) {
        return new ResponseEntity<>(e.getConstraintViolations(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return new ResponseEntity<>(e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage()
                                : "No error message"
                )), HttpStatus.BAD_REQUEST);
    }
}
