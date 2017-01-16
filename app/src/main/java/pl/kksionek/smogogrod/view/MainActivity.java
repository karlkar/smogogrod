package pl.kksionek.smogogrod.view;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import pl.kksionek.smogogrod.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ReportFragment.ReportFragmentListener {

    public static final int REQUEST_IMAGE_CAPTURE = 9876;

    private ReportFragment mReportFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_main, new StatusFragment())
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.menu_item_status);
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
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        //TODO: Reuse fragments
        if (id == R.id.menu_item_report) {
            if (mReportFragment == null)
                mReportFragment = new ReportFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_main, mReportFragment)
                    .commit();
        } else if (id == R.id.menu_item_map) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_main, new MapFragment())
                    .commit();
        } else if (id == R.id.menu_item_status) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_main, new StatusFragment())
                    .commit();
        } else if (id == R.id.menu_item_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK)
                setImageData(resultCode == RESULT_OK ? data : null);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setImageData(Intent data) {
        mReportFragment.setImageData(data);
    }

    @Override
    public void onPictureRequested(Intent data) {
        startActivityForResult(data, REQUEST_IMAGE_CAPTURE);
    }
}
