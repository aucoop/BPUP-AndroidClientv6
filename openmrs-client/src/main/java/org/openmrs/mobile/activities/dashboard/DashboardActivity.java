/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.mobile.activities.dashboard;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.activities.addeditpatient.AddEditPatientActivity;
import org.openmrs.mobile.activities.lastviewedpatients.LastViewedPatientsActivity;
import org.openmrs.mobile.activities.syncedpatients.SyncedPatientsActivity;
import org.openmrs.mobile.activities.syncedpatients.SyncedPatientsFragment;
import org.openmrs.mobile.activities.syncedpatients.SyncedPatientsPresenter;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.StringUtils;

public class DashboardActivity extends ACBaseActivity {
    private static final  int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private SearchView searchView;
    private String query;
    /*TODO: Permission handling to be coded later, moving to SDK 22 for now.
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    Bundle currinstantstate;
    */
    //Menu Items
    private MenuItem mAddPatientMenuItem;

    private SyncedPatientsPresenter mPresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*TODO: Permission handling to be coded later, moving to SDK 22 for now.
        currinstantstate=savedInstanceState;*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.openmrs_action_logo);
        }

        // Create fragment
        /*DashboardFragment dashboardFragment =
                (DashboardFragment) getSupportFragmentManager().findFragmentById(R.id.dashboardContentFrame);
        if (dashboardFragment == null) {
            dashboardFragment = DashboardFragment.newInstance();
        }
        if (!dashboardFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    dashboardFragment, R.id.dashboardContentFrame);
        }

        // Create the presenter
        new DashboardPresenter(dashboardFragment);*/

        SyncedPatientsFragment syncedPatientsFragment =
                (SyncedPatientsFragment) getSupportFragmentManager().findFragmentById(R.id.dashboardContentFrame);
        if (syncedPatientsFragment == null) {
            syncedPatientsFragment = SyncedPatientsFragment.newInstance();
        }
        if (!syncedPatientsFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    syncedPatientsFragment, R.id.dashboardContentFrame);
        }

        // Create the presenter
        //new DashboardPresenter(dashboardFragment);
        // new SyncedPatientsPresenter(dashboardFragment);
        if(savedInstanceState != null){
            query = savedInstanceState.getString(ApplicationConstants.BundleKeys.PATIENT_QUERY_BUNDLE, "");
            mPresenter = new SyncedPatientsPresenter(syncedPatientsFragment, query);
        } else {
            mPresenter = new SyncedPatientsPresenter(syncedPatientsFragment);
        }


        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+
            int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.BLUETOOTH_PRIVILEGED},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }
        else {
            // Pre-Marshmallow
        }
        checkGPSStatus();



    }

    private void checkGPSStatus() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task =
                client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // startNewActivity(SyncedPatientsActivity.class);
                // All location settings are satisfied. The client can initialize
                // location requests here.
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(DashboardActivity.this,
                                    DashboardActivity.REQUEST_CHECK_SETTINGS);
                            Log.d("getGPSData.java", " RESOLUTION_REQUIRED REQUEST_CHECK_SETTINGS");
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }


    /*TODO: Permission handling to be coded later, moving to SDK 22 for now.*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkGPSStatus();

                } else {
                    // Permission Denied
                    Toast.makeText(DashboardActivity.this, "Permission Denied, Exiting", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("getGPSData.java", "onActivityResult");

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == 0) {
                Toast.makeText(DashboardActivity.this, "Location services Denied, Exiting", Toast.LENGTH_SHORT)
                        .show();
                finish();
                Log.d("getGPSData.java", "onActivityResult 0");
            }
        }
    }


    /**
     * Starts new Activity depending on which ImageView triggered it
     */
   /* private void startNewActivity(Class<? extends ACBaseActivity> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.find_locally_and_add_patients_menu, menu);

        mAddPatientMenuItem = menu.findItem(R.id.actionAddPatients);
        //enableAddPatient(OpenMRS.getInstance().getSyncState());
        enableAddPatient(true);

        // Search function
        MenuItem searchMenuItem = menu.findItem(R.id.actionSearchLocal);
        if (OpenMRS.getInstance().isRunningHoneycombVersionOrHigher()) {
            searchView = (SearchView) searchMenuItem.getActionView();
        } else {
            searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        }
        if(StringUtils.notEmpty(query)){
            searchMenuItem.expandActionView();
            searchView.setQuery(query, true);
            searchView.clearFocus();
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                mPresenter.setQuery(query);
                mPresenter.updateLocalPatientsList();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        switch (id) {
            case R.id.syncbutton:
               // enableAddPatient(OpenMRS.getInstance().getSyncState());
                break;
            case R.id.actionAddPatients:
                Intent intent = new  Intent(this, AddEditPatientActivity.class);
                // Intent intent = new Intent(this, LastViewedPatientsActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.actionSearchServer:
                Intent i = new Intent(this, LastViewedPatientsActivity.class);
                startActivity(i);
            default:
                // Do nothing
                break;
        }
        return true;
    }

    private void enableAddPatient(boolean enabled) {
        int resId = enabled ? R.drawable.ic_add : R.drawable.ic_add_disabled;
        mAddPatientMenuItem.setEnabled(enabled);
        mAddPatientMenuItem.setIcon(resId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String query = searchView.getQuery().toString();
        outState.putString(ApplicationConstants.BundleKeys.PATIENT_QUERY_BUNDLE, query);
    }
}
