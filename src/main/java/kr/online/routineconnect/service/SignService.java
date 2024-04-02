package kr.online.routineconnect.service;

import java.util.List;
import kr.online.routineconnect.config.security.TokenProvider;
import kr.online.routineconnect.domain.User;
import kr.online.routineconnect.dto.CheckDuplicatedResponse;
import kr.online.routineconnect.dto.Response;
import kr.online.routineconnect.dto.SignInRequest;
import kr.online.routineconnect.dto.SignInResponse;
import kr.online.routineconnect.dto.SignUpRequest;
import kr.online.routineconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SignService {

    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public CheckDuplicatedResponse checkUserEmailDuplicated(String email) {
        return userRepository.existsByEmail(email) ? CheckDuplicatedResponse.ERROR : CheckDuplicatedResponse.SUCCESS;
    }

    public CheckDuplicatedResponse checkUserNameDuplicated(String name) {
        return userRepository.existsByName(name) ? CheckDuplicatedResponse.ERROR : CheckDuplicatedResponse.SUCCESS;
    }

    public Response signUp(SignUpRequest request) throws DuplicateKeyException {
        var email = request.email();
        var name = request.name();
        var emailCheck = checkUserEmailDuplicated(email);
        var nameCheck = checkUserEmailDuplicated(name);

        if (emailCheck.isDuplicated()) {
            throw new DuplicateKeyException("해당 이메일은 " + emailCheck.getMessage());
        }

        if (nameCheck.isDuplicated()) {
            throw new DuplicateKeyException("해당 이름은 " + nameCheck.getMessage());
        }

        var user = User.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(request.password()))
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        userRepository.save(user);

        return Response.SIGN_UP_SUCCESS;
    }

    public SignInResponse signIn(SignInRequest request) throws BadCredentialsException, UsernameNotFoundException {
        var email = request.email();
        var password = request.password();
        var user = userDetailsService.loadUserByUsername(email);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("잘못된 비밀번호입니다.");
        }

        return tokenProvider.createTokens(email, user.getAuthorities());
    }
}
