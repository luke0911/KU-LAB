package com.fifthdimension.digital_twin.dangerousarea.application;

import com.fifthdimension.digital_twin.dangerousarea.domain.DangerousArea;
import com.fifthdimension.digital_twin.dangerousarea.domain.DangerousAreaRepository;
import com.fifthdimension.digital_twin.dangerousarea.dto.DangerousAreaCreateReqDto;
import com.fifthdimension.digital_twin.dangerousarea.dto.DangerousAreaResDto;
import com.fifthdimension.digital_twin.dangerousarea.dto.DangerousAreaUpdateReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DangerousAreaService {

    private final DangerousAreaRepository dangerousAreaRepository;

    @Transactional
    public DangerousAreaResDto createDangerousArea(DangerousAreaCreateReqDto req) {
        DangerousArea area = dangerousAreaRepository.save(req.toEntity());
        return DangerousAreaResDto.from(area);
    }

    @Transactional(readOnly = true)
    public List<DangerousAreaResDto> getDangerousAreasByMapId(Integer mapId) {
        return dangerousAreaRepository.findAllByMapId(mapId).stream()
                .map(DangerousAreaResDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DangerousAreaResDto getDangerousAreaById(Long areaId) {
        DangerousArea area = dangerousAreaRepository.findById(areaId)
                .orElseThrow(() -> new IllegalArgumentException("해당 AreaId가 존재하지 않습니다: " + areaId));
        return DangerousAreaResDto.from(area);
    }

    @Transactional
    public DangerousAreaResDto updateDangerousArea(Long areaId, DangerousAreaUpdateReqDto req) {
        DangerousArea area = dangerousAreaRepository.findById(areaId)
                .orElseThrow(() -> new IllegalArgumentException("해당 AreaId가 존재하지 않습니다: " + areaId));

        area.updateDangerousArea(
                req.getAreaName(),
                req.getAreaFloor(),
                req.getAreaType(),
                req.getAreaPoints(),
                req.getAreaDescripting(),
                req.getEntryTime(),
                req.getExitTime()
        );
        return DangerousAreaResDto.from(area);
    }

    @Transactional
    public void deleteDangerousArea(Long areaId) {
        DangerousArea area = dangerousAreaRepository.findById(areaId)
                .orElseThrow(() -> new IllegalArgumentException("해당 AreaId가 존재하지 않습니다: " + areaId));
        dangerousAreaRepository.delete(area);
    }
}
