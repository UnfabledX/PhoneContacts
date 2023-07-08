package com.chiacademy.software.phonecontacts.controller;

import com.chiacademy.software.phonecontacts.exception.NotFoundException;
import com.chiacademy.software.phonecontacts.model.Contact;
import com.chiacademy.software.phonecontacts.model.dto.ContactDto;
import com.chiacademy.software.phonecontacts.service.ContactService;
import com.chiacademy.software.phonecontacts.service.JwtService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ContactControllerUnitTest {

    @MockBean
    private ContactService contactService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void init(){
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void createContact_WhenOk_Test() throws Exception {
        ContactDto contactDto = ContactDto.builder()
                .name("Lena")
                .emails(Set.of("Leka@gmail.com", "lena999@gmail.com"))
                .phones(Set.of("+380 93 933 3333", "+380 93 933 3334", "+380 93 933 3335"))
                .build();
        String jsonContent = objectMapper.writeValueAsString(contactDto);
        when(contactService.create(contactDto, null)).thenReturn(contactDto);
        mockMvc.perform(post("/api/v1/contacts/create")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.name", Matchers.is("Lena")))
                .andExpect(jsonPath("$.emails").isArray());

        verify(contactService, Mockito.times(1)).create(contactDto, null);
    }

    @Test
    public void createContact_WhenNameInvalid_Test() throws Exception {
        ContactDto contactDto = ContactDto.builder()
                .name("A1")
                .emails(Set.of("Leka@gmail.com", "lena999@gmail.com"))
                .phones(Set.of("+380 93 933 3333", "+380 93 933 3334", "+380 93 933 3335"))
                .build();
        String jsonContent = objectMapper.writeValueAsString(contactDto);
        String expectedJsonResult = """
                {
                    "type": "about:blank",
                    "title": "Bad Request",
                    "status": 400,
                    "detail": "Failed validation",
                    "instance": "/api/v1/contacts/create",
                    "problemDetails": [
                        {
                            "message": "Contact name mustn't be bigger then 24 letters and less then 3 letters",
                            "field": "name",
                            "wrongValue": "A1"
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/contacts/create")
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedJsonResult));

        verify(contactService, Mockito.times(0)).create(contactDto, null);
    }

    @Test
    public void deleteContact_WhenOK_Test() throws Exception {
        String contactName = "Lena";
        doNothing().when(contactService).delete(contactName, null);
        mockMvc.perform(delete("/api/v1/contacts/delete").param("contact", contactName))
                .andExpect(status().isOk());
        verify(contactService, Mockito.times(1)).delete(contactName, null);
    }

    @Test
    public void deleteContact_WhenNameDoesNotExist_Test() throws Exception {
        String contactName = "alien";
        String expectedJsonResult = """
                {
                     "type": "about:blank",
                     "title": "Not Found",
                     "status": 404,
                     "detail": "Data is not found",
                     "instance": "/api/v1/contacts/delete",
                     "problemDetails": [
                         {
                             "message": "There is no contact present by such name",
                             "wrongValue": "alien"
                         }
                     ]
                 }
                """;
        doThrow(new NotFoundException("There is no contact present by such name", contactName))
                .when(contactService).delete(contactName, null);
        mockMvc.perform(delete("/api/v1/contacts/delete").param("contact", contactName))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json(expectedJsonResult));
        verify(contactService, times(1)).delete(contactName, null);
    }

    @Test
    public void editContact_WhenOK_Test() throws Exception {
        ContactDto contactDto = ContactDto.builder()
                .name("Leno4ka")
                .emails(Set.of("Leka@gmail.com", "lena999@gmail.com"))
                .phones(Set.of("+380 93 933 3333", "+380 93 933 3334", "+380 93 933 3335"))
                .build();
        String oldContactName = "Lena";
        Contact updatedContact = Contact.builder()
                .name("Leno4ka")
                .emails(Set.of("Leka@gmail.com", "lena999@gmail.com"))
                .phones(Set.of("+380 93 933 3333", "+380 93 933 3334", "+380 93 933 3335"))
                .build();
        when(contactService.editContactByName(contactDto, oldContactName, null))
                .thenReturn(updatedContact);
        String jsonContent = objectMapper.writeValueAsString(contactDto);

        mockMvc.perform(put("/api/v1/contacts/{contact}/edit", oldContactName)
                .content(jsonContent)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", Matchers.is(updatedContact.getName())));

        verify(contactService, times(1)).editContactByName(contactDto, oldContactName, null);
    }

    @Test
    public void editContact_WhenContactDoesNotExist_Test() throws Exception {
        ContactDto contactDto = ContactDto.builder()
                .name("Leno4ka")
                .emails(Set.of("Leka@gmail.com", "lena999@gmail.com"))
                .phones(Set.of("+380 93 933 3333", "+380 93 933 3334", "+380 93 933 3335"))
                .build();
        String oldContactName = "unknown";
        String expectedJsonResult = """
                {
                     "type": "about:blank",
                     "title": "Not Found",
                     "status": 404,
                     "detail": "Data is not found",
                     "instance": "/api/v1/contacts/unknown/edit",
                     "problemDetails": [
                         {
                             "message": "There is no contact present by such name",
                             "wrongValue": "unknown"
                         }
                     ]
                 }
                """;
        doThrow(new NotFoundException("There is no contact present by such name", oldContactName))
                .when(contactService).editContactByName(contactDto, oldContactName, null);
        String jsonContent = objectMapper.writeValueAsString(contactDto);

        mockMvc.perform(put("/api/v1/contacts/{contact}/edit", oldContactName)
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(expectedJsonResult));

        verify(contactService, times(1)).editContactByName(contactDto, oldContactName, null);
    }
}
