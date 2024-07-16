package ibf.tfip.__minute_travels.repositories;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;

import ibf.tfip.__minute_travels.constants.Constants;
import ibf.tfip.__minute_travels.entities.User;
import ibf.tfip.__minute_travels.models.Passenger;
import ibf.tfip.__minute_travels.services.JavaSmtpGmailSenderService;

@Repository
public class BackEndRepository {

    @Autowired
    AmadeusRepository amadeusRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    JavaSmtpGmailSenderService javaSmtpGmailSenderService;
    
    public String inputFromGPT(String message, String session_id){
        try{
            File tempScript = extractResource("chatgpt.py");

            tempScript.setExecutable(true);
            ProcessBuilder pb = new ProcessBuilder("python3", tempScript.getAbsolutePath(),message,session_id);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;

            while((line = reader.readLine()) != null){
                result.append(line);
                System.out.println(result);
            }

            int exitCode = process.waitFor();
            if(exitCode == 0){
                return result.toString();
            }else return "Cannot read Python script";
        }catch(Exception e){
            return e.getMessage();
        }
    }

    private File extractResource(String resourceName) throws Exception {

        InputStream resourceStream = getClass().getResourceAsStream("/" + resourceName);
        if (resourceStream == null) {
            throw new RuntimeException("Resource not found: " + resourceName);
        }

        File tempFile = File.createTempFile(resourceName, null);

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = resourceStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }

    public String processGPTMessage(String gptMessage){
        if(gptMessage.contains("find you flights for")){
            return "Search Flights Offers";
        }
        return "";
    }

    public String findIATACodeFromCity(String city){
        System.out.println("City" + city);
        final SqlRowSet rs =jdbcTemplate.queryForRowSet(Constants.findIATAByCity, "%"+city+"%");
        if(rs.next()){
            System.out.println(rs.getString("IATA")); 
            return (rs.getString("IATA"));
        } 
        return "";

    }
    public String findIATACodeFromAIrport(String airportName){
        System.out.println("Airport" + airportName);
        final SqlRowSet rs = jdbcTemplate.queryForRowSet(Constants.findIATAByAirport, "%"+airportName+"%");
        if(rs.next()){
            System.out.println(rs.getString("IATA")); 
            return (rs.getString("IATA"));
        } 
        return "";
    }

    public String findIATACodeFromCountry(String country){
        System.out.println("Country:" + country);
        final SqlRowSet rs = jdbcTemplate.queryForRowSet(Constants.findIATAByCountry, country);
        if(rs.next()){
            System.out.println(rs.getString("IATA")); 
            return (rs.getString("IATA"));
        } 
        return "";
    }


    public String convertDate(String unformattedDate){
        DateTimeFormatter inputFormatter = new DateTimeFormatterBuilder()
                                            .parseCaseSensitive()
                                            .appendOptional(DateTimeFormatter.ofPattern("EEEE, d['st']['nd']['rd']['th'] MMMM"))
                                            .appendOptional(DateTimeFormatter.ofPattern("EEEE, MMMM d['st']['nd']['rd']['th']"))
                                            .toFormatter(Locale.ENGLISH);

        // DateTimeFormatter.ofPattern("EEEE, d['st']['nd']['rd']['th'] MMMM",Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        MonthDay date = MonthDay.parse(unformattedDate, inputFormatter);
        LocalDate parsedDate = date.atYear(2024);
        System.out.println(parsedDate.format(outputFormatter));
       return parsedDate.format(outputFormatter);
    }

    public void saveConfirmedFlight(String username, String flightOrder){
        try{
            JSONObject confirmedFlightOrder = new JSONObject(flightOrder);
            confirmedFlightOrder.put("order_status", "active");
            Map<String,Object> flightOrderMap = confirmedFlightOrder.toMap();
            Query query = Query.query(Criteria.where("username").is(username.toString()));
            
            if(mongoTemplate.find(query, Passenger.class, "passengerDetails").isEmpty()){
                Passenger passenger = new Passenger();
                passenger.setUsername(username.toString());
                passenger.setFlightOrders(List.of(flightOrderMap));
                mongoTemplate.save(passenger);
                System.out.println("Inserted new collection for username: "+ username.toString());
            }else{
                Update update = new Update().addToSet("flightOrders", flightOrderMap);
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Passenger.class);
                System.out.printf("Documents updated: %d\n",updateResult.getModifiedCount());
            }
        }catch(Exception e){
            System.out.println(e.getLocalizedMessage());
        }

        javaSmtpGmailSenderService.sendEmail(username, "Your flight has been booked! Its time to get outta here", flightOrder);

    }

    public List<Map<String,Object>> findBookingsByUsername(String username){
        Query query = Query.query(Criteria.where("username").is(username));
        Passenger passenger = mongoTemplate.findOne(query, Passenger.class,
                                                              "passengerDetails");
        List<Map<String,Object>> flightBookings = new LinkedList<>();
        if(!passenger.getUsername().isBlank()){    
            for(Map<String,Object> flightBooking:passenger.getFlightOrders()){
                if(flightBooking.get("order_status").equals("active"))flightBookings.add(flightBooking);
            }
        }
        return flightBookings;
    }

    public void deleteBooking(String username, String id){
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username).and("flightOrders.data.id").is(id));
        Update update = new Update().set("flightOrders.$.order_status", "deleted");
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, "passengerDetails");
        System.out.printf("Documents updated: %s", updateResult.getUpsertedId());
    }

    public String getUsernameFromToken(){
        //get user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        String username = currentUser.getEmail();
        System.out.println(username);
        return username;
    }

}
