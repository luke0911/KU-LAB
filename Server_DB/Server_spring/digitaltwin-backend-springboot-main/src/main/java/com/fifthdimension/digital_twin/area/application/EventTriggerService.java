package com.fifthdimension.digital_twin.area.application;

import com.fifthdimension.digital_twin.area.domain.EventArea;
import com.fifthdimension.digital_twin.area.domain.EventAreaRepository;
import com.fifthdimension.digital_twin.area.domain.EventTrigger;
import com.fifthdimension.digital_twin.area.domain.EventTriggerRepository;
import com.fifthdimension.digital_twin.area.dto.EventTriggerCreateReqDto;
import com.fifthdimension.digital_twin.area.dto.EventTriggerResDto;
import com.fifthdimension.digital_twin.area.dto.EventTriggerUpdateReqDto;
import com.fifthdimension.digital_twin.global.exception.CustomException;
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
@Slf4j(topic = "Event Trigger Service")
public class EventTriggerService {

    private final EventAreaRepository eventAreaRepository;
    private final EventTriggerRepository eventTriggerRepository;


    @Transactional
    public EventTriggerResDto createEventTrigger(EventTriggerCreateReqDto req) {
        EventArea area;
        try {
            area = eventAreaRepository.findById(req.getEventAreaId()).orElseThrow();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Event Area 정보가 존재하지 않습니다.");
        }


        try {
            EventTrigger entity = req.toEntity(area);
            EventTrigger saved = eventTriggerRepository.save(entity);
            return EventTriggerResDto.from(saved);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "EventTrigger 저장 중 오류 발생");
        }
    }

    public EventTriggerResDto getEventTrigger(Long triggerId) {
        try {
            return EventTriggerResDto.from(eventTriggerRepository.findById(triggerId).orElseThrow());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "EventTrigger 정보가 존재하지 않습니다.");
        }
    }

    @Transactional
    public EventTriggerResDto updateEventTrigger(EventTriggerUpdateReqDto req) {
        EventTrigger trigger;
        try {
            trigger = eventTriggerRepository.findById(req.getTriggerId()).orElseThrow();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "EventTrigger 정보가 존재하지 않습니다.");
        }

        try {
            trigger.updateTrigger(
                    req.getTriggerName(),
                    req.getTargetUserRoles(),
                    req.getTriggerType(),
                    req.getEventMessage(),
                    req.getEventMessageType(),
                    req.getDelay(),
                    req.getIsActive()
            );
            return EventTriggerResDto.from(trigger);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "EventTrigger 업데이트 중 오류 발생");
        }
    }

    @Transactional
    public void deleteEventTrigger(UUID requestUserId, Long triggerId) {
        EventTrigger trigger;
        try {
            trigger = eventTriggerRepository.findById(triggerId).orElseThrow();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "EventTrigger 정보가 존재하지 않습니다.");
        }

        try {
            trigger.softDelete(requestUserId);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "EventTrigger 삭제 중 오류 발생");
        }
    }

    // 이벤트 트리거 목록 조회 (eventAreaId 기준)
    public Page<EventTriggerResDto> searchEventTriggers(Long eventAreaId, String name, Pageable pageable) {
        // Area 존재 여부 검증
        try {
            eventAreaRepository.findById(eventAreaId).orElseThrow();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Event Area 정보가 존재하지 않습니다.");
        }

        try {
            if (name == null || name.isBlank()) {
                return eventTriggerRepository.findAllByEventAreaId(eventAreaId, pageable)
                        .map(EventTriggerResDto::from);
            } else {
                return eventTriggerRepository.findAllByEventAreaIdAndTriggerNameContaining(
                        eventAreaId, name, pageable).map(EventTriggerResDto::from);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "EventTrigger 목록 조회 오류");
        }
    }
}

