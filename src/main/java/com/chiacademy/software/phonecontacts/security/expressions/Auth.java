package com.chiacademy.software.phonecontacts.security.expressions;

import com.chiacademy.software.phonecontacts.model.User;
import com.chiacademy.software.phonecontacts.repository.ContactRepository;
import com.chiacademy.software.phonecontacts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class Auth {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;

    public boolean belongsToUser(String contactName, Principal principal) {
        User user = userRepository.findByLogin(principal.getName())
                .orElseThrow(()->new UsernameNotFoundException("No user found"));
        return contactRepository.existsContactByNameAndUser(contactName, user);
    }
}
