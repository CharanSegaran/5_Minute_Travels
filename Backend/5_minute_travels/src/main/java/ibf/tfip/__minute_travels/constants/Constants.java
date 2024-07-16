package ibf.tfip.__minute_travels.constants;

public class Constants {
    public static final String findIATAByCity = """
            SELECT IATA FROM airports_only WHERE City = ? AND IATA != '\\N' ORDER BY Airport_ID ASC LIMIT 1
            """;
    public static final String findIATAByAirport = """
            SELECT IATA FROM airports_only WHERE Name LIKE ?
            """;
}
