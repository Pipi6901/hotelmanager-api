package com.HotelManager.DTO;

import com.HotelManager.entity.Room;
import lombok.Data;

@Data
public class ReservationDTO {

    private Long id;
    private String name;
    private RoomDTO room;
    private String owner;
    private String photo;

}
