package com.fifthdimension.digital_twin.area.application;

import com.fifthdimension.digital_twin.area.domain.EventArea;
import com.fifthdimension.digital_twin.area.domain.EventAreaRepository;
import com.fifthdimension.digital_twin.area.dto.EventAreaCreateReqDto;
import com.fifthdimension.digital_twin.area.dto.EventAreaResDto;
import com.fifthdimension.digital_twin.area.dto.EventAreaUpdateReqDto;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import com.fifthdimension.digital_twin.map.domain.Map;
import com.fifthdimension.digital_twin.map.domain.MapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
@Slf4j(topic = "Event Area Service")
public class EventAreaService {

    private final MapRepository mapRepository;
    private final EventAreaRepository eventAreaRepository;
    private final CacheManager cacheManager;

    @Transactional
    public EventAreaResDto createEventArea(EventAreaCreateReqDto eventAreaCreateReqDto) {
        Map map;
        try{
            map = mapRepository.findById(eventAreaCreateReqDto.getMapId()).orElseThrow();
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Map 정보가 존재하지 않습니다.");
        }

        try{
            EventAreaResDto result = EventAreaResDto.from(eventAreaRepository.save(eventAreaCreateReqDto.toEntity(map)));
            evictEventAreaCache(result.getMapId(), result.getEventAreaFloor());
            return result;
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
        }
    }

    public EventAreaResDto getEventArea(Long areaId) {
        try{
            return EventAreaResDto.from(eventAreaRepository.findById(areaId).orElseThrow());
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Custom Area 정보가 존재하지 않습니다.");
        }
    }

    public List<EventAreaResDto> getEventAreasByMapIdAndFloor(Integer mapId, Float floor) {
        int floorInt = floor.intValue();
        String cacheKey = "map:" + mapId + ":floor:" + floorInt;
        Cache cache = cacheManager.getCache("eventAreas");

        // 캐시 우선 조회
        List<EventAreaResDto> cached = cache != null ? cache.get(cacheKey, List.class) : null;
        if (cached != null) {
            log.debug("[Cache] Hit EventAreas for {}", cacheKey);
            return cached;
        }

        // 2. DB 조회
        float floorStart = (float) floorInt;
        float floorEnd = floorStart + 1.0f;
        List<EventArea> eventAreas = eventAreaRepository
                .findAllByMapIdAndFloorGreaterThanEqualAndFloorLessThan(mapId, floorStart, floorEnd);

        List<EventAreaResDto> result = eventAreas.stream().map(EventAreaResDto::from).collect(Collectors.toList());
        if (cache != null) cache.put(cacheKey, result);
        return result;
    }

    @Transactional
    public EventAreaResDto updateEventArea(EventAreaUpdateReqDto eventAreaUpdateReqDto) {
        EventArea area;
        try{
            area = eventAreaRepository.findById(eventAreaUpdateReqDto.getAreaId()).orElseThrow();
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Custom Area 정보가 존재하지 않습니다.");
        }

        try{
            area.updateCustomArea(
                    eventAreaUpdateReqDto.getAreaName(),
                    eventAreaUpdateReqDto.getFloor(),
                    eventAreaUpdateReqDto.getPoints(),
                    eventAreaUpdateReqDto.getAreaDescription(),
                    eventAreaUpdateReqDto.getEventAreaType()
            );
            evictEventAreaCache(area.getMap().getId(), area.getFloor());
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
        }

        return EventAreaResDto.from(area);
    }

    @Transactional
    public void deleteEventArea(Long areaId, UUID requestUserId) {
        EventArea area;
        try{
            area = eventAreaRepository.findById(areaId).orElseThrow();
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Custom Area 정보가 존재하지 않습니다.");
        }

        try{
            area.softDelete(requestUserId);
            evictEventAreaCache(area.getMap().getId(), area.getFloor());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
        }
    }

    public Page<EventAreaResDto> searchEventAreas(Integer mapId, String name, Pageable pageable) {
        // Map ID 검증
        try{
            mapRepository.findById(mapId).orElseThrow();
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Map 정보가 존재하지 않습니다.");
        }

        try {
            if (name == null || name.isBlank()) {
                // 검색 조건 없으면 전체 조회
                return eventAreaRepository.findAllByMapId(mapId, pageable).map(EventAreaResDto::from);
            } else {
                // Name이 있는 경우
                return eventAreaRepository.findAllByMapIdAndNameContaining(mapId, name, pageable).map(EventAreaResDto::from);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
        }
    }

    // 캐시 무효화 메서드
    public void evictEventAreaCache(Integer mapId, Float floor) {
        int floorInt = floor.intValue();
        String cacheKey = "map:" + mapId + ":floor:" + floorInt;
        Cache cache = cacheManager.getCache("eventAreas");
        if (cache != null) {
            cache.evict(cacheKey);
            log.debug("[Cache] Evicted cache for key {}", cacheKey);
        }
    }
}
