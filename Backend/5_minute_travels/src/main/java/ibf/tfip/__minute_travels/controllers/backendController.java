package ibf.tfip.__minute_travels.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amadeus.exceptions.ResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import ibf.tfip.__minute_travels.dtos.LoginResponse;
import ibf.tfip.__minute_travels.dtos.LoginUserDto;
import ibf.tfip.__minute_travels.dtos.RegisterUserDto;
import ibf.tfip.__minute_travels.entities.User;
import ibf.tfip.__minute_travels.repositories.StandfordNLPRepository;
import ibf.tfip.__minute_travels.services.AuthenticationService;
import ibf.tfip.__minute_travels.services.BackEndService;
import ibf.tfip.__minute_travels.services.JwtService;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.*;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping()
public class backendController {

    @Autowired
    BackEndService backEndService;
    
    @Autowired
    StandfordNLPRepository standfordNLPRepository;

    @PostMapping("api/NLP")
    public ResponseEntity<Map<String,Object>> getFlightOffers(@RequestBody Map<String,String> message) throws ResponseException,IOException{
        String query = message.get("message");
        String sessionId = message.get("sessionId");
        String gptResponse = backEndService.inputFromGPT(query, sessionId);
        String fromAmadeus = backEndService.engageAmadeus(gptResponse);
        standfordNLPRepository.getNERTag(query);
        Map<String,Object> response = new HashMap<>();
        response.put("gptResponse", gptResponse);
        response.put("fromAmadeus", fromAmadeus);
        response.put("method", "GET");
        return ResponseEntity.ok().body(response);
    }
    
    @PostMapping("/api/createOrder")
    public ResponseEntity<Map<String,Object>> postMethodName(@RequestBody Map<String,Object> message) 
                                                             throws JsonMappingException, JsonProcessingException, ResponseException, JSONException {
        
        
        String username = backEndService.getUsernameFromToken();
        //create the order,store in mongo and retrieve message
        String createdOrder = backEndService.createAndSaveFlightOrder(message.get("passengerDetails"), 
                                                         message.get("flightOffer"),
                                                         username);
        String gptResponse = "";
        if(createdOrder.contains("Sorry there was a problem booking your flight"))gptResponse = createdOrder;
        else gptResponse = "Your flight has been booked! Check the Bookings tab to view your booking";
        Map<String,Object> response = new HashMap<>();
        response.put("gptResponse", gptResponse);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/api/bookings")
    public ResponseEntity<Map<String,Object>> getBookings() {

        String username = backEndService.getUsernameFromToken();
        Map<String,Object> response = new HashMap<>();
        response.put("flightOrders", backEndService.findBookingsByUsername(username));
        response.put("Backend Mesage","Successfully retirieved flight bookings");
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/api/bookings/delete")
    public ResponseEntity<Map<String,Object>> deleteBooking(@RequestParam("id")String id){

        String username = backEndService.getUsernameFromToken();
        backEndService.deleteBooking(username, id);
        Map<String,Object> response = new HashMap<>();
        response.put("flightOrders", backEndService.findBookingsByUsername(username));
        response.put("Backend Mesage","Booking "+id+" is deleted");
        return ResponseEntity.ok().body(response);
    }

    @Autowired
    JwtService jwtService;

    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/api/auth/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        try{
            User authenticatedUser = authenticationService.authenticate(loginUserDto);
            String jwtToken = jwtService.generateToken(authenticatedUser);
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(jwtToken);
            loginResponse.setExpiresIn(jwtService.getExpirationTime());
            return ResponseEntity.ok(loginResponse);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatusCode.valueOf(401)).body(new LoginResponse());
        }
    }
    
    @Value("${openai.api.key}")
    private String apiKey;

    @GetMapping("/api/api-key")
    public String getApiKey() {
        return apiKey;
    }
}
