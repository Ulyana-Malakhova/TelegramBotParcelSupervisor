package org.example.Entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Сущность роли
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"Role\"", schema = "public")
public class Role {
    /**
     * Идентификатор роли
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id_Role\"")
    private Long idRole;
    /**
     * Название роли
     */
    @Column(name = "\"Name_Role\"")
    private String nameRole;
}
