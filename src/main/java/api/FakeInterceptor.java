package api;

import okhttp3.*;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import util.PathDate;
import util.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;

public class FakeInterceptor implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeInterceptor.class);

    ResponseBody cityBody;
    ResponseBody forecastBody;

    String [] validCities = {"lon", "london", "dub", "dubai", "hel", "helsinki", "hyderabad"};
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    PathDate pathDate = new PathDate(tomorrow);


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        System.out.println("date: " + pathDate + "/");

        System.out.println("Request URi: " + chain.request().url().uri());
        System.out.println("search endpoint?: " + chain.request().url().uri().getPath().endsWith("location/search/"));
        System.out.println("forecast endpoint?: " + chain.request().url().uri().getPath().endsWith(pathDate + "/"));

        // Checking which service endpoint we are hitting
        Boolean search_endpoint = chain.request().url().uri().getPath().endsWith("location/search/");
        Boolean forecast_endpoint = chain.request().url().uri().getPath().endsWith(pathDate + "/");

        // Case it is the search endpoint, we modify the response
        if (search_endpoint){
            String searchQuery = chain.request().url().uri().getQuery().split("=")[1];
            // If city is not in valid cities array, unrecognized city scenario
            if (!Arrays.asList(validCities).contains(searchQuery)) {
                cityBody = ResponseBody.create(MediaType.parse("application/json"), Utils.readResourceFileToBytes("badCity.json"));
            }

            else {
                // if it is Hyderabad (Our test city with no forecast), im which case we use noForecastCity.json which holds Hyderabad
                // "woeid": 2295414 that will later trigger an empty forecast response when service.getForecast() is called
                if ( searchQuery.equals("hyderabad")) {
                    cityBody = ResponseBody.create(MediaType.parse("application/json"), Utils.readResourceFileToBytes("noForecastCity.json"));

                }
                // else it must be valid city
                else{
                    cityBody = ResponseBody.create(MediaType.parse("application/json"), Utils.readResourceFileToBytes("fakeCity.json"));
                }
            }

            // Return modified city response

            /*return new Response.Builder()
                    .request(request)
                    .body(cityBody)
                    .protocol(Protocol.HTTP_2)
                    .message("")
                    .build();*/

            return response.newBuilder().body(cityBody)
                    .message("OK")
                    .protocol(Protocol.HTTP_2)
                    .code(200)
                    //.request(request)
                    .build();
        }
        else {
            if (forecast_endpoint){
                String json;
                // if city is Hyderabad (Our test city with no forecast), set response to No Forecast one
                if (chain.request().url().uri().getPath().endsWith("2295414/" + pathDate + "/")){
                    json = "noForecastCity.json";
                    //There is no need to modify default status code as this still triggers a valid 200 response jut empty
                }
                // Case where city is valid and forecast is found
                else {
                    json = "fakeForcast.json";
                }

                //body = ResponseBody.create("[ { \"id\": 5749047877959680, \"weather_state_name\": \"Heavy SNOW\", \"weather_state_abbr\": \"hc\", \"wind_direction_compass\": \"WNW\", \"created\": \"2021-11-10T20:35:41.142286Z\", \"applicable_date\": \"2021-11-12\", \"min_temp\": 2.675, \"max_temp\": 6.365, \"the_temp\": 6.265000000000001, \"wind_speed\": 11.226436167870304, \"wind_direction\": 290.16840347962, \"air_pressure\": 1007.5, \"humidity\": 80, \"visibility\": 10.834849479042393, \"predictability\": 71 } ]".getBytes(StandardCharsets.UTF_8), MediaType.parse("application/json"));
                forecastBody = ResponseBody.create(MediaType.parse("application/json"), Utils.readResourceFileToBytes(json));

                // Return modified forecast response


                /*return new Response.Builder()
                        .request(chain.request())
                        .body(forecastBody)
                        .protocol(Protocol.HTTP_2)
                        .message("")
                        .build();*/

                return response.newBuilder().body(forecastBody)
                        .message("")
                        .protocol(Protocol.HTTP_2)
                        .code(200)
                        .build();

            }
            // As requested: I do not change response in case not matched any of the 2 endpoints above
            //return chain.proceed(chain.request());
            return response;

        }


    }
}