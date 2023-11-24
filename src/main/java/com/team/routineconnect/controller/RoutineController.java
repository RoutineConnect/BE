package com.team.routineconnect.controller;

import com.team.routineconnect.domain.Accomplishment;
import com.team.routineconnect.domain.Hour;
import com.team.routineconnect.domain.ItemOrder;
import com.team.routineconnect.domain.User;
import com.team.routineconnect.dto.RoutineRequest;
import com.team.routineconnect.dto.RoutineUpdate;
import com.team.routineconnect.service.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class RoutineController {

    private final RoutineService routineService;

    // 메인페이지 (개인 루틴) 조회
    @GetMapping("/page/{date}")
    public ResponseEntity<List<ItemOrder>> getMemberRoutinesOnDate(
            @AuthenticationPrincipal User user,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        List<ItemOrder> routines = routineService.findRoutinesByUserOnDate(user, date);
        return ResponseEntity.ok(routines);
    }

    // 달성도 설정
    @PatchMapping("/page")
    public ResponseEntity<Void> setAccomplishment(
            @AuthenticationPrincipal User user,
            @RequestParam Long routine_item_id,
            @RequestParam Boolean accomplishment) {
        routineService.setAccomplishment(user, routine_item_id, accomplishment);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 루틴 추가
    @PostMapping("/routine")
    public ResponseEntity<Void> addRoutine(
            @AuthenticationPrincipal User user, @Valid @RequestBody RoutineRequest request) {
        routineService.addRoutine(user, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 루틴 수정
    @PatchMapping("/routine")
    public ResponseEntity<Void> updateRoutine(
            @AuthenticationPrincipal User user,
            @RequestParam Long routine_id,
            @Valid @RequestBody RoutineRequest request) {
        routineService.updateRoutine(user, routine_id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 루틴 순서 변경
    @PatchMapping("/page/{date}")
    public ResponseEntity<Void> updateRoutineOrder(
            @AuthenticationPrincipal User user,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @Valid @RequestBody List<RoutineUpdate> routineUpdates) {
        routineService.updateRoutineOrder(user, date, routineUpdates);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 일자 별 달성도 표시 조회
    @GetMapping("/achievement/{date}")
    public ResponseEntity<List<Float>> getAchievementsForWeek(
            @AuthenticationPrincipal User user,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        List<Float> achievements = routineService.getAchievementsForWeek(user, date);
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/hour")
    public ResponseEntity<Set<Hour>> getUserHours(@AuthenticationPrincipal User user) {
        Set<Hour> hours = user.getHours();
        return ResponseEntity.ok(hours);
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
                        DefaultMessageSourceResolvable::getDefaultMessage
                )), HttpStatus.BAD_REQUEST);
    }
}
