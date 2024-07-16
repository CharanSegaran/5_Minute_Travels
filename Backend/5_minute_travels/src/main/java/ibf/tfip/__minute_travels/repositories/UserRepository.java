package ibf.tfip.__minute_travels.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ibf.tfip.__minute_travels.entities.User;

@Repository
public interface UserRepository  extends CrudRepository<User, Integer> {
    Optional<User> findByEmail(String email);
} 
