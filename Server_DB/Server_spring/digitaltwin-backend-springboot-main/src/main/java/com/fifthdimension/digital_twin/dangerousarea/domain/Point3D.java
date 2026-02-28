package com.fifthdimension.digital_twin.dangerousarea.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Point3D {
    private Double x;
    private Double y;
    private Double z;
}
