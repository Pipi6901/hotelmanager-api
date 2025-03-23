package com.HotelManager.repo;

import com.HotelManager.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByReservationId(Long reservationId);

    void deleteByReservationId(Long reservationId);
}