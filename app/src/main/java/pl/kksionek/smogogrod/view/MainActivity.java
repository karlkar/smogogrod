package pl.kksionek.smogogrod.view;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import pl.kksionek.smogogrod.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ReportFragment.ReportFragmentListener {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_IMAGE_CAPTURE = 9876;
    private static final int REQUEST_IMAGE_PICK = 6789;
    public static final String ACTIVE_FRAGMENT = "ACTIVE_FRAGMENT";
    public static final String FRAG_TAG_REPORT = "REPORT";
    public static final String FRAG_TAG_MAP = "MAP";
    public static final String FRAG_TAG_STATUS = "STATUS";

    private ReportFragment mReportFragment = null;
    private StatusFragment mStatusFragment = null;
    private int mCheckedItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            mCheckedItem = R.id.menu_item_status;
            if (mStatusFragment == null)
                mStatusFragment = new StatusFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_main, mStatusFragment)
                    .commit();
        } else {
            mCheckedItem = savedInstanceState.getInt(ACTIVE_FRAGMENT);
            mReportFragment = (ReportFragment) getFragmentManager().findFragmentByTag(FRAG_TAG_REPORT);
            mStatusFragment = (StatusFragment) getFragmentManager().findFragmentByTag(FRAG_TAG_STATUS);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(mCheckedItem);

        Location loc = new Location("");
        loc.setLatitude(LwoMapFragment.LWO_LATITUDE);
        loc.setLongitude(LwoMapFragment.LWO_LONGITUDE);

        AdView adView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.adMobTestDeviceS7))
                .addTestDevice(getString(R.string.adMobTestDeviceS5))
                .setLocation(loc)
                .build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setAdListener(null);
                adView.setVisibility(View.VISIBLE);
            }
        });
        adView.loadAd(adRequest);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ACTIVE_FRAGMENT, mCheckedItem);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //TODO: Reuse fragments
        if (id == R.id.menu_item_report) {
            if (mReportFragment == null)
                mReportFragment = new ReportFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_main, mReportFragment, FRAG_TAG_REPORT)
                    .commit();
        } else if (id == R.id.menu_item_map) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_main, new LwoMapFragment(), FRAG_TAG_MAP)
                    .commit();
        } else if (id == R.id.menu_item_status) {
            if (mStatusFragment == null)
                mStatusFragment = new StatusFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_main, mStatusFragment, FRAG_TAG_STATUS)
                    .commit();
        } else if (id == R.id.menu_item_share) {
            ShareCompat.IntentBuilder
                    .from(this)
                    .setText(getString(R.string.activity_main_share_text))
                    .setType("text/plain")
                    .setChooserTitle(R.string.activity_main_sharing)
                    .startChooser();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK)
                setImageData(data);
            return;
        } else if (requestCode == REQUEST_IMAGE_PICK) {
            if (resultCode == RESULT_OK)
                setImageData(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setImageData(Intent data) {
        if (mReportFragment != null) {
            mReportFragment.setImageData(data);
        }
    }

    @Override
    public void onPicturePick(Intent chooserIntent) {
        startActivityForResult(chooserIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onPictureRequested(Intent data) {
        startActivityForResult(data, REQUEST_IMAGE_CAPTURE);
    }
}
