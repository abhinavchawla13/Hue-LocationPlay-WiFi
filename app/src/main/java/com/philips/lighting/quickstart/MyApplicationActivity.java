package com.philips.lighting.quickstart;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

/**
 * MyApplicationActivity - The starting point for creating your own Hue App.
 * Currently contains a simple view with a button to change your lights to random colours.  Remove this and add your own app implementation here! Have fun!
 *
 * @author SteveyO
 */
public class MyApplicationActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

//    In activity variables
    private PHHueSDK phHueSDK;
    private static final int MAX_HUE = 65535;
    public static final String TAG = "QuickStart";
    String mLastUpdateTime;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mCurrentLocation;
    Location mLastLocation;
    String homeLatitude;
    String homeLongitude;
    SharedPreferences savedHomeLocation;

//    GUI Variables
    TextView latitudeText;
    TextView longitudeText;
    Button randomButton;
    Button setHomeButton;
    TextView homeLocation;
    TextView distance;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Hue Location Play!");
        setContentView(R.layout.activity_main);
        phHueSDK = PHHueSDK.create();
        phHueSDK.setAppName("LocationPlay");

        latitudeText = (TextView) findViewById(R.id.latitudeText);
        longitudeText = (TextView) findViewById(R.id.longitudeText);
        randomButton = (Button) findViewById(R.id.buttonRand);
        setHomeButton = (Button) findViewById(R.id.setHome);
        homeLocation = (TextView) findViewById(R.id.homeLocation);
        distance = (TextView) findViewById(R.id.distance);

        savedHomeLocation = getSharedPreferences("home", Context.MODE_PRIVATE);
        homeLatitude = savedHomeLocation.getString("homeLatitude", null);
        homeLongitude = savedHomeLocation.getString("homeLongitude", null);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        createLocationRequest();


        displayHomeLocation();
        randomButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                randomLights();
            }
        });

        setHomeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                homeLocationChangeAlert();
            }
        });

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.
//        mLocationRequest.setPriority(70);
    }

    public void setHomeLocation(){
        homeLatitude = String.valueOf(mCurrentLocation.getLatitude());
        homeLongitude = String.valueOf(mCurrentLocation.getLongitude());
        SharedPreferences.Editor edit = savedHomeLocation.edit();
        edit.clear();
        edit.putString("homeLatitude", homeLatitude);
        edit.putString("homeLongitude", homeLongitude);
        edit.commit();
    }

    public void displayHomeLocation(){
        if (homeLatitude != null && homeLongitude != null){
            homeLocation.setText("(" + homeLatitude + ", " + homeLongitude + ")");
        }
        else{
            homeLocation.setText("");
        }
    }

    public void homeLocationChangeAlert(){
        DialogInterface.OnClickListener confirmCall = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case  DialogInterface.BUTTON_POSITIVE:
                        setHomeLocation();
                        displayHomeLocation();
                        Toast.makeText(MyApplicationActivity.this, "Home location saved!", Toast.LENGTH_SHORT).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        displayHomeLocation();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to set this place as your home location?")
                .setPositiveButton("Yes", confirmCall)
                .setNegativeButton("No", null).show();
    }


    public void randomLights() {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        Random rand = new Random();

        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            lightState.setHue(rand.nextInt(MAX_HUE));
            // To validate your lightstate is valid (before sending to the bridge) you can use:  
            // String validState = lightState.validateState();
            bridge.updateLightState(light, lightState, listener);
            //  bridge.updateLightState(light, lightState);   // If no bridge response is required then use this simpler form.
        }
    }

    // If you want to handle the response from the bridge, create a PHLightListener object.
    PHLightListener listener = new PHLightListener() {

        @Override
        public void onSuccess() {
        }

        @Override
        public void onStateUpdate(Map<String, String> arg0, List<PHHueError> arg1) {
            Log.w(TAG, "Light has updated");
        }

        @Override
        public void onError(int arg0, String arg1) {
        }

        @Override
        public void onReceivingLightDetails(PHLight arg0) {
        }

        @Override
        public void onReceivingLights(List<PHBridgeResource> arg0) {
        }

        @Override
        public void onSearchComplete() {
        }
    };

    @Override
    protected void onDestroy() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {

            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }

            phHueSDK.disconnect(bridge);
            super.onDestroy();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            latitudeText.setText("(" + String.valueOf(mLastLocation.getLatitude()) + ", "
                    + String.valueOf(mLastLocation.getLongitude()) + ")");
            longitudeText.setText(" ");
        }
        startLocationUpdates();


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mCurrentLocation.setAccuracy((float)0.0001);
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    private void updateUI() {
        latitudeText.setText("(" + String.valueOf(mCurrentLocation.getLatitude()) + ", " + String.valueOf(mCurrentLocation.getLongitude()) + ")");
        longitudeText.setText(mLastUpdateTime);
        distance.setText(String.valueOf(calculateDistance(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(),
                Double.parseDouble(homeLatitude), Double.parseDouble(homeLongitude))));
    }

    public double distanceCalculator(){
        if (homeLatitude != null && homeLongitude != null) {
            double dlat = deg2rad((mCurrentLocation.getLatitude() - Double.parseDouble(homeLatitude)));
            double dlon = deg2rad((mCurrentLocation.getLongitude() - Double.parseDouble(homeLongitude)));
            double calA = Math.pow((Math.sin(dlat/2)),2) +
                    Math.cos(deg2rad(mCurrentLocation.getLatitude()))*Math.cos(deg2rad(Double.parseDouble(homeLatitude)))*
                            Math.pow(Math.sin(dlon/2),2);
            double calC = 2*Math.atan2(Math.sqrt(calA), Math.sqrt(1-calA));
            return 6371*calC*(10^20);

//            return Math.sqrt(Math.pow((mCurrentLocation.getLatitude() - Double.parseDouble(homeLatitude)),2) +
//                    Math.sqrt(Math.pow((mCurrentLocation.getLongitude() - Double.parseDouble(homeLongitude)),2)));
        }
        return (double) mCurrentLocation.getAccuracy();
    }

    public double deg2rad(double deg) {
        return deg*(Math.PI/180);
    }

    public final static double AVERAGE_RADIUS_OF_EARTH = 6371;
    public double calculateDistance(double userLat, double userLng,
                                 double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return AVERAGE_RADIUS_OF_EARTH * c * (10^4);
    }

//    I would like if it works in the background as well
//    @Override
//    public void onPause(){
//        super.onPause();
//        stopLocationUpdates();
//    }
//
//    private void stopLocationUpdates() {
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//    }
}
