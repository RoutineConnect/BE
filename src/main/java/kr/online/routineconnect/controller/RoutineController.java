package kr.online.routineconnect.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import kr.online.routineconnect.domain.CustomUserDetails;
import kr.online.routineconnect.domain.Hour;
import kr.online.routineconnect.domain.Routine;
import kr.online.routineconnect.dto.ItemResponse;
import kr.online.routineconnect.dto.ItemUpdate;
import kr.online.routineconnect.dto.Response;
import kr.online.routineconnect.dto.RoutineRequest;
import kr.online.routineconnect.service.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
@RestController
public class RoutineController {

    private final RoutineService routineService;

    // 메인페이지 (개인 루틴) 조회
    @GetMapping("/page/{date}")
    public ResponseEntity<List<ItemResponse>> getMemberItemsOnDate(
            @AuthenticationPrincipal CustomUserDetails user,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @PathVariable LocalDate date) {
        List<ItemResponse> items = routineService.findItemsByUserOnDate(user, date);
        return ResponseEntity.ok(items);
    }

    // 달성도 설정
    @PatchMapping("/page")
    public ResponseEntity<Response> setAccomplishment(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam Long item_order_id,
            @RequestParam Boolean accomplishment) {
        routineService.setAccomplishment(user, item_order_id, accomplishment);
        return ResponseEntity.ok(Response.SUCCESS);
    }

    // 루틴 추가
    @PostMapping("/routine")
    public ResponseEntity<Response> addRoutine(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody RoutineRequest request) {
        Routine routine = routineService.addRoutine(user, request);
        return ResponseEntity.ok(Response.SUCCESS);
    }

    // 루틴 수정
    @PutMapping("/routine")
    public ResponseEntity<Response> updateRoutine(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam Long routine_id,
            @Valid @RequestBody RoutineRequest request) {
        routineService.updateRoutine(user, routine_id, request);
        return ResponseEntity.ok(Response.SUCCESS);
    }

    // 아이템 순서 변경
    @PatchMapping("/page/{date}")
    public ResponseEntity<Response> updateItemOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @PathVariable LocalDate date,
            @Valid @RequestBody List<ItemUpdate> itemUpdates) {
        routineService.updateItemOrder(user, date, itemUpdates);
        return ResponseEntity.ok(Response.SUCCESS);
    }

    // 일자 별 달성도 표시 조회
    @GetMapping("/achievement/{date}")
    public ResponseEntity<List<Float>> getAchievementsForWeek(
            @AuthenticationPrincipal CustomUserDetails user,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @PathVariable LocalDate date) {
        List<Float> achievements = routineService.getAchievementsForWeek(user, date);
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/hour")
    public ResponseEntity<Set<Hour>> getUserHours(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(routineService.getHours(user));
    }
}
