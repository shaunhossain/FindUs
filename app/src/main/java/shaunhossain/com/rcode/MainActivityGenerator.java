package shaunhossain.com.rcode;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import shaunhossain.com.rcode.DB.DBAdapter;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

public class MainActivityGenerator extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE_MAP = 2;
    private Button share;
    TextView code;
    GoogleApiClient mGoogleApiClient;
    String encrypted = "";
    String address = "";
    DBAdapter db = new DBAdapter(this);
    EditText editAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // ask for gps
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            showGPSDisabledAlertToUser();


        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        code = (TextView) findViewById(R.id.code);
        editAddress = (EditText) findViewById(R.id.editAddress);
        Typeface tfa = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Black.ttf");
        code.setTypeface(tfa);
        editAddress.setTypeface(tfa);


        mehdi.sakout.fancybuttons.FancyButton share = (mehdi.sakout.fancybuttons.FancyButton) findViewById(R.id.share);
        if (share != null) {
            share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (encrypted != "") {
                                Intent sharingIntent = new Intent (Intent.ACTION_SEND);
                                sharingIntent.setType("text/plain");
                                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Generated RCode");
                                sharingIntent.putExtra(Intent.EXTRA_TEXT, encrypted.toUpperCase());
                                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                            } else {
                                Snackbar.make(v, "No code to share", Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            }
                }
            });
        }
        mehdi.sakout.fancybuttons.FancyButton viewCodes = (mehdi.sakout.fancybuttons.FancyButton) findViewById(R.id.viewCodes);
        if (viewCodes != null) {
            viewCodes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent (MainActivityGenerator.this, ViewCodes.class);
                    startActivityForResult(intent, 0);
                    overridePendingTransition(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.generate_address) {
            generateGeoHash();
            return true;
        } else if (id == R.id.save) {
            if (encrypted != "") {
                db.open();
                if (!db.existsCode(encrypted)) {
                    db.insertRecord(encrypted, editAddress.getText().toString());
                    Snackbar.make(this.findViewById(R.id.save), "RCode saved.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(this.findViewById(R.id.save), "RCode already Exists!", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
                db.close();
            } else {

                Snackbar.make(this.findViewById(R.id.save), "Nothing to be saved!", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Generate Geohash Address Code
     */
    public void generateGeoHash() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            Log.e("Permision", "Permision is already granted!");
            // if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            //Log.e("LastLoc", String.valueOf(mLastLocation.getLatitude()) + "," + String.valueOf(mLastLocation.getLongitude()));
            if (mLastLocation != null) {
                encrypted = GeoHash.geoHashStringWithCharacterPrecision(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 10);
                // Get address
                address = "";
                editAddress.setText(address);
                try {
                    if (isNetworkAvailable()) {
                        Geocoder geocoder;
                        List<Address> addresses;
                        geocoder = new Geocoder (this, Locale.getDefault());
                        addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                        address = addresses.get(0).getAddressLine(0);
                        editAddress.setText(address);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // they are inserted with lower case
                WGS84Point decrypted = GeoHash.fromGeohashString(encrypted.toLowerCase()).getPoint();
                encrypted = encrypted.substring(0, 4) + "-" + encrypted.substring(4, 8) + "-" + encrypted.substring(8);
                code.setText(encrypted.toUpperCase());
                mMap.clear();
                LatLng myLocationAfterHash = new LatLng (decrypted.getLatitude(), decrypted.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocationAfterHash, 18), 3000, null);
                mMap.addMarker(new MarkerOptions ().position(myLocationAfterHash).title("RCode: " + encrypted.toUpperCase()).snippet(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            } else {
                code.setText("Try again later...");
                encrypted = "";
                address = "";
                editAddress.setText(address);
            }
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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


        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMapLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("Permision", "Permision Granted!");
                    // Do what you want
                    enableMapLocation();
                    generateGeoHash();
                } else {
                    Log.e("Permision", "Permision Denied");
                }
                return;
            }
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
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onPause() {
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mGoogleApiClient.connect();
        super.onResume();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent gpsOptionsIntent = new Intent (
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(gpsOptionsIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
