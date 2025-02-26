package org.example.Dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Date;

/**
 * DTO-объект посылки
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageDto {
    /**
     * Идентификатор посылки
     */
    private Long idPackage;
    /**
     * Трек-номер посылки
     */
    private String trackNumber;
    /**
     * Имя посылки
     */
    private String namePackage;
    /**
     * Дата отправки посылки
     */
    private Date departureDate;
    /**
     * Дата получения посылки
     */
    private Date receiptDate;
    /**
     * id пользователя
     */
    private Long idUser;
    /**
     * Название роли (отправитель, получатель)
     */
    private String nameRole;
    /**
     * Название статуса отслеживания (отслеживается, не отслеживается)
     */
    private String nameTrackingStatus;
}
