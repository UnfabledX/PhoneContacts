package com.chiacademy.software.phonecontacts.controller;

import com.chiacademy.software.phonecontacts.BaseIT;
import com.chiacademy.software.phonecontacts.model.Contact;
import com.chiacademy.software.phonecontacts.model.dto.ContactDto;
import com.chiacademy.software.phonecontacts.repository.UserRepository;
import com.chiacademy.software.phonecontacts.security.AuthenticationRequest;
import com.chiacademy.software.phonecontacts.security.AuthenticationResponse;
import com.chiacademy.software.phonecontacts.service.JwtService;
import com.chiacademy.software.phonecontacts.service.UserService;
import com.chiacademy.software.phonecontacts.utils.RestPageImpl;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest extends BaseIT {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @LocalServerPort
    private int port;

    private HttpHeaders headers;

    @Autowired
    private UserRepository userRepository;

    private String createURLWithPort() {
        return "http://localhost:" + port + "/api/v1";
    }

    @Test
    @Order(1)
    public void registerUser_WhenOk_Test() {
        AuthenticationRequest request = new AuthenticationRequest("Rebeca", "password999");
        HttpEntity<AuthenticationRequest> entityRequest = new HttpEntity<>(request);
        ResponseEntity<AuthenticationResponse> response = template.postForEntity(
                createURLWithPort() + "/users/register", entityRequest, AuthenticationResponse.class);

        assertEquals(CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(userRepository.existsByLogin("Rebeca"));
    }

    @Test
    @Order(2)
    public void registerUser_WhenPasswordIsSmall_Test() {
        AuthenticationRequest request = new AuthenticationRequest("Oleksii", "pass");
        HttpEntity<AuthenticationRequest> entityRequest = new HttpEntity<>(request);
        ResponseEntity<ProblemDetail> response = template.postForEntity(
                createURLWithPort() + "/users/register", entityRequest, ProblemDetail.class);
        ProblemDetail detail = response.getBody();

        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertNotNull(detail);
        assertEquals("Failed validation", detail.getDetail());
        assertNotNull(detail.getProperties());
        List problemDetails = (List) detail.getProperties().get("problemDetails");
        assertEquals("pass", ((LinkedHashMap) problemDetails.get(0)).get("wrongValue"));

    }

    @Test
    @Order(3)
    public void registerUser_WhenOtherLoginAlreadyExists_Test() {
        AuthenticationRequest request = new AuthenticationRequest("Rebeca", "hello321");
        HttpEntity<AuthenticationRequest> entityRequest = new HttpEntity<>(request);
        ResponseEntity<ProblemDetail> response = template.postForEntity(
                createURLWithPort() + "/users/register", entityRequest, ProblemDetail.class);
        ProblemDetail detail = response.getBody();

        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertNotNull(detail);
        assertEquals("User login already exists", detail.getDetail());
        assertNotNull(detail.getProperties());
        List problemDetails = (List) detail.getProperties().get("problemDetails");
        assertEquals("Rebeca", ((LinkedHashMap) problemDetails.get(0)).get("wrongValue"));
    }

    @Test
    @Order(4)
    public void login_WhenOK_Test() {
        AuthenticationRequest request = new AuthenticationRequest("Rebeca", "password999");
        HttpEntity<AuthenticationRequest> entityRequest = new HttpEntity<>(request);
        ResponseEntity<AuthenticationResponse> response = template.postForEntity(
                createURLWithPort() + "/users/auth", entityRequest, AuthenticationResponse.class);

        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        AuthenticationResponse authenticationResponse = response.getBody();
        String token = authenticationResponse.getToken();
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @Order(5)
    public void login_WhenLoginDoesNotExist_Test() {
        AuthenticationRequest request = new AuthenticationRequest("Rebec", "password999");
        HttpEntity<AuthenticationRequest> entityRequest = new HttpEntity<>(request);
        ResponseEntity<Object> response = template.postForEntity(
                createURLWithPort() + "/users/auth", entityRequest, Object.class);

        assertEquals(FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Order(6)
    public void getAllContactsByUser_WhenOk_Test() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("Oleksii", "pass123");
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

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ParameterizedTypeReference<RestPageImpl<Contact>> responseType =
                new ParameterizedTypeReference<>() {
                };

        ResponseEntity<RestPageImpl<Contact>> result = template.exchange(
                createURLWithPort() + "/users/{login}/contacts", HttpMethod.GET,
                entity, responseType, request.getLogin());

        assertEquals(OK, result.getStatusCode());
        assertNotNull(result.getBody());
        Page<Contact> contacts = result.getBody();
        List<Contact> contentList = contacts.getContent();
        assertEquals(2, contentList.size());
        assertEquals(contactPetya.getName(), contentList.get(0).getName());
        assertEquals(contactMarina.getName(), contentList.get(1).getName());
    }

    @Test
    @Order(7)
    public void getAllContactsByUser_WhenNotOwnerLogin_Test() {
        AuthenticationRequest request = new AuthenticationRequest("Rebeca", "password999");
        AuthenticationResponse response = userService.login(request);
        headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Bearer " + response.getToken());
        headers.add("Content-Type", "application/json");

        String ownerNameofContacts = "Oleksii";
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<ProblemDetail> result = template.exchange(
                createURLWithPort() + "/users/{login}/contacts", HttpMethod.GET,
                entity, ProblemDetail.class, ownerNameofContacts);

        ProblemDetail detail = result.getBody();
        assertEquals(FORBIDDEN, result.getStatusCode());
        assertNotNull(detail);
        assertEquals("Illegal access", detail.getDetail());
        assertNotNull(detail.getProperties());
        List problemDetails = (List) detail.getProperties().get("problemDetails");
        assertEquals("Access is not allowed", ((LinkedHashMap) problemDetails.get(0)).get("message"));
    }
}
