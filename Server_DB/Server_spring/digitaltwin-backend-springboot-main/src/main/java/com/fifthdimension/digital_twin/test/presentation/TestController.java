package com.fifthdimension.digital_twin.test.presentation;

import com.fifthdimension.digital_twin.test.dto.TestDto;
import com.fifthdimension.digital_twin.test.model.TestModel;
import com.fifthdimension.digital_twin.test.service.TestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping("/opensearch")
    public TestModel saveToOpenSearch(@RequestBody TestModel testModel) {
        return testService.saveOpenSearchTestModel(testModel);
    }

    @GetMapping("/opensearch/{id}")
    public List<TestModel> searchOpenSearch(@PathVariable String id){
        return testService.getOpenSearchTestModel(id);
    }

    @PostMapping("/redis")
    public String saveToRedis(@RequestBody TestModel testModel) {
        try{
            testService.saveRedisTestModel(testModel);
        }catch (Exception e){
            return e.getMessage();
        }
        return "success";
    }

    @GetMapping("/redis/{id}")
    public TestModel getRedisTestModel(@PathVariable String id){
        return testService.getRedisTestModel(id);
    }

    @PostMapping("/mysql")
    public TestDto saveToMysql(
            @RequestHeader(name = "X-USER-ID") Long userId,
            @RequestBody TestDto testDto) {
        return testService.saveMySQLTestModel(testDto);
    }

    @GetMapping("/mysql")
    public List<TestDto> getAllMysqlTestModel(){
        return testService.getAllMySQLTestModel();
    }
}
