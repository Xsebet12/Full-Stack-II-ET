package com.SebastianCornejo.Proyecto.Fullstack.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "region")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Region {
    @Id
    @Column(name = "id_region")
    private Integer idRegion;

    @Column(name = "nom_region", nullable = false, length = 100)
    private String nomRegion;
}
