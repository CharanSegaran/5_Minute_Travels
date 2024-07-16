package ibf.tfip.__minute_travels.services;


import ibf.tfip.__minute_travels.dtos.RegisterUserDto;
import ibf.tfip.__minute_travels.dtos.LoginUserDto;
import ibf.tfip.__minute_travels.entities.User;
import ibf.tfip.__minute_travels.repositories.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;
    
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
        UserRepository userRepository,
        AuthenticationManager authenticationManager,
        PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(RegisterUserDto input) {
        User user = new User();
        user.setFullName(input.getFullName());
        user.setEmail(input.getSignUpEmail());
        user.setPassword(passwordEncoder.encode(input.getSignUpPassword()));

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getLogInEmail(),
                        input.getLogInPassword()
                )
        );

        return userRepository.findByEmail(input.getLogInEmail())
                .orElseThrow();
    }
}
