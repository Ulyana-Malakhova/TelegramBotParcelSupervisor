package org.example.Dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String surname;
    private String username;
    private String phoneNumber;
    private Long idStatus;
    private String email;
    private String password;
}
