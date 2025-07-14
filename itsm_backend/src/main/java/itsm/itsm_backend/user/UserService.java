package itsm.itsm_backend.user;

import itsm.itsm_backend.ticket.jpa.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    public List<UserResponse> getUsers() {
        UserResponse user=getUserInfo();
        return userRepository.findAllUsersExceptSelf(user.getId()).stream().map(UserResponse::fromEntity).collect(Collectors.toList());
    }

    public UserResponse getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            String email = null;
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                email = principal.toString();
            }

            if (email != null) {
                User user= userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                return UserResponse.fromEntity(user);
            }
        }

        return null;
    }

    public UserResponse getUserInfo(String email) {
        User user=userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("User not found"));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public void deleteUser(String email) {
        userRepository.deleteUserByEmail(email);
    }

    public  Integer updateUser(String email, User user) {
        User user1 = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("User not found"));
        user1.setEmail(user.getEmail());
        user1.setPassword(user.getPassword());
        user1.setFirstname(user.getFirstname());
        user1.setLastname(user.getLastname());
        user1.setGroup(user.getGroup());
        user1.setRole(user.getRole());
       return userRepository.save(user1).getId();
    }

}
