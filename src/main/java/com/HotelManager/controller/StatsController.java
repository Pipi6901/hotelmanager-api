package com.HotelManager.controller;

import com.HotelManager.DTO.statsDTO.ComparisonByRoomCategoryDTO;
import com.HotelManager.DTO.statsDTO.DurationDistributionDTO;
import com.HotelManager.DTO.statsDTO.LengthOfStayAnalysisDTO;
import com.HotelManager.DTO.statsDTO.PopProfitRatingDTO;
import com.HotelManager.DTO.statsDTO.RoomStatsDTO;
import com.HotelManager.entity.Reservation;
import com.HotelManager.entity.enums.ReservationStatus;
import com.HotelManager.entity.enums.Type;
import com.HotelManager.repo.ReservationRepository;
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


    @GetMapping("/pop-profit-rating")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PopProfitRatingDTO> getPopProfitRating() {
        List<Reservation> reservations = reservationRepository.findAll();

        int totalIncome = reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.DONE)
                .mapToInt(reservation -> reservation.getDays() * reservation.getRoom().getPrice())
                .sum();

        List<RoomStatsDTO> topRoomsByIncome = reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.DONE)
                .collect(Collectors.groupingBy(Reservation::getRoom))
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
                .collect(Collectors.groupingBy(Reservation::getRoom))
                .entrySet().stream()
                .map(entry -> new RoomStatsDTO(
                        entry.getKey().getName(),
                        entry.getValue().size()))
                .sorted(Comparator.comparingInt(RoomStatsDTO::getIncome).reversed())
                .limit(5)
                .collect(Collectors.toList());

        PopProfitRatingDTO popProfitRatingDTO = new PopProfitRatingDTO();
        popProfitRatingDTO.setTotalIncome(totalIncome);
        popProfitRatingDTO.setTopRoomsByIncome(topRoomsByIncome);
        popProfitRatingDTO.setTopRoomsByBookings(topRoomsByBookings);

        return ResponseEntity.ok(popProfitRatingDTO);
    }

    @GetMapping("/length-of-stay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LengthOfStayAnalysisDTO>> getLenghtOfStay() {
        List<Reservation> reservations = reservationRepository.findAll();

        List<LengthOfStayAnalysisDTO> stats = reservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getRoom().getName(),            // имя комнаты
                        Collectors.summarizingInt(Reservation::getDays) // статистика по "days"
                ))
                .entrySet().stream()
                .map(entry -> {
                    LengthOfStayAnalysisDTO dto = new LengthOfStayAnalysisDTO();
                    dto.setRoomName(entry.getKey());
                    dto.setTotal((int) entry.getValue().getSum());      // общее количество дней
                    dto.setIncome(entry.getValue().getAverage()); // среднее количество дней
                    return dto;
                })
                .collect(Collectors.toList());


        return ResponseEntity.ok(stats);
    }

    @GetMapping("duration-distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DurationDistributionDTO> getDurationDistribution() {

        List<Reservation> reservations = reservationRepository.findAll();

        int OneThree = (int) reservations.stream()
                .filter(reservation -> reservation.getDays() <= 3)
                .count();

        int FourSix = (int) reservations.stream()
                .filter(reservation -> reservation.getDays() >= 4 && reservation.getDays() <= 6)
                .count();

        int SevenNine = (int) reservations.stream()
                .filter(reservation -> reservation.getDays() >= 7 && reservation.getDays() <= 9)
                .count();

        int TenAndMore = (int) reservations.stream()
                .filter(reservation -> reservation.getDays() >= 10)
                .count();

        DurationDistributionDTO durationDistributionDTO = new DurationDistributionDTO();
        durationDistributionDTO.setOneThree(OneThree);
        durationDistributionDTO.setFourSix(FourSix);
        durationDistributionDTO.setSevenNine(SevenNine);
        durationDistributionDTO.setTenAndMore(TenAndMore);

        return ResponseEntity.ok(durationDistributionDTO);
    }

    @GetMapping("comparison-by-room-category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComparisonByRoomCategoryDTO> getComparisonByRoomCategory() {
        List<Reservation> reservations = reservationRepository.findAll();

        double averageStandardDuration = reservations.stream()
                .filter(reservation -> reservation.getType().equals(Type.STANDARD))
                .mapToInt(Reservation::getDays)
                .average()
                .orElse(0.0);

        double averageEconomyDuration = reservations.stream()
                .filter(reservation -> reservation.getType().equals(Type.ECONOMY))
                .mapToInt(Reservation::getDays)
                .average()
                .orElse(0.0);

        double averageVipDuration = reservations.stream()
                .filter(reservation -> reservation.getType().equals(Type.VIP))
                .mapToInt(Reservation::getDays)
                .average()
                .orElse(0.0);

        ComparisonByRoomCategoryDTO comparisonByRoomCategoryDTO = new ComparisonByRoomCategoryDTO();
        comparisonByRoomCategoryDTO.setStandard(averageStandardDuration);
        comparisonByRoomCategoryDTO.setEconomy(averageEconomyDuration);
        comparisonByRoomCategoryDTO.setVip(averageVipDuration);

        return ResponseEntity.ok(comparisonByRoomCategoryDTO);
    }
}