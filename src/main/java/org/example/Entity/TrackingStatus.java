package org.example.Entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"Tracking_Status\"", schema = "public")
public class TrackingStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id_Tracking_Status\"")
    private Long idTrackingStatus;
    @Column(name = "\"Name_Tracking_Status\"")
    private String nameTrackingStatus;
}
