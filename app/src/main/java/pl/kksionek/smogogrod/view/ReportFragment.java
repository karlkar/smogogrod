package pl.kksionek.smogogrod.view;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import pl.kksionek.smogogrod.R;
import pl.kksionek.smogogrod.model.Network;
import rx.android.schedulers.AndroidSchedulers;

import static android.app.Activity.RESULT_OK;

public class ReportFragment extends Fragment {

    private FloatingActionButton mFab;

    public interface ReportFragmentListener {
        void onPictureRequested(Intent data);
    }

    private ReportFragmentListener mReportFragmentListener = null;

    private static final String TAG = "ReportFragment";

    private TextView mReportNameTextView;
    private TextView mReportDescTextView;
    private TextView mReportCityTextView;
    private TextView mReportStreetTextView;
    private TextView mReportStreetNumberTextView;
    private TextView mReportReporterTextView;
    private TextView mReportEMailTextView;
    private Button mBtnReport;
    private Uri mImageUri;
    private ImageView mImage;
    private View mOverlay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, null);

        mReportNameTextView = (TextView) view.findViewById(R.id.report_name);
        mReportDescTextView = (TextView) view.findViewById(R.id.report_desc);
        mReportCityTextView = (TextView) view.findViewById(R.id.report_city);
        mReportStreetTextView = (TextView) view.findViewById(R.id.report_street);
        mReportStreetNumberTextView = (TextView) view.findViewById(R.id.report_number);
        mReportReporterTextView = (TextView) view.findViewById(R.id.report_reporter);
        mReportEMailTextView = (TextView) view.findViewById(R.id.report_email);
        mBtnReport = (Button) view.findViewById(R.id.report_button);
        mBtnReport.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(v.getContext().getPackageManager()) != null) {
                mReportFragmentListener.onPictureRequested(takePictureIntent);
            }
        });
        mImage = (ImageView) view.findViewById(R.id.report_image);

        mOverlay = view.findViewById(R.id.fragment_report_progress_overlay);

        mReportReporterTextView.setText(PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString("PREF_USERNAME", ""));
        mReportEMailTextView.setText(PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString("PREF_EMAIL", ""));

        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mFab.setOnClickListener(view1 -> {
            if (validate()) {
                Log.d(TAG, "onCreateView: Send data");
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit()
                        .putString("PREF_USERNAME", mReportReporterTextView.getText().toString())
                        .putString("PREF_EMAIL", mReportEMailTextView.getText().toString())
                        .apply();
                showProgressOverlay(true);
                Network.sendReport(
                        getContext(),
                        mReportNameTextView.getText().toString(),
                        mReportDescTextView.getText().toString(),
                        mReportCityTextView.getText().toString(),
                        mReportStreetTextView.getText().toString(),
                        mReportStreetNumberTextView.getText().toString(),
                        mReportReporterTextView.getText().toString(),
                        mReportEMailTextView.getText().toString(),
                        mImageUri)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(responseBody -> {
                                    showProgressOverlay(false);
                                },
                                throwable -> {
                                    showProgressOverlay(false);
                                    throwable.printStackTrace();
                                    Toast.makeText(
                                            getContext(),
                                            "Problem z połączeniem. Spróbuj ponownie.",
                                            Toast.LENGTH_SHORT).show();
                                });
            } else {
                Toast.makeText(getContext(), "Wypełnij formularz", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void showProgressOverlay(boolean show) {
        mFab.setEnabled(!show);
        int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        if (show) {
            mOverlay.setVisibility(View.VISIBLE);
            mOverlay.setAlpha(0);
            mOverlay.animate().alpha(1.0f).setDuration(duration).start();
        } else {
            mOverlay.animate()
                    .alpha(0.0f)
                    .setDuration(duration)
                    .setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    mOverlay.setVisibility(View.GONE);
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            }).start();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mReportFragmentListener = (ReportFragmentListener) getActivity();
    }

    private boolean validate() {
        if (mReportNameTextView.getText().toString().isEmpty()
                || mReportDescTextView.getText().toString().isEmpty()
                || mReportCityTextView.getText().toString().isEmpty()
                || mReportStreetTextView.getText().toString().isEmpty()
                || mReportStreetNumberTextView.getText().toString().isEmpty()
                || mReportReporterTextView.getText().toString().isEmpty()
                || mReportEMailTextView.getText().toString().isEmpty()
                || mImageUri == null
                || mImageUri.toString().isEmpty())
            return false;
        return true;
    }

    public void setImageData(Intent data) {
        mImageUri = data.getData();
        mImage.setImageURI(mImageUri);

        mImage.setRotation(getOrientation(getContext(), mImageUri));
    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        int result = -1;
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0);
            }
            cursor.close();
        }

        return result;
    }
}
