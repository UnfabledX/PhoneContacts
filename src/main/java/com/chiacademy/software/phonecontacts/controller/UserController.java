package com.chiacademy.software.phonecontacts.controller;

import com.chiacademy.software.phonecontacts.model.Contact;
import com.chiacademy.software.phonecontacts.security.AuthenticationRequest;
import com.chiacademy.software.phonecontacts.security.AuthenticationResponse;
import com.chiacademy.software.phonecontacts.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthenticationResponse register(@RequestBody @Valid AuthenticationRequest request) throws Exception {
        return userService.register(request);
    }

    @PostMapping("/auth")
    @ResponseStatus(HttpStatus.OK)
    public AuthenticationResponse login(@RequestBody @Valid AuthenticationRequest request) throws Exception {
        return userService.login(request);
    }

    @GetMapping("{login}/contacts")
    @ResponseStatus(HttpStatus.OK)
    public Page<Contact> getAllContactsByUser(@PathVariable("login") String login,
                                              @PageableDefault Pageable pageable,
                                              Principal principal){
        return userService.getAllContactsByLogin(login, pageable, principal);
    }
}
