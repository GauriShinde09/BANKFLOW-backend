package com.app.repo;

import com.app.entity.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanTypeRepository extends JpaRepository<LoanType, Long> {

    Optional<LoanType> findByName(String name);

}