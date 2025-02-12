package org.example.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Сущность статуса пользователя
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "\"Status\"", schema = "public")
public class Status {
    /**
     * id статуса
     */
    @Id
    @Column(name = "\"Id_Status\"")
    private Long idStatus;
    /**
     * Название статуса
     */
    @Column(name = "\"Status_Name\"")
    private String statusName;
}
