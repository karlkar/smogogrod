package pl.kksionek.smogogrod.data;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import rx.Observable;

public interface LegionowoRetrofitService {
    @POST("/zapisz/index.hhp")
    @Multipart
    Observable<ResponseBody> savePoint(
            @PartMap Map<String, RequestBody> partMap,
            @Part MultipartBody.Part file);
}
