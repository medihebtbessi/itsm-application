package itsm.itsm_backend.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserResponse {

    private Integer id;
    private String firstname;
    private String lastname;
    private String fullName;
    private String email;
    private LocalDate dateOfBirth;
    private Role role;
    private Group group;
    private boolean enable;


    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .fullName(user.fullName())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .role(user.getRole())
                .group(user.getGroup())
                .enable(user.isEnable())
                .build();
    }
}