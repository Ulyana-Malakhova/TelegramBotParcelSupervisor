package org.example.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Сущность посылки
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"Package\"", schema = "public")
public class Package {
    /**
     * Идентификатор посылки
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id_Package\"")
    private Long idPackage;
    /**
     * Трек-номер посылки
     */
    @Column(name = "\"Track_Number\"")
    private String trackNumber;
    /**
     * Имя посылки
     */
    @Column(name = "\"Name_Package\"")
    private String namePackage;
    /**
     * Дата отправки
     */
    @Column(name = "\"Departure_Date\"")
    private LocalDate departureDate;
    /**
     * Дата получения
     */
    @Column(name = "\"Receipt_Date\"")
    private LocalDate receiptDate;
    /**
     * Сущность пользователя
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"Id\"")
    private User userEntity;
    /**
     * Сущность роли
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "\"Id_Role\"")
    private Role roleEntity;
    /**
     * Сущность статуса отслеживания
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "\"Id_Tracking_Status\"")
    private TrackingStatus trackingStatusEntity;
}
