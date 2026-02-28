package com.fifthdimension.digital_twin.test.model;

import com.fifthdimension.digital_twin.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "TestTable")
@AllArgsConstructor
@NoArgsConstructor
public class TestMySQLModel extends BaseEntity {

    @Id
    private Long id;

    private String description;
}
