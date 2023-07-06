package com.chiacademy.software.phonecontacts.service;

import com.chiacademy.software.phonecontacts.exception.UserAlreadyExistsException;
import com.chiacademy.software.phonecontacts.model.Contact;
import com.chiacademy.software.phonecontacts.model.Role;
import com.chiacademy.software.phonecontacts.model.User;
import com.chiacademy.software.phonecontacts.repository.ContactRepository;
import com.chiacademy.software.phonecontacts.repository.UserRepository;
import com.chiacademy.software.phonecontacts.security.AuthenticationRequest;
import com.chiacademy.software.phonecontacts.security.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthenticationResponse register(AuthenticationRequest request) {
        String userLogin = request.getLogin();
        if (userRepository.existsByLogin(userLogin)) {
            throw new UserAlreadyExistsException("The user already exists", userLogin);
        }
        User user = User.builder()
                .login(userLogin)
                .password(encoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Override
    public AuthenticationResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getLogin(),
                request.getPassword())
        );
        User user = userRepository.findByLogin(request.getLogin()).orElseThrow(
                () -> new UsernameNotFoundException("No user with such login name"));
        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Override
    public Page<Contact> getAllContactsByLogin(String login, Pageable pageable) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(()->new UsernameNotFoundException("No user found"));
        Page<Contact> contacts = contactRepository.findAllByUser(user, pageable);
        return contacts;
    }


}
