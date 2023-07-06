package com.chiacademy.software.phonecontacts.service;

import com.chiacademy.software.phonecontacts.model.dto.ContactDto;

import java.security.Principal;

public interface ContactService {

    ContactDto create(ContactDto request, Principal principal);

    void delete(String contactName, Principal principal);
}


