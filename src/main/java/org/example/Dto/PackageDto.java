package org.example.Dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageDto {
    private Long idPackage;
    private String trackNumber;
    private String namePackage;
    private LocalDate departureDate;
    private LocalDate receiptDate;
    private Long idUser;
    private String nameRole;
    private String nameTrackingStatus;
}
