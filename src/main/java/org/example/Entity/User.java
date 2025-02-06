package org.example.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "\"User\"", schema = "public")
public class User {
    @Id
    @Column(name = "\"Id_User\"")
    private Long id;
    @Column(name = "\"Name\"")
    private String name;
    @Column(name = "\"Surname\"")
    private String surname;
    @Column(name = "\"Username\"")
    private String username;
    @Column(name = "\"Phone_Number\"")
    private String phoneNumber;
    @Column(name = "\"Id_Status\"")
    private int idStatus;
    @Column(name = "\"Email\"")
    private String email;
    @Column(name = "\"Password\"")
    private String password;
}
