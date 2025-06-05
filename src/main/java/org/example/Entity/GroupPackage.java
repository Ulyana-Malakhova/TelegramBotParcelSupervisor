package org.example.Entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "\"Group_Package\"", schema = "public")
public class GroupPackage {
    @EmbeddedId
    private GroupPackageId id;
    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("packageId")
    @JoinColumn(name = "\"Id_Package\"")
    private Package packageEntity;
    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("groupId")
    @JoinColumn(name = "\"Id_Group\"")
    private Group group;
}
