package org.example.Entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "\"Group\"", schema = "public")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id_Group\"")
    private Long id;
    @Column(name = "\"Name_Group\"")
    private String text;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "\"Id_User\"")
    private User user;
}
