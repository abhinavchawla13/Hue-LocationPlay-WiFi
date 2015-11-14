package com.philips.lighting.LocationPlayWiFi;

import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.LocationPlayWiFi.R;

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

    //    GUI Variables
    TextView connectionStrength;
    Button refresh;
    EditText hueWiFi;
    EditText presentWiFiText;
    ImageButton hueButton;
    SharedPreferences savedHue;
    String homeHue;
    String hueBottonPresent;
    ImageView status;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle("Hue Location Play!");
        setContentView(R.layout.activity_main);
        phHueSDK = PHHueSDK.create();
        phHueSDK.setAppName("LocationPlay");

        //GUI Variables
        connectionStrength = (TextView) findViewById(R.id.connectionStrength);
        refresh = (Button) findViewById(R.id.refresh);
        hueWiFi = (EditText) findViewById(R.id.hueWiFiText);
        presentWiFiText = (EditText) findViewById(R.id.presentWiFiText);
        hueButton = (ImageButton) findViewById(R.id.hueButton);
        savedHue = getSharedPreferences("home", Context.MODE_PRIVATE);
        homeHue = savedHue.getString("homeHue", null);
        status = (ImageView) findViewById(R.id.status);

        presentWiFiText.setEnabled(false);
        presentWiFiText.setFocusable(false);
        hueWiFi.setEnabled(false);
        hueWiFi.setFocusable(false);

        hueBottonPresent = "edit";
        hueWiFi.setText(savedHue.getString("homeHue", null));

        mainWiFi();
        changeStatus();

        refresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mainWiFi();
                changeStatus();
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

    private boolean sameWifiName(){
        return presentWiFiText.getText().toString().equals(hueWiFi.getText().toString());
    }

    private  void changeStatus(){
        if (sameWifiName()){
            status.setBackgroundResource(R.drawable.yes);
        }
        else{
            status.setBackgroundResource(R.drawable.no);
            connectionStrength.setText("Connection Strength: 0%");
        }
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
        changeStatus();
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
                connectionStrength.setText("Connection Strength: " + level/100 + "%");
            }
        }
    }


    public void randomLights(int level) {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        Random rand = new Random();

        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
//            distance.setText(light.getLastKnownLightState().getBrightness().toString());
//            lightState.setBrightness(3);
//            lightState.setHue(rand.nextInt(MAX_HUE));
            lightState.setOn(true);
            lightState.setTransitionTime(15);
            if (level < 6000) {
                lightState.setOn(false);
            }
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

}
