package com.fortebank.fraud.customer.repository;

import com.fortebank.fraud.customer.entity.CustomerBehaviorPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerBehaviorPatternRepository extends JpaRepository<CustomerBehaviorPattern, Long> {
    
    Optional<CustomerBehaviorPattern> findByCustomerIdAndTransDate(String customerId, LocalDate transDate);
    
    List<CustomerBehaviorPattern> findByCustomerId(String customerId);
    
    @Query("SELECT cbp FROM CustomerBehaviorPattern cbp WHERE cbp.customerId = :customerId " +
           "ORDER BY cbp.transDate DESC LIMIT 1")
    Optional<CustomerBehaviorPattern> findLatestByCustomerId(@Param("customerId") String customerId);
}