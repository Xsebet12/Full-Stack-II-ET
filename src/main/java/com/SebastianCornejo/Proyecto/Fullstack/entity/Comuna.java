package com.SebastianCornejo.Proyecto.Fullstack.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "comuna")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Comuna {
    @Id
    @Column(name = "id_comuna")
    private Integer idComuna;

    @Column(name = "nom_comuna", nullable = false, length = 100)
    private String nomComuna;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_region", nullable = false)
    private Region region;
}
