package ibf.tfip.__minute_travels.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ibf.tfip.__minute_travels.entities.User;
import ibf.tfip.__minute_travels.services.userService;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping(path = "/users")
public class userController {
    @Autowired
    userService userService;

    @GetMapping("/user")
    public ResponseEntity<User> getUserByToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(currentUser);
    }
    
    @GetMapping("/allUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
}
