package com.HotelManager.DTO;

import com.HotelManager.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String photo;
    private String name;
    private String phone;
    private List<String> roles;

    public UserDTO() {

    }

    public UserDTO(Long id, String username, String email, String name, String phone) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.phone = phone;
    }

    public UserDTO(Long id, String username, String email, String name, String phone, List<String> roles) {
    }
}
