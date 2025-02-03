package org.example.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
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

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getUsername() {
        return username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getIdStatus() {
        return idStatus;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
