package com.chiacademy.software.phonecontacts.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactDto {

    @Size(min = 3, max = 24, message = "Contact name mustn't be bigger then 24 letters and less then 3 letters")
    private String name;

    Set<@Email(message = "Email is written in a wrong format")
        @NotEmpty(message = "Email must not be empty")String> emails;

    Set<@NotEmpty(message = "Phone must not be empty")
        @Pattern(regexp = "\\+[0-9]{3}\\s?[0-9]{2}\\s?[0-9]{3}\\s?[0-9]{4}",
                message = "Invalid phone number. " +
                        "Valid format is +380 93 123 4567 or without spaces +380931234567") String> phones;
}
