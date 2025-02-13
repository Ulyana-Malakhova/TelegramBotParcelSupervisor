package org.example.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"Package\"", schema = "public")
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id_Package\"")
    private Long idPackage;
    @Column(name = "\"Track_Number\"")
    private String trackNumber;
    @Column(name = "\"Name_Package\"")
    private String namePackage;
    @Column(name = "\"Departure_Date\"")
    private LocalDate departureDate;
    @Column(name = "\"Receipt_Date\"")
    private LocalDate receiptDate;
    @ManyToOne(fetch = FetchType.LAZY)
    private User userEntity;
    @ManyToOne(fetch = FetchType.EAGER)
    private Role roleEntity;
    @ManyToOne(fetch = FetchType.EAGER)
    private TrackingStatus trackingStatusEntity;
}
