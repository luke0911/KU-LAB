package com.fifthdimension.digital_twin.test.dto;

import com.fifthdimension.digital_twin.test.model.TestMySQLModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TestDto {
    private Long id;
    private String description;

    public TestMySQLModel toEntity(){
        return new TestMySQLModel(this.id, this.description);
    }

    public static TestDto fromEntity(TestMySQLModel entity){
        return new TestDto(entity.getId(), entity.getDescription());
    }
}
