package pl.kksionek.smogogrod;

import android.app.Application;
import android.content.Context;

import pl.kksionek.smogogrod.data.AirRetrofitService;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SmogApplication extends Application {

    AirRetrofitService mAirRetrofitService;

    public static AirRetrofitService getAirRetrofitService(Context context) {
        return ((SmogApplication)context.getApplicationContext()).mAirRetrofitService;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAirRetrofitService = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl("http://powietrze.gios.gov.pl")
                .build().create(AirRetrofitService.class);

    }
}
