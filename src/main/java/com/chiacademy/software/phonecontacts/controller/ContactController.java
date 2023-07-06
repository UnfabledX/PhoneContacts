package com.chiacademy.software.phonecontacts.controller;

import com.chiacademy.software.phonecontacts.model.dto.ContactDto;
import com.chiacademy.software.phonecontacts.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    private final ContactService contactService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ContactDto create(@RequestBody @Valid ContactDto request, Principal principal) {
        return contactService.create(request, principal);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@RequestParam(value = "contact") String contactName, Principal principal){
        contactService.delete(contactName, principal);
    }

}
