package com.fifthdimension.digital_twin.test.repository;

import com.fifthdimension.digital_twin.test.model.TestMySQLModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestMySQLRepository extends JpaRepository<TestMySQLModel, Long> {
}
