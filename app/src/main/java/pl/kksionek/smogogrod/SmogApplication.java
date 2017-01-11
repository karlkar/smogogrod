package pl.kksionek.smogogrod;

import android.app.Application;
import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import pl.kksionek.smogogrod.data.AirRetrofitService;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SmogApplication extends Application {

    AirRetrofitService mAirRetrofitService;
    private OkHttpClient mOkHttpClient;

    public static AirRetrofitService getAirRetrofitService(Context context) {
        return ((SmogApplication)context.getApplicationContext()).mAirRetrofitService;
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

        mAirRetrofitService = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(mOkHttpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl("http://powietrze.gios.gov.pl")
                .build().create(AirRetrofitService.class);

    }
}
