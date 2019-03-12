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

package org.openmrs.mobile.activities.formlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.api.FormListService;
import org.openmrs.mobile.dao.EncounterDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.models.EncounterType;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.ApplicationConstants;

import java.util.List;

public class FormListActivity extends ACBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_form_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Intent i=new Intent(this,FormListService.class);
        startService(i);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Create fragment
        FormListFragment formListFragment =
                (FormListFragment) getSupportFragmentManager().findFragmentById(R.id.formListContentFrame);
        if (formListFragment == null) {
            formListFragment = FormListFragment.newInstance();
        }
        if (!formListFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    formListFragment, R.id.formListContentFrame);
        }

        Bundle bundle = getIntent().getExtras();
        Long mPatientID = null;
        if(bundle != null)
        {
            mPatientID = bundle.getLong(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
        }
        new FormListPresenter(formListFragment, mPatientID);
       /* Patient mPatient =new PatientDAO().findPatientByID(Long.toString(mPatientID));
        //String prova = mPatient.getEncounters();
        String encnters = mPatient.getEncounters();
        EncounterDAO eco = new EncounterDAO();
        List<Encounter> prova = eco.findEncountersByPatientID(mPatient.getUuid());
        EncounterType et= new EncounterType();
        et.setDisplay(et.VITALSBPUP);
        eco.getAllEncountersByType2(mPatientID,et);
        int OROR = prova.size();  // prueba*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }
}
