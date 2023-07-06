package com.chiacademy.software.phonecontacts.security;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {
    @Size(min = 3, max = 24, message = "Login mustn't be bigger then 24 letters and less then 3 letters")
    private String login;

    @Size(min = 5, max = 64, message = "Name mustn't be bigger then 64 and less then 5 characters")
    private String password;
}
