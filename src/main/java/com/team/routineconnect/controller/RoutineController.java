package com.team.routineconnect.controller;

import com.team.routineconnect.dto.RoutineRequest;
import com.team.routineconnect.dto.RoutineUpdate;
import com.team.routineconnect.dto.RoutineWithAccomplishment;
import com.team.routineconnect.service.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member/{user_id}")
public class RoutineController {

    private final RoutineService routineService;

    // 메인페이지 (개인 루틴) 조회
    @GetMapping("/page/{date}")
    public ResponseEntity<List<RoutineWithAccomplishment>> getMemberRoutinesOnDate(
            @PathVariable Long user_id, @PathVariable LocalDate date) {
        List<RoutineWithAccomplishment> routines = routineService.findRoutinesByUserOnDate(user_id, date);
        return ResponseEntity.ok(routines);
    }

    // 일자 별 달성도 표시 조회
    @GetMapping("/achievement/{date}")
    public ResponseEntity<List<Float>> getAchievementsForWeek(
            @PathVariable Long user_id, @PathVariable LocalDate date) {
        List<Float> achievements = routineService.getAchievementsForWeek(user_id, date);
        return ResponseEntity.ok(achievements);
    }

    // 루틴 추가
    @PostMapping("/routine")
    public ResponseEntity<Void> addRoutine(@PathVariable Long user_id, @RequestBody RoutineRequest request) {
        routineService.addRoutine(user_id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 루틴 수정
    @PatchMapping("/routine")
    public ResponseEntity<Void> updateRoutine(
            @PathVariable Long user_id,
            @RequestParam Long routine_id,
            @RequestBody RoutineRequest request) {
        routineService.updateRoutine(user_id, routine_id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 루틴 순서 변경
    @PatchMapping("/page/{date}")
    public ResponseEntity<Void> updateRoutineOrder(
            @PathVariable Long user_id,
            @PathVariable LocalDate date,
            @RequestBody List<RoutineUpdate> routineUpdates) {
        routineService.updateRoutineOrder(user_id, date, routineUpdates);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException() {
        return "/error";
    }
}
