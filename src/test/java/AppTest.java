import api.FakeInterceptor;
import api.ForecastServiceImpl;
import model.Forecast;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import util.PathDate;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    String[] validCities = {"lon", "london", "dub", "dubai", "hel"};
    String[] invalidCities = {"Atlantis", "Sokovia", "Wakanda", "123"};

    Random r=new Random();
    String validCity;
    private static ForecastServiceImpl service ;
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    private static final Logger LOGGER = LoggerFactory.getLogger(AppTest.class);
    static MockWebServer server = new MockWebServer();



    @BeforeAll
    public static void setup() throws IOException {

        LOGGER.info(() -> "Test Suite Setup STARTED");

        server.enqueue(new MockResponse().setBody("[{\"title\":\"MockWebServer\", \"location_type\":\"City\", \"woeid\":44418, \"latt_long\":\"51.506321,-0.12714\"}]"));
        server.start();

        final OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new FakeInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        service = new ForecastServiceImpl(retrofit);




        LOGGER.info(() -> "Test Suite Setup FINISHED");

    }

    @AfterAll
    public static void tearDown() throws IOException {

        server.shutdown();

    }


        @Test
    @DisplayName("Test1: Valid city and Forecast")
    void testValidCityForecastOutput() throws IOException {

        LOGGER.info(() -> "Test1 Started");

        try {

            // Given OK city string
            validCity =  validCities[r.nextInt(validCities.length)];

            // When I call weather api and get a response and format it as a string
            String responseString = service.getForecast(validCity,tomorrow);
            System.out.println(responseString);

            String [] actualForecast  = responseString.split("\\R");

            PathDate pathDate = new PathDate(tomorrow);

            Forecast expectedForecast  = new Forecast();
            expectedForecast.setId(5749047877959680L);
            expectedForecast.setWeatherState("Heavy SNOW");
            expectedForecast.setTemperature(6.265000000000001);
            expectedForecast.setWindSpeed(11.2);
            expectedForecast.setHumidity(80);


            // Then
            assertEquals(String.format("Weather on (%s) in Helsinki:", pathDate), actualForecast[0]);
            // AND
            assertEquals(expectedForecast.getWeatherState(), actualForecast[1]);
            // AND
            assertEquals(String.format("Temp: %.1f °C",expectedForecast.getTemperature() ) , actualForecast[2]);
            // AND
            assertEquals(String.format("Wind: %.1f mph",expectedForecast.getWindSpeed() ) , actualForecast[3]);
            // AND
            assertEquals(String.format("Humidity: %d%%",expectedForecast.getHumidity() ) , actualForecast[4]);

            LOGGER.info(() -> "Test1 Ended");


        } catch (Exception e) {
            LOGGER.error( () ->  String.format(e.getMessage()));
            System.exit(1);
        }
    }


    @Test
    @DisplayName("Test2: City name not valid")
    @Disabled
    void testInvalidCityForecastOutput() throws IOException {

        LOGGER.info(() -> "Test2 Started");

        try {

            // Given Invalid city name
            validCity =  invalidCities[r.nextInt(invalidCities.length)];

            // When I call weather api get a response
            String responseString = service.getForecast(validCity,tomorrow);
            //Next line shouldn't be reached
            System.out.println(responseString);


        } catch (Exception e) {
            // Then
            assertEquals(String.format("Can't find city id for '%s'", validCity), e.getMessage());
            LOGGER.info( () ->  String.format(e.getMessage()));
            LOGGER.info(() -> "Test2 Ended");

        }

    }

    @Test
    @DisplayName("Test3: Valid city name but no forecast")
    @Disabled
    void testValidCityNoForecast() throws IOException {

        LOGGER.info(() -> "Test3 Started");

        try {

            // Given valid city name but with bad forecast response
            validCity =  "hyderabad";

            // When I call weather api get a response
            String responseString = service.getForecast(validCity,tomorrow);
            System.out.println(responseString);

        } catch (Exception e) {
            // Then
            assertEquals(String.format("Can't get forecast for '%s'", validCity), e.getMessage());
            LOGGER.info( () ->  String.format(e.getMessage()));
            LOGGER.info(() -> "Test3 Ended");

        }

    }

    // Purpose: Verify basic functioning for the REST API Retrofit app
    // More tests can be added: Status codes, JSON Payload, Media Type,
    // other HTTP responses, headers in the response, Schema validation...

}