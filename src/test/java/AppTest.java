import api.FakeInterceptor;
import api.ForecastServiceImpl;
import okhttp3.OkHttpClient;
import org.junit.After;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

    ;



//
    //private MockWebServer server = new MockWebServer();

    @BeforeAll
    public static void setup() {


        LOGGER.info(() -> "Before Test Setup");


        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new FakeInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.metaweather.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        service = new ForecastServiceImpl(retrofit);

    }

    @After
    public void teardown() throws IOException {
        //server.shutdown();
    }


    @Test
    void testValidCityForecastOutput() throws IOException {

        LOGGER.info(() -> "testValidCityForecastOutput Started");

        try {

            // Given OK city string
            validCity =  validCities[r.nextInt(validCities.length)];

            // When I call weather api and get a response
            String responseSting = service.getForecast(validCity,tomorrow);
            System.out.println(responseSting);

            String [] splits  = responseSting.split("\\R");



            // Then
            PathDate pathDate = new PathDate(tomorrow);
            assertEquals(String.format("Weather on (%s) in Helsinki:", pathDate), splits[0]);
            // AND
            assertEquals("Heavy SNOW", splits[1]);
            // AND
            assertEquals("Temp: 6.3 °C", splits[2]);
            // AND
            assertEquals("Wind: 11.2 mph", splits[3]);
            // AND
            assertEquals("Humidity: 80%", splits[4]);

            LOGGER.info(() -> "Test Ended");


        } catch (Exception e) {
            LOGGER.error( () ->  String.format("Retrofit failed with error: %s", e.getMessage()));
            System.exit(1);
        }


    }



    @Test
    void testInvalidCityForecastOutput() throws IOException {

        LOGGER.info(() -> "Test testInvalidCityForecastOutput Started");

        try {

            // Given Invalid city name
            validCity =  invalidCities[r.nextInt(invalidCities.length)];

            // When I call weather api get a response
            String responseSting = service.getForecast(validCity,tomorrow);
            System.out.println(responseSting);



        } catch (Exception e) {
            // Then
            assertEquals(String.format("Can't find city id for '%s'", validCity), e.getMessage());
            LOGGER.error( () ->  String.format("Retrofit failed with error: %s - Will exit", e.getMessage()));
            LOGGER.info(() -> "Test testInvalidCityForecastOutput Ended");


            // commented out to prevent Exit during dev / testing
            //System.exit(1);
        }



    }

    @Test
    void testValidCityNoForecast() throws IOException {

        LOGGER.info(() -> "Test testValidCityNoForecast Started");

        try {


            // Given valid city name but with bad forecast response
            validCity =  "hyderabad";

            // When I call weather api get a response
            String responseSting = service.getForecast(validCity,tomorrow);
            System.out.println(responseSting);



        } catch (Exception e) {
            // Then
            assertEquals(String.format("Can't get forecast for '%s'", validCity), e.getMessage());
            LOGGER.error( () ->  String.format("Retrofit failed with error: %s - Will exit", e.getMessage()));
            LOGGER.info(() -> "Test testValidCityNoForecast Ended");

            // commented out to prevent Exit during dev / testing
            //System.exit(1);
        }



    }

    //Purpoose: Verify basic functioning for the REST API Retrofit app
    // More tests to come: Status codes, JSON Payload, Media Type, other HTTP response headers in the response




}