package api;

import okhttp3.*;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import util.PathDate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;

public class FakeInterceptor implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeInterceptor.class);
    ResponseBody body;
    String [] validCities = {"lon", "london", "dub", "dubai", "hel", "helsinki", "hyderabad"};
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    PathDate pathDate = new PathDate(tomorrow);

    @Override
    public Response intercept(Chain chain) throws IOException {
        //Request request = chain.request();
        Response response;

        System.out.println("date: " + pathDate + "/");

        //System.out.println("query: " + chain.request().url().query().split("=")[1]);
        System.out.println("URi: " + chain.request().url().uri());
        System.out.println("search endpoint?: " + chain.request().url().uri().getPath().endsWith("location/search/"));
        System.out.println("forecast endpoint?: " + chain.request().url().uri().getPath().endsWith(pathDate + "/"));

        Boolean search_endpoint = chain.request().url().uri().getPath().endsWith("location/search/");
        Boolean forecast_endpoint = chain.request().url().uri().getPath().endsWith(pathDate + "/");

        String responseString  = "";

        if (search_endpoint){
            String searchQuery = chain.request().url().uri().getQuery().split("=")[1];
            if (!Arrays.asList(validCities).contains(searchQuery)) {
            responseString = "[]";
            body = ResponseBody.create("[]".getBytes(StandardCharsets.UTF_8), MediaType.parse("application/json"));
                //body = ResponseBody.create(Utils.readResourceFileToBytes("badCity.json"), MediaType.parse("application/json"));
            }
            else {
                if ( searchQuery.equals("hyderabad")) {
                    responseString = "[{\"title\":\"hyderabad\", \"location_type\":\"City\", \"woeid\":99999, \"latt_long\":\"51.506321,-0.12714\"}]";
                } else{
                    responseString = "[{\"title\":\"Helsinki\", \"location_type\":\"City\", \"woeid\":44418, \"latt_long\":\"51.506321,-0.12714\"}]";
                }
                body = ResponseBody.create("[{\"title\":\"Helsinki\", \"location_type\":\"City\", \"woeid\":44418, \"latt_long\":\"51.506321,-0.12714\"}]".getBytes(StandardCharsets.UTF_8), MediaType.parse("application/json"));
                //body = ResponseBody.create(Utils.readResourceFileToBytes("fakeCity.json"), MediaType.parse("application/json"));
            }
        }
        else {
            if (forecast_endpoint){
                if (chain.request().url().uri().getPath().endsWith("99999/" + pathDate + "/")){
                    responseString ="[ ]";
                } else {
                    responseString ="[ { \"id\": 5749047877959680, \"weather_state_name\": \"Heavy SNOW\", \"weather_state_abbr\": \"hc\", \"wind_direction_compass\": \"WNW\", \"created\": \"2021-11-10T20:35:41.142286Z\", \"applicable_date\": \"2021-11-12\", \"min_temp\": 2.675, \"max_temp\": 6.365, \"the_temp\": 6.265000000000001, \"wind_speed\": 11.226436167870304, \"wind_direction\": 290.16840347962, \"air_pressure\": 1007.5, \"humidity\": 80, \"visibility\": 10.834849479042393, \"predictability\": 71 } ]";
                }

                body = ResponseBody.create("[ { \"id\": 5749047877959680, \"weather_state_name\": \"Heavy SNOW\", \"weather_state_abbr\": \"hc\", \"wind_direction_compass\": \"WNW\", \"created\": \"2021-11-10T20:35:41.142286Z\", \"applicable_date\": \"2021-11-12\", \"min_temp\": 2.675, \"max_temp\": 6.365, \"the_temp\": 6.265000000000001, \"wind_speed\": 11.226436167870304, \"wind_direction\": 290.16840347962, \"air_pressure\": 1007.5, \"humidity\": 80, \"visibility\": 10.834849479042393, \"predictability\": 71 } ]".getBytes(StandardCharsets.UTF_8), MediaType.parse("application/json"));
                //body = ResponseBody.create(Utils.readResourceFileToBytes("fakeForcast.json"), MediaType.parse("application/json"));
            }
        }
        response = new Response.Builder()
                .code(200)
                .message(body.string())
                .request(chain.request())
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(responseString.getBytes(), MediaType.parse("application/json")))
                //.body(body)   // TODO: fix
                .addHeader("content-type", "application/json")
                .build();

        return  response;

    }
}
