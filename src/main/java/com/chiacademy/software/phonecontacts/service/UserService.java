package com.chiacademy.software.phonecontacts.service;

import com.chiacademy.software.phonecontacts.model.Contact;
import com.chiacademy.software.phonecontacts.security.AuthenticationRequest;
import com.chiacademy.software.phonecontacts.security.AuthenticationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface UserService {

    AuthenticationResponse register(AuthenticationRequest authenticationRequest) throws Exception;

    AuthenticationResponse login(AuthenticationRequest request);

    Page<Contact> getAllContactsByLogin(String login, Pageable pageable, Principal principal);
}
