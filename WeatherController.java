package com.londonappbrewery.climapm;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.sql.Time;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final int REQUEST_CODE =1235;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "59e57395ed6aab6f696b98e351863cf1";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;
    WeatherDataModel weather = new WeatherDataModel();

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:
        mCityLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(WeatherController.this, ChangeCityCOntroller.class);
                startActivity(myIntent);

            }
        });
    }


    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Clima", "OnResume() Called");

        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("City");

        if(city != null){
            getWeatherforNewCity(city);
        } else {
            Log.d("Clima", "Getting Weather for Current Location");
            getWeatherforCurrentLocation();

        }
    }


    // TODO: Add getWeatherForNewCity(String city) here:
        private void getWeatherforNewCity(String city){
            RequestParams params = new RequestParams();
            params.put("q",city);
            params.put("appid",APP_ID);
            letsDoSomeNetworking(params);
        }

    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherforCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Clima", "onLocationChanged() callback recieved");
              String longitude = String.valueOf( location.getLongitude());
              String latitude = String.valueOf(location.getLatitude());

              Log.d("Clima", "Longitude is " + longitude);
              Log.d("Clima","Latitude is " + latitude);

                RequestParams Params = new RequestParams();
                Params.put("Longi", longitude);
                Params.put("Lati",latitude);
                Params.put("appid",APP_ID);
                letsDoSomeNetworking(Params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Clima", "onStatusChangedCallback () recieved");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Clima", "onProviderEnabled() callback recieved");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Clima", "onProviderDisabled()");

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("CLima","OnPermissionsResult(): Permission Granted!");
                getWeatherforCurrentLocation();
            } else {
                Log.d("Clima",": Permission Denied");
            }
        }
    }



    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
   private void letsDoSomeNetworking(RequestParams Params) {

       AsyncHttpClient client = new AsyncHttpClient();

       client.get(WEATHER_URL, Params, new JsonHttpResponseHandler() {
           @Override
           public void onSuccess(int statusCOde, Header[] headers, JSONObject response) {
               Log.d("Clima", "Success" + response.toString());
               WeatherDataModel weatherData = WeatherDataModel.fromJSON(response);
               updateUI(weatherData);

           }

           @Override
           public void onFailure(int statusCode, Header[] headers,Throwable e, JSONObject response) {
               Log.e("Clima", "Fail " + e.toString());
               Log.d("Clima", "Status Code " + statusCode);
               Toast.makeText(WeatherController.this, "Request Failure", Toast.LENGTH_SHORT).show();
           }

       });


   }
    // TODO: Add updateUI() here:

    private void updateUI(WeatherDataModel weatherData) {
        mTemperatureLabel.setText(weather.getTemperature());
        mCityLabel.setText(weather.getCity());

        int resourceID = getResources().getIdentifier(weather.getIconName(),"drawable",getPackageName());
        mWeatherImage.setImageResource(resourceID);

    }

    // TODO: Add onPause() here:

    @Override
    protected void onPause(){
        super.onPause();

        if(mLocationManager != null) mLocationManager.removeUpdates(mLocationListener);
    }
}
