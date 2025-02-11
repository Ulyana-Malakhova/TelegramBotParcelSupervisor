package org.example.Entity;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "\"Id_Status\"")
    private Status status;
    @Column(name = "\"Email\"")
    private String email;
    @Column(name = "\"Password\"")
    private String password;
}
