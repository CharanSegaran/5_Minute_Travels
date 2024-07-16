package ibf.tfip.__minute_travels.models;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "passengerDetails")
@Data
@NoArgsConstructor
public class Passenger {
    @Id
    private String id;
    private String username;
    @Field("flightOrders")
    private List<Map<String,Object>> flightOrders;
}
