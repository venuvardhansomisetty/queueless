package com.queueless.repository;

import com.queueless.model.ServiceItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    List<ServiceItem> findByStaffId(Long staffId);

    // Same as findById, but it LOCKS the row until our transaction finishes.
    // If two students press "join" at the same moment, the second one waits for the first.
    // That is how we stop two people from getting the same token number.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ServiceItem s where s.id = :id")
    Optional<ServiceItem> findByIdForUpdate(@Param("id") Long id);
}
