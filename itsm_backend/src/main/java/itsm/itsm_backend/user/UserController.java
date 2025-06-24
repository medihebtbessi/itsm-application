package itsm.itsm_backend.user;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;


    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getUsers();
        return ResponseEntity.ok(users);
    }


    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse current = userService.getUserInfo();
        if (current == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(current);
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserResponse> getUser(@PathVariable("email") String email) {
        return ResponseEntity.ok(userService.getUserInfo(email));
    }

    @DeleteMapping("{email}")
    public void deleteUser(@PathVariable("email") String email) {
        userService.deleteUser(email);

    }

    @PutMapping("{email}")
    public ResponseEntity<Integer> updateUser(@PathVariable("email") String email, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(email, user));
    }
}
