package shaunhossain.com.rcode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivityLocator extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE_MAP = 2;
    private FancyButton show_location, navigate_location;
    GoogleApiClient mGoogleApiClient;
    EditText code;
    String address = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_locator);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        code = (EditText) findViewById(R.id.code);
        show_location = (FancyButton) findViewById(R.id.show_location);
        navigate_location = (FancyButton) findViewById(R.id.navigate_location);

        Typeface tfa = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Black.ttf");
        code.setTypeface(tfa);


        show_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(code.getText().toString().length() >=10) {
                    LatLng coord = decodeGeoHash(code.getText().toString().trim());
                    if (coord != null) {
                        String lat = String.valueOf(coord.latitude);
                        String lon = String.valueOf(coord.longitude);

                        // get address
                        address = "";
                        mMap.clear();
                        try {
                            if (isNetworkAvailable()) {
                                Geocoder geocoder;
                                List<Address> addresses;
                                geocoder = new Geocoder (MainActivityLocator.this, Locale.getDefault());
                                addresses = geocoder.getFromLocation(coord.latitude, coord.longitude, 1);
                                address = addresses.get(0).getAddressLine(0);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 18), 3000, null);
                        mMap.addMarker(new MarkerOptions ().position(coord).title("RCode: " + code.getText().toString()).snippet(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                    }
                }
            }
        });
        navigate_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(code.getText().toString().length() >=9) {

                    LatLng coord = decodeGeoHash(code.getText().toString().trim());
                    if (coord != null) {
                        String lat = String.valueOf(coord.latitude);
                        String lon = String.valueOf(coord.longitude);
                        Log.i("MainActivityGenerator", lat + "," + lon);

                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon);
                        Intent mapIntent = new Intent (Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }else{

                        Intent intent = new Intent(MainActivityLocator.this, MainActivityLocator.class);
                        startActivity(intent);

                    }

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_locator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.emergencies) {
                String RCode = code.getText().toString();
                Intent sharingIntent = new Intent (Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "RCode");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, RCode);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public LatLng decodeGeoHash(String code) {
        try {
            WGS84Point decrypted = GeoHash.fromGeohashString(code.toLowerCase().replaceAll("-", "")).getPoint();
            return new LatLng (decrypted.getLatitude(), decrypted.getLongitude());

        }catch(Exception ex){
            return null;
        }
    }

    /**
     * Generate Geohash Address Code
     */

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
