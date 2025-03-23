package com.HotelManager.controller;

import com.HotelManager.DTO.StatsDTO;
import com.HotelManager.DTO.RoomStatsDTO;
import com.HotelManager.entity.Reservation;
import com.HotelManager.entity.Room;
import com.HotelManager.entity.enums.ReservationStatus;
import com.HotelManager.repo.ReservationRepository;
import com.HotelManager.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StatsDTO> getStatistics() {
        List<Reservation> reservations = reservationRepository.findAll();

        int totalIncome = reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.DONE)
                .mapToInt(reservation -> reservation.getDays() * reservation.getRoom().getPrice())
                .sum();

        List<RoomStatsDTO> topRoomsByIncome = reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.DONE)
                .collect(Collectors.groupingBy(reservation -> reservation.getRoom()))
                .entrySet().stream()
                .map(entry -> new RoomStatsDTO(
                        entry.getKey().getName(),
                        entry.getValue().stream()
                                .mapToInt(reservation -> reservation.getDays() * reservation.getRoom().getPrice())
                                .sum()))
                .sorted(Comparator.comparingInt(RoomStatsDTO::getIncome).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<RoomStatsDTO> topRoomsByBookings = reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.DONE)
                .collect(Collectors.groupingBy(reservation -> reservation.getRoom()))
                .entrySet().stream()
                .map(entry -> new RoomStatsDTO(
                        entry.getKey().getName(),
                        entry.getValue().size()))
                .sorted(Comparator.comparingInt(RoomStatsDTO::getIncome).reversed())
                .limit(5)
                .collect(Collectors.toList());

        StatsDTO statsDTO = new StatsDTO();
        statsDTO.setTotalIncome(totalIncome);
        statsDTO.setTopRoomsByIncome(topRoomsByIncome);
        statsDTO.setTopRoomsByBookings(topRoomsByBookings);

        return ResponseEntity.ok(statsDTO);
    }
}