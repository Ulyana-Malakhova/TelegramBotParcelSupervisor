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
@Table(name = "\"Status\"", schema = "public")
public class Status {
    @Id
    @Column(name = "\"Id_Status\"")
    private Long idStatus;
    @Column(name = "\"Status_Name\"")
    private String statusName;
}
