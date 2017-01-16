package pl.kksionek.smogogrod.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
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

import static android.app.Activity.RESULT_OK;

public class ReportFragment extends Fragment {

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

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> {
            if (validate()) {
                Log.d(TAG, "onCreateView: Send data");
            } else {
                Toast.makeText(getContext(), "Wypełnij formularz", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
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
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

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
