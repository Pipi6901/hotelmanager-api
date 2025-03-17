package com.HotelManager.controller;


import com.HotelManager.DTO.RoomDTO;
import com.HotelManager.entity.Reservation;
import com.HotelManager.entity.Room;
import com.HotelManager.entity.User;
import com.HotelManager.repo.ReservationRepository;
import com.HotelManager.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final UserRepository userRepository;


    @GetMapping
    public ResponseEntity<?> getReservation(){

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(currentUser)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        List<Reservation> reservations;

        if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_MANAGER")){
            reservations = reservationRepository.findAll();

            List<RoomDTO> roomDTOs = reservations.stream().map(reservation -> {
                RoomDTO roomDTO = new RoomDTO();
                roomDTO.setId(reservation.getId());
                roomDTO.setName(reservation.getName());
                roomDTO.setPrice(reservation.getRoom().getPrice());
                roomDTO.setFree(reservation.getRoom().isFree());
                roomDTO.setDays(reservation.getRoom().getDays());
                roomDTO.setType(reservation.getRoom().getType());
                roomDTO.setBeds(reservation.getRoom().getBeds());
                roomDTO.setNumber(reservation.getRoom().getNumber());
                roomDTO.setDescription(reservation.getRoom().getDescription());
                roomDTO.setFloor(reservation.getRoom().getFloor());
                roomDTO.setComments(reservation.getRoom().getComments());

                Room room = reservation.getRoom();

                if (room.getPhoto() != null && !room.getPhoto().isEmpty()) {
                    roomDTO.setPhoto("http://localhost:8080/img/hotel/" + room.getPhoto());
                }

                return roomDTO;
            }).toList();

            return ResponseEntity.ok(roomDTOs);
        } else if (roles.contains("ROLE_USER")){
            reservations = reservationRepository.findByOwner(currentUser);

            List<RoomDTO> roomDTOs = reservations.stream().map(reservation -> {
                RoomDTO roomDTO = new RoomDTO();
                roomDTO.setId(reservation.getId());
                roomDTO.setName(reservation.getName());
                roomDTO.setPrice(reservation.getRoom().getPrice());
                roomDTO.setFree(reservation.getRoom().isFree());
                roomDTO.setDays(reservation.getRoom().getDays());
                roomDTO.setType(reservation.getRoom().getType());
                roomDTO.setBeds(reservation.getRoom().getBeds());
                roomDTO.setNumber(reservation.getRoom().getNumber());
                roomDTO.setDescription(reservation.getRoom().getDescription());
                roomDTO.setFloor(reservation.getRoom().getFloor());
                roomDTO.setComments(reservation.getRoom().getComments());

                Room room = reservation.getRoom();

                if (room.getPhoto() != null && !room.getPhoto().isEmpty()) {
                    roomDTO.setPhoto("http://localhost:8080/img/hotel/" + room.getPhoto());
                }

                return roomDTO;
            }).toList();

            return ResponseEntity.ok(roomDTOs);
        } else
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нет доступа к заявкам");

    }






}
