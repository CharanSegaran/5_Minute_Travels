package ibf.tfip.__minute_travels.repositories;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.referencedata.Locations;
import com.amadeus.resources.FlightOfferSearch;
import com.amadeus.resources.FlightOrder;
import com.amadeus.resources.FlightPrice;
import com.amadeus.resources.Location;
import com.amadeus.resources.FlightOrder.Contact;
import com.amadeus.resources.FlightOrder.Document;
import com.amadeus.resources.FlightOrder.Document.DocumentType;
import com.amadeus.resources.FlightOrder.Name;
import com.amadeus.resources.FlightOrder.Phone;
import com.amadeus.resources.FlightOrder.Phone.DeviceType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.amadeus.resources.FlightOrder.Traveler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;



@Repository
public class AmadeusRepository {
    @Value("${amadeus.client_id}")
    private String key;

    @Value("${amadeus.client_secret}")
    private String access;


    public FlightOfferSearch[] flightOfferSearches(Map<String,String> query) throws ResponseException{

        Amadeus amadeus = Amadeus.builder(key,access).build();
        Params params = Params.with("originLocationCode", query.get("originLocationCode"))
        .and("destinationLocationCode", query.get("destinationLocationCode"))
        .and("departureDate", query.get("departureDate"))
        .and("currencyCode","SGD")
        .and("excludedAirlineCodes","6X,7X,8X");

        if(query.get("returnDate") != null)params.and("returnDate", query.get("returnDate"));
        if(query.get("max") == null)params.and("max", "5");
        if(query.get("adults") != null)params.and("adults", query.get("adults"));
        if(query.get("children") != null)params.and("children", query.get("children"));

        FlightOfferSearch[] flightOfferSearchs = amadeus.shopping.flightOffersSearch.get(params);
        return flightOfferSearchs;
    }

    public FlightPrice flightFinalPrice(Map<String,String> query, int i) throws ResponseException{
        Amadeus amadeus = Amadeus.builder(key,access).build();
        try{
            FlightOfferSearch[] flightOffers = flightOfferSearches(query);
        FlightPrice flightPricing = amadeus.shopping.flightOffersSearch.pricing.post(
            flightOffers[i],
            Params.with("include", "detailed-fare-rules")
              .and("forceClass", "false")
          );
            return flightPricing;
        }catch(Exception e){
            
        }
        return null;
        
    }

    public Location[] findAirportByCityName(String cityName) throws ResponseException{
        Amadeus amadeus = Amadeus.builder(key,access).build();
        Location[] locations = amadeus.referenceData.locations.get(Params.with("keyword", cityName).and("subType",Locations.AIRPORT));
        return locations;
    }

    public String createOrder(Object passengerDetails, Object flightOffer) throws ResponseException, JsonProcessingException, JSONException{
        Amadeus amadeus = Amadeus.builder(key,access).build();
        String response="";
        if(passengerDetails instanceof List){
            try{
                JSONArray passengerDetailsArray = new JSONArray((List<?>) passengerDetails);
                Traveler[] travellers = new Traveler[passengerDetailsArray.length()];
                for(int i=0;i<passengerDetailsArray.length();i++){
                    JSONObject passenger = passengerDetailsArray.getJSONObject(i);

                    Traveler traveler = new Traveler();
                    traveler.setId(passenger.getString("passengerId"));
                    traveler.setDateOfBirth(convertDateToISO8601(passenger.getJSONObject("dateOfBirth")));
                    traveler.setName(new Name(passenger.getString("firstName"),passenger.getString("lastName")));

                    Phone[] phone = new Phone[1];
                    phone[0] = new Phone();
                    phone[0].setCountryCallingCode(passenger.getString("countryCallingCode"));
                    phone[0].setNumber(String.valueOf(passenger.getInt("mobileNumber")));
                    phone[0].setDeviceType(DeviceType.MOBILE);
                    Contact contact = new Contact();
                    contact.setPhones(phone);
                    traveler.setContact(contact);

                    Document[] document = new Document[1];
                    document[0] = new Document();
                    document[0].setDocumentType(DocumentType.PASSPORT);
                    document[0].setNumber(String.valueOf(passenger.getInt("passportNumber")));
                    document[0].setExpiryDate(convertDateToISO8601(passenger.getJSONObject("passportExpiryDate")));
                    document[0].setIssuanceCountry(passenger.getString("passportIssuanceCountry"));
                    document[0].setNationality(passenger.getString( "nationality"));
                    document[0].setHolder(true);
                    traveler.setDocuments(document);

                    travellers[i] = traveler;
                    System.out.println(travellers[i]);
                }
            
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String flightOfferString = gson.toJson(flightOffer);
                
            
                JsonObject jsonObject = gson.fromJson(flightOfferString, JsonObject.class);
                
            
                JsonObject flightSelected = jsonObject.getAsJsonObject("data")
                                                    .getAsJsonArray("flightOffers")
                                                    .get(0)
                                                    .getAsJsonObject();

            
                JsonObject finalJsonObject = new JsonObject();
                JsonObject dataObject = new JsonObject();
                JsonArray flightOffersArray = new JsonArray();

                JsonArray travllerArray = new JsonArray();
                for(Traveler traveler:travellers){
                    JsonObject travlerJsonObject = gson.toJsonTree(traveler).getAsJsonObject();
                    travllerArray.add(travlerJsonObject);
                }

                flightOffersArray.add(flightSelected);
                dataObject.addProperty("type", "flight-order");
                dataObject.add("flightOffers", flightOffersArray);
                dataObject.add("travelers", travllerArray);
                finalJsonObject.add("data", dataObject);
                FlightOrder order = amadeus.booking.flightOrders.post(finalJsonObject);

                response = order.getResponse().getBody();
            }catch(Exception e){
                System.out.println(e.getLocalizedMessage());
                response = "Sorry there was a problem booking your flight. Please contact support for help";
            }    
        }
        return response;
    }

    public String convertDateToISO8601(JSONObject dateObject){
        int year = dateObject.getInt("year");
        int month = dateObject.getInt("month");
        int day = dateObject.getInt("day");
        LocalDate localDate = LocalDate.of(year, month, day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return localDate.format(formatter);
   }

}