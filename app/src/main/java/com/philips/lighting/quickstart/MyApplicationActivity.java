package com.philips.lighting.quickstart;

import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

/**
 * MyApplicationActivity - The starting point for creating your own Hue App.
 * Currently contains a simple view with a button to change your lights to random colours.  Remove this and add your own app implementation here! Have fun!
 *
 * @author SteveyO
 */
public class MyApplicationActivity extends Activity {
    //    In activity variables
    private PHHueSDK phHueSDK;
    private static final int MAX_HUE = 65535;
    public static final String TAG = "QuickStart";
//    String mLastUpdateTime;
//    GoogleApiClient mGoogleApiClient;
//    LocationRequest mLocationRequest;
//    Location mCurrentLocation;
//    Location mLastLocation;
//    String homeLatitude;
//    String homeLongitude;
//    SharedPreferences savedHomeLocation;

    //    GUI Variables
    TextView latitudeText;
    TextView longitudeText;
    Button randomButton;
    Button setHomeButton;
    TextView homeLocation;
    TextView distance;

//    New GUI
    EditText hueWiFi;
    EditText presentWiFiText;
    ImageButton hueButton;
    SharedPreferences savedHue;
    String homeHue;
    String hueBottonPresent;

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
        setHomeButton = (Button) findViewById(R.id.refresh);
        homeLocation = (TextView) findViewById(R.id.homeLocation);
        distance = (TextView) findViewById(R.id.distance);

//        New GUI
        hueWiFi = (EditText) findViewById(R.id.hueWiFiText);
        presentWiFiText = (EditText) findViewById(R.id.presentWiFiText);
        hueButton = (ImageButton) findViewById(R.id.hueButton);
        savedHue = getSharedPreferences("home", Context.MODE_PRIVATE);
        homeHue = savedHue.getString("homeHue", null);

        presentWiFiText.setEnabled(false);
        presentWiFiText.setFocusable(false);
        hueWiFi.setEnabled(false);
        hueWiFi.setFocusable(false);

        hueBottonPresent = "edit";
        hueWiFi.setText(savedHue.getString("homeHue", null));

        mainWiFi();

        setHomeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mainWiFi();
            }
        });

        hueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hueBottonPresent.equals("edit")) {
                    hueWorkEdit();
                    hueBottonPresent = "save";
                }
                else {
                    hueWorkSave();
                    hueBottonPresent = "edit";
                }
            }

        });
    }

    private void hueWorkEdit(){
        hueWiFi.setEnabled(true);
        hueWiFi.setFocusableInTouchMode(true);
        hueWiFi.setFocusable(true);
        hueButton.setBackgroundResource(R.drawable.done);

    }

    private void hueWorkSave(){
        hueButton.setBackgroundResource(R.drawable.edit);
        hueWiFi.setEnabled(false);
        hueWiFi.setFocusable(false);
        SharedPreferences.Editor edit = savedHue.edit();
        edit.clear();
        edit.putString("homeHue", hueWiFi.getText().toString());
        edit.commit();
        Toast.makeText(MyApplicationActivity.this, "Hue's WiFi SSID saved." ,Toast.LENGTH_LONG).show();
        mainWiFi();
    }

    private void mainWiFi(){
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int state = wifi.getWifiState();
        if (state == WifiManager.WIFI_STATE_ENABLED) {
            WifiInfo currentWifi = wifi.getConnectionInfo();
            String presentWifiName = currentWifi.getSSID().substring(1, currentWifi.getSSID().length() - 1);
            presentWiFiText.setText(presentWifiName);
            if (presentWifiName.equals(hueWiFi.getText().toString())) {
                int level = wifi.calculateSignalLevel(currentWifi.getRssi(), 10000);
                randomLights(level);
                longitudeText.setText("Level is " + level + " out of 10000");
            }
        }
    }

    private void checkWifi() {
        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int state = wifi.getWifiState();
        if (state == WifiManager.WIFI_STATE_ENABLED) {
            List<ScanResult> results = wifi.getScanResults();
            int level = 150;
            for (ScanResult result : results) {
                {
                    if (result.BSSID.equals(wifi.getConnectionInfo().getBSSID())) {

                        level = WifiManager.calculateSignalLevel(wifi.getConnectionInfo().getRssi(),
                                result.level);
                        int level2 = WifiManager.calculateSignalLevel(wifi.getConnectionInfo().getRssi(),
                                500);
                        int difference = level * 100 / result.level;
                        int difference2 = level2 * 100 / 500;
//                    int signalStrangth= 0;
//                    if(difference >= 100)
//                        signalStrangth = 4;
//                    else if(difference >= 75)
//                        signalStrangth = 3;
//                    else if(difference >= 50)
//                        signalStrangth = 2;
//                    else if(difference >= 25)
//                        signalStrangth = 1;
//                    homeLocation.setText(homeLocation.getText() + "\n" + result.SSID + ": Difference :" + difference + " signal state:" + signalStrangth);
//                        int level = WifiManager.calculateSignalLevel(wifi.getConnectionInfo().getRssi(),result.level);
                        homeLocation.setText("Level: " + level + " || Result.level: " + result.level + " || Difference: " + difference);
                        latitudeText.setText("Leve2: " + level2 + " || Result.level: " + 500 + " || Difference: " + difference2);
                    }
                }
            }
        }
    }


    public void randomLights(int level) {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        Random rand = new Random();

        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            distance.setText(light.getLastKnownLightState().getBrightness().toString());
//            lightState.setBrightness(3);
//            lightState.setHue(rand.nextInt(MAX_HUE));
            lightState.setTransitionTime(15);
            if (level < 6000) {
                lightState.setBrightness(3);
            }
//            else if (level < 7000){
//                lightState.setBrightness(60);
//            }
            else if (level < 7500){
                lightState.setBrightness(80);
            }
            else if (level < 9000) {
                lightState.setBrightness(170);
            }
            else {
                lightState.setBrightness(250);
            }
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

    //
//
    private void updateUI() {
//        latitudeText.setText("(" + String.valueOf(mCurrentLocation.getLatitude()) + ", " + String.valueOf(mCurrentLocation.getLongitude()) + ")");
//        longitudeText.setText(mLastUpdateTime);
//        distance.setText(String.valueOf(calculateDistance(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(),
//                Double.parseDouble(homeLatitude), Double.parseDouble(homeLongitude))));
    }

//
}
