package com.fifthdimension.digital_twin.global.util;

import com.fifthdimension.digital_twin.global.entity.Point;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "PolygonUtil")
public class PolygonUtils {

    // 좌표 정상 다각형으로 변환 처리 메서드
    // OpenSearch에 내장되어있는 JTS 라이브러리 활용
    public static List<Point> normalizePolygon(List<Point> points) {
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coords = points.stream()
                .map(pt -> new Coordinate(pt.getX(), pt.getY()))
                .toArray(Coordinate[]::new);

        // Convex Hull을 구해 항상 정상(비꼬임) 다각형으로!
        ConvexHull hull = new ConvexHull(coords, gf);
        Geometry geom = hull.getConvexHull();

        // (다각형 아닌 경우 예외 처리)
        if (!(geom instanceof Polygon)) {
            log.error("입력값이 충분히 polygon을 만들지 못함 : {}", geom);
            throw new CustomException(HttpStatus.BAD_REQUEST, "입력값이 충분히 polygon을 만들지 못함");
        }

        Polygon polygon = (Polygon) geom;
        Coordinate[] orderedCoords = polygon.getExteriorRing().getCoordinates();

        // JTS는 polygon의 마지막 꼭짓점을 첫 번째와 중복 저장하므로, 마지막은 생략
        List<Point> result = new ArrayList<>();
        for (int i = 0; i < orderedCoords.length - 1; i++) {
            result.add(new Point(orderedCoords[i].x, orderedCoords[i].y));
        }
        return result;
    }
}
