package com.fifthdimension.digital_twin.map.application;

import com.fifthdimension.digital_twin.global.exception.CustomException;
import com.fifthdimension.digital_twin.map.domain.Map;
import com.fifthdimension.digital_twin.map.domain.MapRepository;
import com.fifthdimension.digital_twin.map.dto.MapCreateReqDto;
import com.fifthdimension.digital_twin.map.dto.MapResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j(topic = "Map Service")
public class MapService {

    private final MapRepository mapRepository;

    @Transactional
    public MapResDto createMap(MapCreateReqDto mapCreateReqDto) {

        try{
            return MapResDto.from(mapRepository.save(mapCreateReqDto.toEntity()));
        }catch (Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public MapResDto getMap(Integer mapId) {
        Map map;
        try{
            map = mapRepository.findById(mapId).orElseThrow();
            return MapResDto.from(map);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Map 정보가 존재하지 않습니다.");
        }
    }

    @Transactional
    public void deleteMap(Integer mapId, UUID requestUserId) {
        Map map;
        try{
            map = mapRepository.findById(mapId).orElseThrow();
        }catch (Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Map 정보가 존재하지 않습니다.");
        }

        try{
            map.softDelete(requestUserId);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
        }
    }

    public Page<MapResDto> searchMaps(String name, Pageable pageable) {
        try {
            if (name == null || name.isBlank()) {
                // 검색 조건 없으면 전체 조회
                return mapRepository.findAll(pageable).map(MapResDto::from);
            } else {
                // Name이 있는 경우
                return mapRepository.findAllByNameContaining(name, pageable).map(MapResDto::from);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
        }

    }
}
