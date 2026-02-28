package com.fifthdimension.digital_twin.test.repository;

import com.fifthdimension.digital_twin.test.model.TestModel;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestOpenSearchRepository extends ElasticsearchRepository<TestModel, String> {
    List<TestModel> getById(String id);
}
