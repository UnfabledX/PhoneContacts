package com.chiacademy.software.phonecontacts.controller;

import com.chiacademy.software.phonecontacts.model.Contact;
import com.chiacademy.software.phonecontacts.model.dto.ContactDto;
import com.chiacademy.software.phonecontacts.service.ContactService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@Validated
public class ContactController {

    public static final String NO_LESS_THEN_3_LETTERS = "Contact name mustn't be bigger then 24 letters and less then 3 letters";

    private final ContactService contactService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ContactDto create(@RequestBody @Valid ContactDto request, Principal principal) {
        return contactService.create(request, principal);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@RequestParam(value = "contact") String contactName, Principal principal) {
        contactService.delete(contactName, principal);
    }

    @PutMapping("{contact}/edit")
    @ResponseStatus(HttpStatus.OK)
    public Contact editContactByName(@RequestBody @Valid ContactDto request,
                                     @PathVariable("contact") @Size(min = 3, max = 24, message = NO_LESS_THEN_3_LETTERS) String oldContactName,
                                     Principal principal) {
        return contactService.editContactByName(request, oldContactName, principal);
    }
}
