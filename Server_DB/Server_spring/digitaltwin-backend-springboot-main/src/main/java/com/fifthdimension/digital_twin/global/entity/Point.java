package com.fifthdimension.digital_twin.global.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Point {
    private Double x;
    private Double y;

    // Points 중심 좌표를 계산하기 위한 static method
    public static Point calculateCenterPoint(List<Point> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("points is null or empty");
        }

        double sumX = 0.0;
        double sumY = 0.0;

        for (Point point : points) {
            sumX += point.getX();
            sumY += point.getY();
        }

        return new Point(sumX / points.size(), sumY / points.size());
    }
}
