package com.chiacademy.software.phonecontacts.repository;

import com.chiacademy.software.phonecontacts.model.Contact;
import com.chiacademy.software.phonecontacts.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    boolean existsContactByNameAndUser(String contactName, User user);

    void deleteContactByNameAndUser(String contactName, User user);

    Page<Contact> findAllByUser(User user, Pageable pageable);
}

