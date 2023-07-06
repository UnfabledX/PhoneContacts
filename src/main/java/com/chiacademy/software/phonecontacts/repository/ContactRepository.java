package com.chiacademy.software.phonecontacts.repository;

import com.chiacademy.software.phonecontacts.model.Contact;
import com.chiacademy.software.phonecontacts.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    Optional<Contact> findContactByNameAndUser(String contactName, User user);
    @Modifying
    @Query("DELETE FROM Contact c WHERE c.id=:id")
    void deleteContactById(Long id);

    Page<Contact> findAllByUser(User user, Pageable pageable);

    boolean existsContactByUserAndEmailsContaining(User user, String email);

    boolean existsContactByUserAndPhonesContaining(User user, String phone);
}

