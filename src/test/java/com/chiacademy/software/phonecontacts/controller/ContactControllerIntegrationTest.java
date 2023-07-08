package com.chiacademy.software.phonecontacts.controller;

import com.chiacademy.software.phonecontacts.BaseIT;
import com.chiacademy.software.phonecontacts.model.Contact;
import com.chiacademy.software.phonecontacts.model.User;
import com.chiacademy.software.phonecontacts.model.dto.ContactDto;
import com.chiacademy.software.phonecontacts.repository.ContactRepository;
import com.chiacademy.software.phonecontacts.repository.UserRepository;
import com.chiacademy.software.phonecontacts.security.AuthenticationRequest;
import com.chiacademy.software.phonecontacts.security.AuthenticationResponse;
import com.chiacademy.software.phonecontacts.service.JwtService;
import com.chiacademy.software.phonecontacts.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContactControllerIntegrationTest extends BaseIT {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @LocalServerPort
    private int port;

    private final AuthenticationRequest request = new AuthenticationRequest("Mishenka", "pass123333");

    private HttpHeaders headers;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private UserRepository userRepository;

    private String createURLWithPort() {
        return "http://localhost:" + port + "/api/v1";
    }

    @BeforeAll
    public void init() throws Exception {
        AuthenticationResponse response = userService.register(request);
        headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + response.getToken());
        headers.add("Content-Type", "application/json");

        ContactDto contactPetya = ContactDto.builder()
                .name("Petya")
                .emails(Set.of("Petro@gmail.com", "besheniy@gmail.com", "petrogolenya@mail.ru"))
                .phones(Set.of("+380 94 933 3433", "+380999123456", "+38050 631 2221")).build();
        HttpEntity<ContactDto> entityPetya = new HttpEntity<>(contactPetya, headers);
        template.postForEntity(createURLWithPort() + "/contacts/create", entityPetya, ContactDto.class);

        ContactDto contactMarina = ContactDto.builder()
                .name("Marina Svirska")
                .emails(Set.of("svMaina@gmail.com"))
                .phones(Set.of("+380 99 5673433", "+380993767744")).build();
        HttpEntity<ContactDto> entityMarina = new HttpEntity<>(contactMarina, headers);
        template.postForEntity(createURLWithPort() + "/contacts/create", entityMarina, ContactDto.class);
    }

    @Test
    @Order(1)
    public void createContact_WhenOk_Test() {
        ContactDto contactDto = ContactDto.builder()
                .name("Lena")
                .emails(Set.of("Leka@gmail.com", "lena999@gmail.com"))
                .phones(Set.of("+380 93 933 3333", "+380 93 933 3334", "+380 93 933 3335"))
                .build();

        HttpEntity<ContactDto> entity = new HttpEntity<>(contactDto, headers);
        ResponseEntity<ContactDto> result = template
                .postForEntity(createURLWithPort() + "/contacts/create", entity, ContactDto.class);

        assertEquals(CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(contactDto.getName(), result.getBody().getName());
    }

    @Test
    @Order(2)
    public void createContact_WhenNameInvalid_Test() {
        ContactDto contactDto = ContactDto.builder()
                .name("A1")
                .emails(Set.of("Leka@gmail.com", "lena999@gmail.com"))
                .phones(Set.of("+380 93 933 3333", "+380 93 933 3334", "+380 93 933 3335"))
                .build();

        HttpEntity<ContactDto> entity = new HttpEntity<>(contactDto, headers);
        ResponseEntity<ProblemDetail> result = template
                .postForEntity(createURLWithPort() + "/contacts/create", entity, ProblemDetail.class);
        ProblemDetail detail = result.getBody();

        assertEquals(BAD_REQUEST, result.getStatusCode());
        assertNotNull(detail);
        assertEquals("Failed validation", detail.getDetail());
        assertNotNull(detail.getProperties());
        List problemDetails = (List) detail.getProperties().get("problemDetails");
        assertEquals("A1", ((LinkedHashMap) problemDetails.get(0)).get("wrongValue"));
    }

    @Test
    @Order(3)
    public void deleteContact_WhenOK_Test() {
        String contactName = "Lena";
        Optional<User> optionalUser = userRepository.findByLogin(request.getLogin());
        assertTrue(optionalUser.isPresent());
        User userOleksii = optionalUser.get();
        Optional<Contact> contact = contactRepository.findContactByNameAndUser(contactName, userOleksii);
        assertTrue(contact.isPresent());

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Object> result = template
                .exchange(createURLWithPort() + "/contacts/delete?contact=" + contactName,
                        HttpMethod.DELETE, entity, Object.class);

        assertTrue(contactRepository.findContactByNameAndUser(contactName, userOleksii).isEmpty());
        assertEquals(OK, result.getStatusCode());
    }

    @Test
    @Order(4)
    public void deleteContact_WhenContactNameDoesNotExist_Test() throws Exception {
        String contactName = "alien";
        Optional<User> optionalUser = userRepository.findByLogin(request.getLogin());
        assertTrue(optionalUser.isPresent());
        User userOleksii = optionalUser.get();
        Optional<Contact> contact = contactRepository.findContactByNameAndUser(contactName, userOleksii);
        assertFalse(contact.isPresent());

        HttpEntity<ProblemDetail> entity = new HttpEntity<>(headers);
        ResponseEntity<ProblemDetail> result = template
                .exchange(createURLWithPort() + "/contacts/delete?contact=" + contactName,
                        HttpMethod.DELETE, entity, ProblemDetail.class);
        ProblemDetail detail = result.getBody();

        assertEquals(NOT_FOUND, result.getStatusCode());
        assertNotNull(detail);
        assertEquals("Data is not found", detail.getDetail());
        assertNotNull(detail.getProperties());
        List problemDetails = (List) detail.getProperties().get("problemDetails");
        assertEquals("alien", ((LinkedHashMap) problemDetails.get(0)).get("wrongValue"));
    }

    @Test
    @Order(5)
    public void editContact_WhenOK_Test() {
        ContactDto updatedContact = ContactDto.builder()
                .name("Petro Ivanovich")
                .emails(Set.of("Petro@gmail.com"))
                .phones(Set.of("+380 94 933 3433", "+380999123456")).build();
        String oldContactName = "Petya";
        HttpEntity<ContactDto> entity = new HttpEntity<>(updatedContact, headers);

        ResponseEntity<Contact> result = template.exchange(createURLWithPort() + "/contacts/{contact}/edit",
                HttpMethod.PUT, entity, Contact.class, oldContactName);
        assertEquals(OK, result.getStatusCode());
        Contact contactAfterUpdate = result.getBody();
        assertNotNull(contactAfterUpdate);
        assertEquals(updatedContact.getName(), contactAfterUpdate.getName());
        assertEquals(updatedContact.getEmails(), contactAfterUpdate.getEmails());
        assertEquals(updatedContact.getPhones(), contactAfterUpdate.getPhones());
    }

    @Test
    @Order(6)
    public void editContact_WhenContactDoesNotExist_Test() {
        ContactDto updatedContact = ContactDto.builder()
                .name("Marina")
                .emails(Set.of("svMarina@gmail.com"))
                .phones(Set.of("+380 99 5673433")).build();
        String oldContactName = "unknown";
        HttpEntity<ContactDto> entity = new HttpEntity<>(updatedContact, headers);

        ResponseEntity<ProblemDetail> result = template.exchange(createURLWithPort() + "/contacts/{contact}/edit",
                HttpMethod.PUT, entity, ProblemDetail.class, oldContactName);
        ProblemDetail detail = result.getBody();

        assertEquals(NOT_FOUND, result.getStatusCode());
        assertNotNull(detail);
        assertEquals("Data is not found", detail.getDetail());
        assertNotNull(detail.getProperties());
        List problemDetails = (List) detail.getProperties().get("problemDetails");
        assertEquals("unknown", ((LinkedHashMap) problemDetails.get(0)).get("wrongValue"));
    }
}
