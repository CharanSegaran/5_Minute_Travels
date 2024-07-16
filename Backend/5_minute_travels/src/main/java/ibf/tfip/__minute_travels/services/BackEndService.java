package ibf.tfip.__minute_travels.services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.amadeus.exceptions.ResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.*;

import ibf.tfip.__minute_travels.entities.User;
import ibf.tfip.__minute_travels.repositories.AmadeusRepository;
import ibf.tfip.__minute_travels.repositories.BackEndRepository;
import ibf.tfip.__minute_travels.repositories.StandfordNLPRepository;

@Service
public class BackEndService {
    
    @Autowired
    BackEndRepository backEndRepository;

    @Autowired
    StandfordNLPRepository standfordNLPRepository;

    @Autowired
    AmadeusRepository amadeusRepository;

    public String inputFromGPT(String message, String session_id){
        return backEndRepository.inputFromGPT(message, session_id);
    }

    public String engageAmadeus(String gptMessage) throws ResponseException, JsonProcessingException{
        String proccessedGPTMessage = backEndRepository.processGPTMessage(gptMessage);
        System.out.println(proccessedGPTMessage);
        if(proccessedGPTMessage.equals("Search Flights Offers")){
            Map<String,String> queryToAmadeus = standfordNLPRepository.extractRequestDetails(gptMessage);
            System.out.println(queryToAmadeus);
            JSONObject jsonObject = new JSONObject();
            JSONArray flightArray = new JSONArray();
            try{
                for(int i=0; i<5;i++){
                    String responseBody = amadeusRepository.flightFinalPrice(queryToAmadeus, i).getResponse().getBody();
                    JSONObject flightJson = new JSONObject(responseBody);
                    flightArray.put(flightJson);
                }
                return jsonObject.put("flights", flightArray).toString();
            }catch(Exception e){

            }
            return "Sorry we could not find any flights for your request. Try another request instead";
        }
        return gptMessage;   
    }

    public String createAndSaveFlightOrder(Object passengerDetails, Object flightOffer, String username) throws JsonProcessingException, ResponseException, JSONException{
        String createOrderResponse = amadeusRepository.createOrder(passengerDetails, flightOffer);
        backEndRepository.saveConfirmedFlight(username, createOrderResponse);
        return createOrderResponse;
    }

    public List<Map<String,Object>> findBookingsByUsername(String username){
        return backEndRepository.findBookingsByUsername(username);
    }

    public void deleteBooking(String username, String id){
        backEndRepository.deleteBooking(username, id);
    }

    public String getUsernameFromToken(){
        return backEndRepository.getUsernameFromToken();
    }
}

