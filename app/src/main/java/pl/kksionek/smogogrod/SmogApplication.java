package pl.kksionek.smogogrod;

import android.app.Application;
import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import pl.kksionek.smogogrod.data.AirRetrofitService;
import pl.kksionek.smogogrod.data.LegionowoRetrofitService;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SmogApplication extends Application {

    AirRetrofitService mAirRetrofitService;
    LegionowoRetrofitService mLegionowoRetrofitService;
    private OkHttpClient mOkHttpClient;

    public static AirRetrofitService getAirRetrofitService(Context context) {
        return ((SmogApplication)context.getApplicationContext()).mAirRetrofitService;
    }

    public static LegionowoRetrofitService getLegionowoRetrofitService(Context context) {
        return ((SmogApplication)context.getApplicationContext()).mLegionowoRetrofitService;
    }

    public static OkHttpClient getOkHttpClient(Context context) {
        return ((SmogApplication)context.getApplicationContext()).mOkHttpClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();

        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create();
        RxJavaCallAdapterFactory rxJavaCallAdapterFactory = RxJavaCallAdapterFactory.create();
        
        mAirRetrofitService = new Retrofit.Builder()
                .addConverterFactory(gsonConverterFactory)
                .client(mOkHttpClient)
                .addCallAdapterFactory(rxJavaCallAdapterFactory)
                .baseUrl("http://powietrze.gios.gov.pl")
                .build().create(AirRetrofitService.class);

        mLegionowoRetrofitService = new Retrofit.Builder()
                .addConverterFactory(gsonConverterFactory)
                .client(mOkHttpClient)
                .addCallAdapterFactory(rxJavaCallAdapterFactory)
                .baseUrl("http://alarm.legionowo.info.pl")
                .build().create(LegionowoRetrofitService.class);
    }
}
