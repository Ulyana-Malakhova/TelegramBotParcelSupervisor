package org.example.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class GroupPackageId implements Serializable {
    @Column(name = "\"Id_Package\"")
    private Long packageId;

    @Column(name = "\"Id_Group\"")
    private Long groupId;
}
