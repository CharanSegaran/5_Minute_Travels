package ibf.tfip.__minute_travels.constants;

public class Constants {
    public static final String findIATAByCity = """
            SELECT IATA FROM airports_only WHERE City LIKE ? AND Name NOT LIKE '%Base%' 
            ORDER BY Airport_ID ASC LIMIT 1
            """;
    public static final String findIATAByAirport = """
            SELECT IATA FROM airports_only WHERE Name LIKE ? AND Name NOT LIKE '%Base%'
            ORDER BY Airport_ID ASC LIMIT 1
            """;
    public static final String findIATAByCountry = """
                SELECT IATA FROM airports_only WHERE Country LIKE ? AND Name NOT LIKE '%Base%' 
                ORDER BY Airport_ID ASC LIMIT 1;
                """;
}
