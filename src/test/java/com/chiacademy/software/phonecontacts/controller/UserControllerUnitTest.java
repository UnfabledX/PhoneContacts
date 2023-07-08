package com.chiacademy.software.phonecontacts.controller;

import com.chiacademy.software.phonecontacts.exception.UserAlreadyExistsException;
import com.chiacademy.software.phonecontacts.model.Contact;
import com.chiacademy.software.phonecontacts.model.dto.ContactDto;
import com.chiacademy.software.phonecontacts.security.AuthenticationRequest;
import com.chiacademy.software.phonecontacts.security.AuthenticationResponse;
import com.chiacademy.software.phonecontacts.service.JwtService;
import com.chiacademy.software.phonecontacts.service.UserService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerUnitTest {

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void registerUser_WhenOk_Test() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("Oleksii", "pass123");
        String jsonContent = objectMapper.writeValueAsString(request);

        AuthenticationResponse response = new AuthenticationResponse("eyJhbGciOiJIUzI1NiJ9." +
                "eyJzdWIiOiJPbGVrc2lpIiwiaWF0IjoxNjg4OTkwNjAwLCJleHAiOjE2ODg5OTIwNDB9" +
                ".U-gnRTNtyE6pbhi-3P6qFfNd9dYk0sfT6zzJYu0vFrI");

        when(userService.register(request)).thenReturn(response);
        mockMvc.perform(post("/api/v1/users/register")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.token", Matchers.is(response.getToken())));

        verify(userService, times(1)).register(request);
    }

    @Test
    public void registerUser_WhenPasswordIsSmall_Test() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("Oleksii", "pass");
        String jsonContent = objectMapper.writeValueAsString(request);

        String expectedJsonResult = """
                {
                     "type": "about:blank",
                     "title": "Bad Request",
                     "status": 400,
                     "detail": "Failed validation",
                     "instance": "/api/v1/users/register",
                     "problemDetails": [
                         {
                             "message": "Name mustn't be bigger then 64 and less then 5 characters",
                             "field": "password",
                             "wrongValue": "pass"
                         }
                     ]
                 }
                """;

        mockMvc.perform(post("/api/v1/users/register")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedJsonResult));

        verify(userService, times(0)).register(request);
    }

    @Test
    public void registerUser_WhenOtherLoginAlreadyExists_Test() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("Oleksii", "pass1234");
        String jsonContent = objectMapper.writeValueAsString(request);

        String expectedJsonResult = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "User login already exists",
                    "instance": "/api/v1/users/register",
                    "problemDetails": [
                        {
                            "message": "The user already exists",
                            "wrongValue": "Oleksii"
                        }
                    ]
                }
                """;
        doThrow(new UserAlreadyExistsException("The user already exists", request.getLogin()))
                .when(userService).register(request);
        mockMvc.perform(post("/api/v1/users/register")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedJsonResult));

        verify(userService, times(1)).register(request);
    }


    @Test
    public void login_WhenOK_Test() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("Oleksii", "pass123");
        String jsonContent = objectMapper.writeValueAsString(request);

        AuthenticationResponse response = new AuthenticationResponse("eyJhbGciOiJIUzI1NiJ9." +
                "eyJzdWIiOiJPbGVrc2lpIiwiaWF0IjoxNjg4OTkwNjAwLCJleHAiOjE2ODg5OTIwNDB9" +
                ".U-gnRTNtyE6pbhi-3P6qFfNd9dYk0sfT6zzJYu0vFrI");

        when(userService.login(request)).thenReturn(response);
        mockMvc.perform(post("/api/v1/users/auth")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.token", Matchers.is(response.getToken())));

        verify(userService, times(1)).login(request);
    }

    @Test
    public void login_WhenLoginDoesNotExist_Test() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("Oleksii", "pass123");
        String jsonContent = objectMapper.writeValueAsString(request);

        doThrow(new SecurityException()).when(userService).login(request);
        mockMvc.perform(post("/api/v1/users/auth")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(userService, times(1)).login(request);
    }

    @Test
    public void getAllContactsByUser_WhenOk_Test() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("Oleksii", "pass123");
        ContactDto contactFirst = ContactDto.builder()
                .name("Leno4ka")
                .emails(Set.of("Leka@gmail.com", "lena999@gmail.com"))
                .phones(Set.of("+380 93 933 3333", "+380 93 933 3334", "+380 93 933 3335"))
                .build();
        ContactDto contactSecond = ContactDto.builder()
                .name("Petya")
                .emails(Set.of("petro@gmail.com", "petgolenya@gmail.com"))
                .phones(Set.of("+38094 9331243", "+380504563334"))
                .build();
        List<Contact> contactList = Stream.of(contactFirst, contactSecond)
                .map(contactDto -> Contact.builder()
                        .name(contactDto.getName())
                        .emails(contactDto.getEmails())
                        .phones(contactDto.getPhones()).build())
                .toList();
        Pageable pageRequest = PageRequest.of(0, 10, Sort.unsorted());
        Page<Contact> contacts = new PageImpl<>(contactList, pageRequest, contactList.size());
        String expectedJsonResult = """
                {
                     "content": [
                         {
                             "name": "Petya",
                             "emails": [
                                 "petro@gmail.com",
                                 "petgolenya@gmail.com"
                             ],
                             "phones": [
                                 "+38094 9331243",
                                 "+380504563334"
                             ]
                         },
                         {
                             "name": "Leno4ka",
                             "emails": [
                                 "Leka@gmail.com",
                                 "lena999@gmail.com"
                             ],
                             "phones": [
                                 "+380 93 933 3333",
                                 "+380 93 933 3334",
                                 "+380 93 933 3335"
                             ]
                         }
                     ],
                     "pageable": {
                         "sort": {
                             "empty": true,
                             "sorted": false,
                             "unsorted": true
                         },
                         "offset": 0,
                         "pageNumber": 0,
                         "pageSize": 10,
                         "paged": true,
                         "unpaged": false
                     },
                     "totalElements": 2,
                     "totalPages": 1,
                     "last": true,
                     "size": 10,
                     "number": 0,
                     "sort": {
                         "empty": true,
                         "sorted": false,
                         "unsorted": true
                     },
                     "first": true,
                     "numberOfElements": 2,
                     "empty": false
                 }
                """;
        when(userService.getAllContactsByLogin(request.getLogin(), pageRequest, null))
                .thenReturn(contacts);

        mockMvc.perform(get("/api/v1/users/{login}/contacts", request.getLogin()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJsonResult));

        verify(userService, times(1)).getAllContactsByLogin(request.getLogin(), pageRequest, null);
    }

    @Test
    public void getAllContactsByUser_WhenNotOwnerLogin_Test() throws Exception {
        Pageable pageRequest = PageRequest.of(0, 10, Sort.unsorted());
        String expectedJson = """
                {
                    "type": "about:blank",
                    "title": "Forbidden",
                    "status": 403,
                    "detail": "Illegal access",
                    "instance": "/api/v1/users/Misha/contacts",
                    "problemDetails": [
                        {
                            "message": "Access is not allowed"
                        }
                    ]
                }
                """;
        doThrow(new SecurityException("Access is not allowed"))
                .when(userService).getAllContactsByLogin("Misha", pageRequest, null);

        mockMvc.perform(get("/api/v1/users/{login}/contacts", "Misha"))
                .andExpect(status().isForbidden())
                .andExpect(content().json(expectedJson));

        verify(userService, times(1)).getAllContactsByLogin("Misha", pageRequest, null);
    }
}
