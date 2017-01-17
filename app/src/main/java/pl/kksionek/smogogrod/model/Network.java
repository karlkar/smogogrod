package pl.kksionek.smogogrod.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
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
import rx.Observable;
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

    private Network() {
    }

    public static Observable<ArrayList<Station>> getStations(@NonNull Context context) {
        return SmogApplication.getAirRetrofitService(context)
                .getStations("AQI")
                .subscribeOn(Schedulers.io());
    }

    public static Observable<StationDetails> getStationDetails(@NonNull Context context, int id) {
        return SmogApplication.getAirRetrofitService(context)
                .getStationDetails(1, id)
                .subscribeOn(Schedulers.io());
    }

    public static Observable<StationDetails> getLegionowoStationDetails(@NonNull Context context) {
        return getStationDetails(context, LEGIONOWO_STATION_ID);
    }

    public static Observable<ArrayList<MarkedPlace>> getMarkedPlaces(@NonNull Context context) {
        return Observable.fromCallable(() -> getMarkedPlacesArray(context))
                .subscribeOn(Schedulers.io());
    }

    static private ArrayList<MarkedPlace> getMarkedPlacesArray(Context context)
            throws ParseException, IOException {
        Matcher featureMatcher = FEATURE_PATTERN.matcher(getMarkedPlacesSiteSource(context));
        if (featureMatcher.find()) {
            return extractPlaces(featureMatcher.group(1));
        } else {
            throw new ParseException("No matches found for all features.", 0);
        }
    }

    @Nullable
    static private String getMarkedPlacesSiteSource(@NonNull Context context) throws IOException {
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

    static private ArrayList<MarkedPlace> extractPlaces(@NonNull String text) throws ParseException {
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

    public static Observable<ResponseBody> sendReport(
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
        return Observable.defer(() -> Observable.just(prepareFilePart(context, "zdjecie", fileUri)))
                .flatMap(filePart -> SmogApplication.getLegionowoRetrofitService(context)
                        .savePoint(partMap, filePart)
                        .subscribeOn(Schedulers.io()))
                .subscribeOn(Schedulers.io());
    }

    static private MultipartBody.Part prepareFilePart(
            @NonNull Context context,
            @NonNull String name,
            @NonNull Uri fileUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), fileUri);
            boolean higher = bitmap.getWidth() > bitmap.getHeight();
            int width = bitmap.getWidth(), height = bitmap.getHeight();
            if (higher) {
                if (bitmap.getWidth() > 500) {
                    width = 500;
                    height /= (bitmap.getWidth() / 500.0f);
                }
            } else {
                if (bitmap.getHeight() > 500) {
                    height = 500;
                    width /= (bitmap.getHeight() / 500.0f);
                }
            }
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, false);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            RequestBody requestFile = RequestBody.create(
                    MediaType.parse(MULTIPART_FORM_DATA),
                    byteArray);
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData(
                            name,
                            fileUri.getLastPathSegment(),
                            requestFile);
            return body;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    static private RequestBody createPartFromString(@NonNull String descriptionString) {
        return RequestBody.create(
                MediaType.parse(MULTIPART_FORM_DATA), descriptionString);
    }
}
