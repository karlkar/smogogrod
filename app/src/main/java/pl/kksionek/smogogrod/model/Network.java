package pl.kksionek.smogogrod.model;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import pl.kksionek.smogogrod.SmogApplication;
import pl.kksionek.smogogrod.data.MarkedPlace;
import pl.kksionek.smogogrod.data.Station;
import pl.kksionek.smogogrod.data.StationDetails;
import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class Network {

    private static final String TAG = "NETWORK";

    private static final String MULTIPART_FORM_DATA = "multipart/form-data";

    private static final Pattern FEATURE_PATTERN =
            Pattern.compile("var features = \\[([\\w\\W]*?)\\}\\];", Pattern.MULTILINE);
    private static final Pattern POSITION_PATTERN =
            Pattern.compile("google\\.maps\\.LatLng\\(([0-9\\.,]+)\\)");
    private static final Pattern TYPE_PATTERN =
            Pattern.compile("type: '(.*?)'");
    private static final Pattern TITLE_PATTERN =
            Pattern.compile("title: '(.*?)'");
    private static final Pattern LABEL_PATTERN =
            Pattern.compile("label: '(.*?)'");
    private static final int LEGIONOWO_STATION_ID = 471;

    Observable<ArrayList<Station>> getStations(@NonNull Context context) {
        return SmogApplication.getAirRetrofitService(context)
                .getStations("AQI")
                .subscribeOn(Schedulers.io());
    }

    Observable<StationDetails> getStationDetails(@NonNull Context context) {
        return SmogApplication.getAirRetrofitService(context)
                .getStationDetails(1, LEGIONOWO_STATION_ID)
                .subscribeOn(Schedulers.io());
    }

    Observable<ArrayList<MarkedPlace>> getMarkedPlaces(@NonNull Context context) {
        return Observable.fromEmitter(new Action1<Emitter<ArrayList<MarkedPlace>>>() {
            @Override
            public void call(Emitter<ArrayList<MarkedPlace>> emitter) {
                String responseStr;
                try {
                    responseStr = getMarkedPlacesSiteSource(context);
                } catch (IOException e) {
                    emitter.onError(e);
                    return;
                }

                Matcher featureMatcher = FEATURE_PATTERN.matcher(responseStr);
                if (featureMatcher.find()) {
                    ArrayList<MarkedPlace> places = null;
                    try {
                        places = extractPlaces(featureMatcher.group(1));
                        emitter.onNext(places);
                        emitter.onCompleted();
                    } catch (ParseException e) {
                        emitter.onError(e);
                        return;
                    }
                } else {
                    emitter.onError(new ParseException("No matches found for all features.", 0));
                    return;
                }
            }
        }, Emitter.BackpressureMode.DROP);
    }

    @Nullable
    private String getMarkedPlacesSiteSource(@NonNull Context context) throws IOException {
        OkHttpClient okHttpClient = SmogApplication.getOkHttpClient(context);

        Request req = new Request.Builder()
                .url("http://alarm.legionowo.info.pl/")
                .build();

        String responseStr;
        Response response = okHttpClient.newCall(req).execute();
        responseStr = response.body().string();

        if (responseStr == null)
            throw new IOException("Empty response received.");
        return responseStr;
    }

    private ArrayList<MarkedPlace> extractPlaces(@NonNull String text) throws ParseException {
        boolean arrayEnded = false;
        ArrayList<MarkedPlace> places = new ArrayList<>();
        Matcher positionMatcher = POSITION_PATTERN.matcher(text);
        Matcher typeMatcher = TYPE_PATTERN.matcher(text);
        Matcher titleMatcher = TITLE_PATTERN.matcher(text);
        Matcher labelMatcher = LABEL_PATTERN.matcher(text);
        while (!arrayEnded) {
            if (!positionMatcher.find()) {
                if (places.isEmpty()) {
                    throw new ParseException("No matches found for position.", 0);
                }
                arrayEnded = true;
                continue;
            }
            if (!typeMatcher.find()) {
                throw new ParseException("No matches found for type.", 0);
            }
            if (!titleMatcher.find()) {
                throw new ParseException("No matches found for title.", 0);
            }
            if (!labelMatcher.find()) {
                throw new ParseException("No matches found for label.", 0);
            }
            places.add(new MarkedPlace(
                    positionMatcher.group(1),
                    typeMatcher.group(1),
                    titleMatcher.group(1),
                    labelMatcher.group(1)));
        }
        return places;
    }

    Observable<ResponseBody> sendReport(
            @NonNull Context context,
            @NonNull String name,
            @NonNull String desc,
            @NonNull String city,
            @NonNull String address,
            @NonNull String number,
            @NonNull String reporter,
            @NonNull String email,
            @NonNull Uri fileUri) {
        Map<String, RequestBody> partMap = new HashMap<>();
        partMap.put("nazwa", createPartFromString(name));
        partMap.put("opis", createPartFromString(desc));
        partMap.put("miejscowosc", createPartFromString(city));
        partMap.put("adres", createPartFromString(address));
        partMap.put("budynek", createPartFromString(number));
        partMap.put("zglaszajacy", createPartFromString(reporter));
        partMap.put("email", createPartFromString(email));

        MultipartBody.Part filePart = prepareFilePart("zdjecie", fileUri);
        return SmogApplication.getLegionowoRetrofitService(context)
                .savePoint(partMap, filePart)
                .subscribeOn(Schedulers.io());
    }

    private MultipartBody.Part prepareFilePart(@NonNull String name, @NonNull Uri fileUri) {
        File file = new File(fileUri.getPath());
        RequestBody requestFile =
                RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), file);

        MultipartBody.Part body =
                MultipartBody.Part.createFormData(name, file.getName(), requestFile);
        return body;
    }

    @NonNull
    private RequestBody createPartFromString(@NonNull String descriptionString) {
        return RequestBody.create(
                MediaType.parse(MULTIPART_FORM_DATA), descriptionString);
    }
}