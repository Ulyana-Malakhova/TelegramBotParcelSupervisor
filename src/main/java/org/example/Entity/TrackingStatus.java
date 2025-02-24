package org.example.Entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Сущность статуса отслеживания
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"Tracking_Status\"", schema = "public")
public class TrackingStatus {
    /**
     * Идентификатор статуса отслеживания
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id_Tracking_Status\"")
    private Long idTrackingStatus;
    /**
     * Название статуса отслеживания
     */
    @Column(name = "\"Name_Tracking_Status\"")
    private String nameTrackingStatus;
}
