package shaunhossain.com.rcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import shaunhossain.com.rcode.DB.DBAdapter;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

public class ViewCodes extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    DBAdapter db = new DBAdapter(this);
    Typeface tf;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    ArrayList<ListViewItem> items;
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE_MAP = 2;
    String allCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_codes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        recyclerView = (RecyclerView) findViewById(R.id.recyclelist);
        items = new ArrayList<> ();

        insertItems(items);


//        adapter = new RecyclerViewAdapter(ViewCodes.this, items, getAssets(),mMap);
//        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(ViewCodes.this));
    }

    private void insertItems(ArrayList<ListViewItem> items) {
        items.clear();
        db.open();
        final Cursor c = db.getAllCodes();
        if (c.moveToFirst()) {
            do {
                items.add(new ListViewItem() {
                    {
                        code = c.getString(0).toUpperCase();
                        address = c.getString(1);
                    }
                });
            } while (c.moveToNext());
        }
        c.close();
        db.close();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMapLocation();
    }

    /*
 *  Enable Map Location
 */
    public void enableMapLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE_MAP);
        } else {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);

            mMap.clear();
            db.open();
            final Cursor c = db.getAllCodes();
            boolean first = true;
            if (c.moveToFirst()) {
                do {
                    String code = c.getString(0);
                    String address = c.getString(1);
                    WGS84Point decrypted = GeoHash.fromGeohashString(code.replaceAll("-", "")).getPoint();

                    LatLng myLocationAfterHash = new LatLng (decrypted.getLatitude(), decrypted.getLongitude());
                    if (first == true) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocationAfterHash, 13), 1, null);
                        first = false;
                    }
                    mMap.addMarker(new MarkerOptions ().position(myLocationAfterHash).title("RCode: " + code.toUpperCase()).snippet(address));
                } while (c.moveToNext());
            }
            c.close();
            db.close();
            ///
            adapter = new RecyclerViewAdapter(ViewCodes.this, items, getAssets(), mMap);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager (ViewCodes.this));

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE_MAP: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("Permision", "Permision Granted!");
                    // Do what you want
                    enableMapLocation();
                } else {
                    Log.e("Permision", "Permision Denied");
                }
                return;
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_codes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.share_all) {
            shareAllCodes();
            return true;
        }else if( id == android.R.id.home){
            finish();
            overridePendingTransition(android.R.anim.fade_in,
                    android.R.anim.fade_out);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    private void shareAllCodes() {
        allCodes = "";
        db.open();
        final Cursor c = db.getAllCodes();
        if (c.moveToFirst()) {
            do {
                allCodes += c.getString(0).toUpperCase() + "\n";
              //  allCodes += "Address: " + c.getString(1) + "\n";
              //  allCodes += "-----------------------------\n";
            } while (c.moveToNext());
        }
        c.close();
        db.close();

        Intent sharingIntent = new Intent (Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "All Generated RCodes");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, allCodes);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

}
