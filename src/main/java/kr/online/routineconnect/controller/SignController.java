package kr.online.routineconnect.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import kr.online.routineconnect.dto.CheckDuplicatedResponse;
import kr.online.routineconnect.dto.Response;
import kr.online.routineconnect.dto.SignInRequest;
import kr.online.routineconnect.dto.SignInResponse;
import kr.online.routineconnect.dto.SignUpRequest;
import kr.online.routineconnect.service.SignService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/sign-api")
@RestController
public class SignController {

    private final SignService signService;

    @GetMapping("/check-user-email")
    public ResponseEntity<CheckDuplicatedResponse> checkUserEmailDuplicated(@NotBlank @RequestParam String email) {
        return ResponseEntity.ok(signService.checkUserEmailDuplicated(email));
    }

    @GetMapping("/check-user-name")
    public ResponseEntity<CheckDuplicatedResponse> checkUserNameDuplicated(@NotBlank @RequestParam String name) {
        return ResponseEntity.ok(signService.checkUserNameDuplicated(name));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Response> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(signService.signUp(signUpRequest));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponse> signIn(@Valid @RequestBody SignInRequest request) {
        return ResponseEntity.ok(signService.signIn(request));
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    ResponseEntity<Response> onBadCredentialsException(Exception e) {
        return new ResponseEntity<>(Response.SIGN_IN_FAIL, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    ResponseEntity<Response> onDuplicateKeyException(DuplicateKeyException e) {
        return new ResponseEntity<>(Response.FAIL.setMessage(e.getMessage()), HttpStatus.CONFLICT);
    }
}
