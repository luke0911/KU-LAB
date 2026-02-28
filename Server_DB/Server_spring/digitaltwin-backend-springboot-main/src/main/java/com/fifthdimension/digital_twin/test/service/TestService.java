package com.fifthdimension.digital_twin.test.service;

import com.fifthdimension.digital_twin.test.dto.TestDto;
import com.fifthdimension.digital_twin.test.model.TestModel;
import com.fifthdimension.digital_twin.test.model.TestMySQLModel;
import com.fifthdimension.digital_twin.test.repository.TestMySQLRepository;
import com.fifthdimension.digital_twin.test.repository.TestOpenSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestService {

    private final TestOpenSearchRepository openSearchRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final TestMySQLRepository mySQLRepository;

    // OpenSearch 연결 테스트용
    public TestModel saveOpenSearchTestModel(TestModel testModel) {
        return openSearchRepository.save(testModel);
    }

    public List<TestModel> getOpenSearchTestModel(String id) {
        return openSearchRepository.getById(id);
    }

    // Redis 연결 테스트용
    public void saveRedisTestModel(TestModel testModel) {
        redisTemplate.opsForValue().set(testModel.getId(), testModel.getDescription(), 600L, TimeUnit.SECONDS);
    }

    public TestModel getRedisTestModel(String id) {
        String description = redisTemplate.opsForValue().get(id);
        return new TestModel(id, description);
    }

    // MySQL 연결 테스트용
    public TestDto saveMySQLTestModel(TestDto testDto) {
        return TestDto.fromEntity(mySQLRepository.save(testDto.toEntity()));
    }

    public List<TestDto> getAllMySQLTestModel() {
        return mySQLRepository.findAll().stream().map(TestDto::fromEntity).collect(Collectors.toList());
    }
}
