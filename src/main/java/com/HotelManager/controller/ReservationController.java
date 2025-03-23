package com.HotelManager.controller;


import com.HotelManager.DTO.ReservationResponseDTO;
import com.HotelManager.DTO.RoomDTO;
import com.HotelManager.entity.Receipt;
import com.HotelManager.entity.Reservation;
import com.HotelManager.entity.Room;
import com.HotelManager.entity.User;
import com.HotelManager.entity.enums.ReservationStatus;
import com.HotelManager.exception.ErrorResponse;
import com.HotelManager.repo.ReceiptRepository;
import com.HotelManager.repo.ReservationRepository;
import com.HotelManager.repo.RoomRepository;
import com.HotelManager.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ReceiptRepository receiptRepository;


    @GetMapping
    public ResponseEntity<?> getReservation() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(currentUser)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        List<Reservation> reservations;

        if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_MANAGER")) {
            reservations = reservationRepository.findAll();
        } else if (roles.contains("ROLE_USER")) {
            reservations = reservationRepository.findByOwner(currentUser);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нет доступа к заявкам");
        }

        List<ReservationResponseDTO> reservationDTOs = reservations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reservationDTOs);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/confirm") // http://localhost:8080/reservation/1/confirm
    public ResponseEntity<?> confirmReservation(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

        reservation.setStatus(ReservationStatus.DONE);
        reservationRepository.save(reservation);

        return ResponseEntity.ok(convertToDTO(reservation));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reject") // http://localhost:8080/reservation/1/reject
    public ResponseEntity<?> rejectReservation(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

        if (reservation.getStatus() == ReservationStatus.REJECT) {
            return ResponseEntity.badRequest().body("Бронирование уже отменено.");
        }

        User user = userRepository.findByUsername(reservation.getOwner())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        int totalCost = reservation.getPrice() * reservation.getDays();
        user.setBalance(user.getBalance() + totalCost);
        userRepository.save(user);

        Room room = reservation.getRoom();
        if (room != null && !room.isFree()) {
            room.setFree(true);
            roomRepository.save(room);
        }

        reservation.setStatus(ReservationStatus.REJECT);
        reservationRepository.save(reservation);

        Receipt receipt = receiptRepository.findByReservationId(reservation.getId())
                .orElseThrow(() -> new RuntimeException("Чек не найден"));

        receipt.setStatus("Cancelled");
        receiptRepository.save(receipt);

        return ResponseEntity.ok(convertToDTO(reservation));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/{id}/cancel") // http://localhost:8080/reservation/1/cancel
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

        if (!reservation.getOwner().equals(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы не можете отменить это бронирование");
        }

        User user = userRepository.findByUsername(reservation.getOwner())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        int totalCost = reservation.getPrice() * reservation.getDays();
        user.setBalance(user.getBalance() + totalCost);
        userRepository.save(user);

        Room room = reservation.getRoom();
        if (room != null && !room.isFree()) {
            room.setFree(true);
            roomRepository.save(room);
        }

        reservation.setStatus(ReservationStatus.REJECT);
        reservationRepository.save(reservation);

        Receipt receipt = receiptRepository.findByReservationId(reservation.getId())
                .orElseThrow(() -> new RuntimeException("Чек не найден"));

        receipt.setStatus("Cancelled");
        receiptRepository.save(receipt);

        return ResponseEntity.ok(convertToDTO(reservation));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{id}/delete") // http://localhost:8080/reservation/1/delete
    public ResponseEntity<?> deleteReservation(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

        reservationRepository.deleteById(id);

        return ResponseEntity.ok(convertToDTO(reservation));
    }

    private ReservationResponseDTO convertToDTO(Reservation reservation) {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(reservation.getId());
        dto.setName(reservation.getName());
        dto.setPrice(reservation.getPrice() * reservation.getRoom().getDays());
        dto.setStatus(reservation.getStatus().name());
        dto.setOwner(reservation.getOwner());
        dto.setDays(reservation.getDays());
        dto.setType(reservation.getType());
        dto.setBeds(reservation.getBeds());
        dto.setNumber(reservation.getNumber());
        dto.setDescription(reservation.getDescription());
        dto.setFloor(reservation.getFloor());

        Room room = reservation.getRoom();
        if (room != null) {
            dto.setRoomId(room.getId());
            dto.setRoomPrice(room.getPrice());

            if (room.getPhoto() != null && !room.getPhoto().isEmpty()) {
                dto.setPhoto("http://localhost:8080/img/hotel/" + room.getPhoto());
            }
        }

        Receipt receipt = receiptRepository.findByReservationId(reservation.getId())
                .orElseThrow(() -> new RuntimeException("Чек не найден"));
        dto.setReceiptStatus(receipt.getStatus());

        return dto;
    }


}
