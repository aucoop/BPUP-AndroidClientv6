package org.openmrs.mobile.utilities;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.openmrs.mobile.activities.formdisplay.FormDisplayPageFragment;
import org.xml.sax.Locator;

import java.util.List;

/**
 * Created by Hector on 06/04/2018.
 */



public class Gps implements LocationListener {
    private static final String TAG = "GPS.JAVA";
    static private final int TIME_INTERVAL = 0; // minimum time between updates in milliseconds
    static private final int DISTANCE_INTERVAL = 0; // minimum distance between updates in meters
    private final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;
    private Location location;
    private Context context;
    LocationManager locationManager;
    GPSListener gpsL;

    public Gps(Context context, GPSListener gps) {
        super();
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.gpsL = gps;
    }

    private void requestUpdates(String provider) {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this.context, new String[] {
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    MY_PERMISSION_ACCESS_FINE_LOCATION);
        }
        if ( Build.VERSION.SDK_INT >= 23
                &&
                (ContextCompat.checkSelfPermission( this.context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
                        ||
                        ContextCompat.checkSelfPermission( this.context, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED))

        {
            ToastUtil.error("No GPS permission granted, won't be able to calculate gps location");
            return;
        } /*permission check*/
        if (this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && provider.contentEquals(LocationManager.NETWORK_PROVIDER)) {
            Log.d(TAG, "Network connected, start listening : " + LocationManager.NETWORK_PROVIDER);
            this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_INTERVAL,DISTANCE_INTERVAL, this);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        }
        else if (this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER )&& provider.contentEquals(LocationManager.NETWORK_PROVIDER)) {
            Log.d(TAG, "Mobile network connected, start listening : " +LocationManager.GPS_PROVIDER);
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_INTERVAL,DISTANCE_INTERVAL , this);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            Log.d(TAG, "Proper network not connected for provider : " + provider);
            this.onProviderDisabled(provider);
            }

    }

    public void makeMeasurement() {
        requestUpdates(LocationManager.NETWORK_PROVIDER);
    }

    public Double getLatitude() {
        return location == null ? 0 : location.getLatitude();
    }

    public Double getLongitude() {
        return location == null ? 0 : location.getLongitude();
    }

    public void cancel() {
        this.locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location locationN) {
        Log.d(TAG, "Location changed: " + location.getLatitude() + ", " + location.getLongitude());
        this.location = locationN;
        gpsL.onGPSResult();
        this.locationManager.removeUpdates(this);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d(TAG, "Provided status changed : " + s + " : status : " + i);
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(TAG, "Provider enabled : " + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(TAG, "Provider disabled : " + s);
        if (s.contentEquals(LocationManager.NETWORK_PROVIDER)) {
            // Network provider disabled, try GPS
            Log.d(TAG, "Request updates from GPS provider, network provider disabled.");
            this.requestUpdates(LocationManager.GPS_PROVIDER);
        } else {
            this.locationManager.removeUpdates(this);

        }
    }

    public interface GPSListener {

        public void onGPSResult();

    };
}
