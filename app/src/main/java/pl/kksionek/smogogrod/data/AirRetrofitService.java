package pl.kksionek.smogogrod.data;

import java.util.ArrayList;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface AirRetrofitService {
    @POST("/pjp/current/getAQIDetailsList")
    @FormUrlEncoded
    Observable<ArrayList<Station>> getStations(@Field("param") String aqi);
}
