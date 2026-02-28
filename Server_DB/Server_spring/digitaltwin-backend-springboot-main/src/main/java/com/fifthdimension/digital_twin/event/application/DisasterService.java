package com.fifthdimension.digital_twin.event.application;

import com.fifthdimension.digital_twin.event.domain.DevicePlatform;
import com.fifthdimension.digital_twin.event.domain.Disaster;
import com.fifthdimension.digital_twin.event.domain.DisasterRepository;
import com.fifthdimension.digital_twin.event.domain.EventStatus;
import com.fifthdimension.digital_twin.event.dto.DisasterCreateReqDto;
import com.fifthdimension.digital_twin.event.dto.DisasterResDto;
import com.fifthdimension.digital_twin.event.dto.EventStatusUpdateReqDto;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import com.fifthdimension.digital_twin.map.domain.Map;
import com.fifthdimension.digital_twin.map.domain.MapRepository;
import com.fifthdimension.digital_twin.pushmsg.application.PushMessageService;
import com.fifthdimension.digital_twin.user.domain.User;
import com.fifthdimension.digital_twin.user.domain.UserRepository;
import com.fifthdimension.digital_twin.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j(topic = "Disaster Service")
public class DisasterService {

    private final MapRepository mapRepository;
    private final DisasterRepository disasterRepository;
    private final UserRepository userRepository;
    private final PushMessageService pushMessageService;

    @Transactional
    public DisasterResDto createDisaster(UUID reporterId, DisasterCreateReqDto disasterCreateReqDto) {
        Map map;
        try{
            map = mapRepository.findById(disasterCreateReqDto.getMapId()).orElseThrow();
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Map 정보가 존재하지 않습니다.");
        }

        User reporter;
        try{
            reporter = userRepository.findById(reporterId).orElseThrow();
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Reporter 정보가 존재하지 않습니다.");
        }
        
        // Reporter가 Admin이 아닌 경우 Admin Web에 Push 알람 필요
        if(!reporter.getRole().equals(UserRole.ADMIN)) {
            String title = "Disaster Report : " + disasterCreateReqDto.getDisasterType().getValue();
            String body = "내용 : " + disasterCreateReqDto.getEventDetails() + "\n" +
                    "제보자 : " + reporter.getAccountId();
            pushMessageService.sendToRolesAndPlatforms(
                    Collections.singletonList(UserRole.ADMIN),
                    Collections.singletonList(DevicePlatform.WEB),
                    title,
                    body
            );
        }

        try{
            return DisasterResDto.from(disasterRepository.save(disasterCreateReqDto.toEntity(map, reporter)));
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
        }
    }

    public DisasterResDto getDisaster(Long disasterId) {
        try{
            return DisasterResDto.from(disasterRepository.findById(disasterId).orElseThrow());
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Disaster 정보가 존재하지 않습니다.");
        }
    }

    @Transactional
    public DisasterResDto updateEventStatus(Long disasterId, EventStatusUpdateReqDto eventStatusUpdateReqDto, UUID handlerId) {
        Disaster disaster;
        try{
            disaster = disasterRepository.findById(disasterId).orElseThrow();
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Disaster 정보가 존재하지 않습니다.");
        }

        User handler;
        try{
            handler = userRepository.findById(handlerId).orElseThrow();
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.NOT_FOUND, "Handler 정보가 존재하지 않습니다.");
        }

        try{
            switch (eventStatusUpdateReqDto.getEventStatus()) {
                case RECEIVED -> {
                    disaster.receive();

                    String title = disaster.getDisasterType().getValue() + " 발생 !!!";
                    String body = disaster.getDisasterType().getValue() + "\n" +
                            "내용 : " + disaster.getDetails() + "\n" +
                            "처리자 : " + handler.getAccountId();

                    // User/Android, Admin/Android, Admin/Web 전체에 발송
                    pushMessageService.sendToRolesAndPlatforms(
                            Arrays.asList(UserRole.USER, UserRole.ADMIN),
                            Arrays.asList(DevicePlatform.ANDROID, DevicePlatform.WEB),
                            title,
                            body
                    );
                }
                case COMPLETED -> disaster.complete();
                case CANCELLED -> disaster.cancel();
                default -> throw new CustomException(HttpStatus.BAD_REQUEST, "Disaster 상태 변경은 접수, 종료, 취소만 가능합니다.");
            }
        }catch(Exception e){
            log.error(e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
        }

        return DisasterResDto.from(disaster);
    }

    public Page<DisasterResDto> searchDisasters(Integer mapId, EventStatus eventStatus, Pageable pageable) {
        if (eventStatus != null && !eventStatus.getValue().isEmpty()) {
            // keyword가 있는 경우 Status 별로 검색
            try{
                return disasterRepository.findAllByMapIdAndEventStatus(mapId, eventStatus, pageable).map(DisasterResDto::from);
            }catch(Exception e){
                log.error(e.getMessage());
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
            }
        } else {
            // keyword가 없는 경우 모든 Disasters를 페이징하여 반환
            try{
                return disasterRepository.findAllByMapId(mapId, pageable).map(DisasterResDto::from);
            }catch(Exception e){
                log.error(e.getMessage());
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
            }
        }
    }
}
