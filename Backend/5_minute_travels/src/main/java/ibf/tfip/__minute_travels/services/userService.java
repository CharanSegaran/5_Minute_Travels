package ibf.tfip.__minute_travels.services;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ibf.tfip.__minute_travels.entities.User;
import ibf.tfip.__minute_travels.repositories.UserRepository;

@Service
public class userService {
    @Autowired
    UserRepository userRepository;

    public List<User> getAllUsers(){
        List<User> allUsers = new LinkedList<>();
        userRepository.findAll().forEach(allUsers::add);
        return allUsers;
    }
}
