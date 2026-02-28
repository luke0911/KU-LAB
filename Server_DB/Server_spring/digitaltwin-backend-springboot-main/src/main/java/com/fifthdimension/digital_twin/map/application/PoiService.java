package com.fifthdimension.digital_twin.map.application;

import com.fifthdimension.digital_twin.global.exception.CustomException;
import com.fifthdimension.digital_twin.map.domain.Map;
import com.fifthdimension.digital_twin.map.domain.MapRepository;
import com.fifthdimension.digital_twin.map.domain.Poi;
import com.fifthdimension.digital_twin.map.domain.PoiRepository;
import com.fifthdimension.digital_twin.map.dto.PoiCreateReqDto;
import com.fifthdimension.digital_twin.map.dto.PoiResDto;
import com.fifthdimension.digital_twin.map.dto.PoiUpdateReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j(topic = "POI Service")
public class PoiService {

    private final MapRepository mapRepository;
    private final PoiRepository poiRepository;

    // Redis Caching 용
    private final CacheManager cacheManager;

    @Transactional
    public PoiResDto createPoi(PoiCreateReqDto poiCreateReqDto) {
        Map map;
        try{
            map = mapRepository.findById(poiCreateReqDto.getMapId()).orElseThrow();
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Map 정보가 존재하지 않습니다.");
        }

        try{
            PoiResDto result = PoiResDto.from(poiRepository.save(poiCreateReqDto.toEntity(map)));
            evictPoiCache(result.getMapId(), result.getPoiFloor());
            return result;
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
        }
    }

    public PoiResDto getPoi(Long poiId) {
        try{
            return PoiResDto.from(poiRepository.findById(poiId).orElseThrow());
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "POI 정보가 존재하지 않습니다.");
        }
    }

    public List<PoiResDto> getPoisByMapIdAndFloor(Integer mapId, Float floor) {
        int floorInt = floor.intValue();
        String cacheKey = "map:" + mapId + ":floor:" + floorInt;
        Cache cache = cacheManager.getCache("pois");

        // 1. 캐시 확인
        List<PoiResDto> cached = cache.get(cacheKey, List.class);
        if (cached != null) {
            return cached;
        }

        // 2. [floorInt, floorInt+1) 구간의 POI 조회
        float floorStart = (float) floorInt;
        float floorEnd = floorStart + 1.0f;
        List<Poi> pois = poiRepository.findAllByMapIdAndFloorGreaterThanEqualAndFloorLessThan(mapId, floorStart, floorEnd);

        // 3. DTO 변환 후 캐시 저장
        List<PoiResDto> result = pois.stream().map(PoiResDto::from).collect(Collectors.toList());
        cache.put(cacheKey, result);
        return result;
    }

    @Transactional
    public PoiResDto updatePoi(PoiUpdateReqDto poiUpdateReqDto) {
        Poi poi;
        try{
            poi = poiRepository.findById(poiUpdateReqDto.getPoiId()).orElseThrow();
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "POI 정보가 존재하지 않습니다.");
        }

        try{
            poi.updatePoiInfo(
                    poiUpdateReqDto.getPoiName(),
                    poiUpdateReqDto.getPoiFloor(),
                    poiUpdateReqDto.getPoiPoints(),
                    poiUpdateReqDto.getPoiCategory(),
                    poiUpdateReqDto.getPoiDescription()
                    );
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
        }

        PoiResDto result =  PoiResDto.from(poi);
        evictPoiCache(result.getMapId(), result.getPoiFloor());
        return result;
    }

    @Transactional
    public void deletePoi(Long poiId, UUID requestUserId) {
        Poi poi;
        try{
            poi = poiRepository.findById(poiId).orElseThrow();
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "POI 정보가 존재하지 않습니다.");
        }

        try{
            poi.softDelete(requestUserId);
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
        }
    }

    public Page<PoiResDto> searchPois(Integer mapId, String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            // keyword가 있는 경우 poiName로 검색
            try{
                return poiRepository.findAllByMapIdAndNameContainingIgnoreCase(mapId, keyword, pageable).map(PoiResDto::from);
            }catch(Exception e){
                log.error(e.getMessage());
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
            }
        } else {
            // keyword가 없는 경우 모든 POI를 페이징하여 반환
            try{
                return poiRepository.findAllByMapId(mapId, pageable).map(PoiResDto::from);
            }catch(Exception e){
                log.error(e.getMessage());
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
            }
        }
    }

    private void evictPoiCache(Integer mapId, Float floor) {
        int floorInt = floor.intValue();
        String cacheKey = "map:" + mapId + ":floor:" + floorInt;
        Cache cache = cacheManager.getCache("pois");
        if (cache != null) {
            cache.evict(cacheKey); // 캐시 무효화
            log.debug("[Cache] Evicted cache for key {}", cacheKey);
        }
    }
}
