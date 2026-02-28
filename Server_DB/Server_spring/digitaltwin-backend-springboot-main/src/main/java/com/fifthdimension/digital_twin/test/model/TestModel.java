package com.fifthdimension.digital_twin.test.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "testmodel")
@AllArgsConstructor
public class TestModel {

    @Id
    private String id;

    private String description;

}
