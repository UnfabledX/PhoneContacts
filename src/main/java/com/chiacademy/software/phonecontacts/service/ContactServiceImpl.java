package com.chiacademy.software.phonecontacts.service;

import com.chiacademy.software.phonecontacts.exception.NotFoundException;
import com.chiacademy.software.phonecontacts.model.Contact;
import com.chiacademy.software.phonecontacts.model.User;
import com.chiacademy.software.phonecontacts.model.dto.ContactDto;
import com.chiacademy.software.phonecontacts.repository.ContactRepository;
import com.chiacademy.software.phonecontacts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Set;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    @Override
    public ContactDto create(ContactDto request, Principal principal) {
        User user = getUser(principal);
        boolean isDuplicateEmailPresentInTheSameUser = Stream.of(request)
                .map(ContactDto::getEmails)
                .flatMap(Set::stream)
                .anyMatch(email -> contactRepository.existsContactByUserAndEmailsContaining(user, email));
        boolean isDuplicatePhonePresentInTheSameUser = Stream.of(request)
                .map(ContactDto::getPhones)
                .flatMap(Set::stream)
                .anyMatch(phone -> contactRepository.existsContactByUserAndPhonesContaining(user, phone));
        if (isDuplicateEmailPresentInTheSameUser || isDuplicatePhonePresentInTheSameUser) {
            throw new DataIntegrityViolationException("Such phone/email is already present in your contacts");
        }
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
        User user = getUser(principal);
        Contact contact = getContact(contactName, user);
        contactRepository.deleteContactById(contact.getId());
    }

    @Override
    public Contact editContactByName(ContactDto dto, String oldContactName, Principal principal) {
        User user = getUser(principal);
        Contact oldContact = getContact(oldContactName, user);
        Contact updatedContact = Contact.builder()
                .id(oldContact.getId())
                .name(dto.getName())
                .emails(dto.getEmails())
                .phones(dto.getPhones())
                .user(user).build();
        return contactRepository.save(updatedContact);
    }

    private Contact getContact(String contactName, User user) {
        return contactRepository.findContactByNameAndUser(contactName, user)
                .orElseThrow(() -> new NotFoundException("There is no contact present by such name", contactName));
    }

    private User getUser(Principal principal) {
        return userRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("No user found"));
    }

}
