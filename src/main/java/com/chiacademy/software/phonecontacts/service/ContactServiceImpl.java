package com.chiacademy.software.phonecontacts.service;

import com.chiacademy.software.phonecontacts.exception.NotFoundException;
import com.chiacademy.software.phonecontacts.model.Contact;
import com.chiacademy.software.phonecontacts.model.User;
import com.chiacademy.software.phonecontacts.model.dto.ContactDto;
import com.chiacademy.software.phonecontacts.repository.ContactRepository;
import com.chiacademy.software.phonecontacts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@Transactional
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    @Override
    public ContactDto create(ContactDto request, Principal principal) {
        User user = userRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("No user found"));
        Contact contact = Contact.builder()
                .name(request.getName())
                .emails(request.getEmails())
                .phones(request.getPhones())
                .user(user).build();
        contactRepository.save(contact);
        return request;
    }

    @Override
    public void delete(String contactName, Principal principal) {
        User user = userRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("No user found"));
        if (contactRepository.existsContactByNameAndUser(contactName, user)) {
            contactRepository.deleteContactByNameAndUser(contactName, user);
        } else {
            throw new NotFoundException("There is no contact present by such name", contactName);
        }
    }
}
