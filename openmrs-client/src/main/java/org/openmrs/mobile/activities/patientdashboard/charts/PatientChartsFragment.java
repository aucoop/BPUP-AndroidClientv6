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

package org.openmrs.mobile.activities.patientdashboard.charts;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardContract;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardFragment;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.models.EncounterMethods;
import org.openmrs.mobile.models.EncounterType;
import org.openmrs.mobile.models.Observation;
import org.openmrs.mobile.models.ObservationMethods;
import org.openmrs.mobile.models.Visit;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.FontsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class PatientChartsFragment extends PatientDashboardFragment implements PatientDashboardContract.ViewPatientCharts {

    private ExpandableListView mExpandableListView;
    private TextView mEmptyListView;
    private int lastExpandedPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setPresenter(mPresenter);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_patient_charts, null, false);

        mEmptyListView = (TextView) root.findViewById(R.id.vitalEmpty);
        FontsUtil.setFont(mEmptyListView, FontsUtil.OpenFonts.OPEN_SANS_BOLD);
        mExpandableListView = (ExpandableListView) root.findViewById(R.id.vitalExpList);
        mExpandableListView.setEmptyView(mEmptyListView);
        setEmptyListVisibility(false);
        return root;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // This method is intentionally empty
    }

    public static PatientChartsFragment newInstance() {
        return new PatientChartsFragment();
    }

    @Override
    public void setEmptyListVisibility(boolean visibility) {
        if (visibility) {
            mEmptyListView.setVisibility(View.VISIBLE);
        } else {
            mEmptyListView.setVisibility(View.GONE);
        }
    }


     /*for (Encounter visit : visits) {
            //List<Encounter> encounters = visit.getEncounters();
            if (!encounters.isEmpty()) {
                setEmptyListVisibility(false);*/

    @Override
    public void populateList(List<Encounter> encounters) throws JSONException {
        final String[] displayableEncounterTypes = {EncounterType.VITALSBPUP};
        final HashSet<String> displayableEncounterTypesArray =
                new HashSet<>(Arrays.asList(displayableEncounterTypes));
        JSONObject observationList = new JSONObject();
        List<EncounterMethods> encounterMethods = new ArrayList<EncounterMethods>();
        encounterMethods.addAll(encounters);
        encounterMethods.addAll(new PatientDAO().findPatientByID(String.valueOf
                (mPresenter.getPatientId())).getEncountercreates());
        for (EncounterMethods encounter : encounterMethods) {
            //String datetime = DateUtils.convertTime(encounter.getEncounterDatetime());
            String datetime = DateUtils.convertTime(encounter.getEncounterDatetime(), DateUtils.DATE_WITH_TIME_FORMAT);
            String encounterTypeDisplay = encounter.getFormName();
            if (displayableEncounterTypesArray.contains(encounterTypeDisplay)) {
                for (ObservationMethods obs : encounter.getObservationsMethods()) {
                    if (obs.getConceptUuid().equals(ApplicationConstants.ConceptUuids.BMI) ||
                            obs.getConceptUuid().equals(ApplicationConstants.ConceptUuids.DIASTOLIC)
                            || obs.getConceptUuid().equals(ApplicationConstants.ConceptUuids.SYSTOLIC) ||
                            obs.getConceptUuid().equals(ApplicationConstants.ConceptUuids.PULSE)) {
                        String observationLabel = obs.getDisplay();
                        if (observationLabel.contains(":")) {
                            observationLabel = observationLabel.substring(0, observationLabel.indexOf(':'));
                        }
                        if (observationList.has(observationLabel)) {
                            JSONObject chartData = null;
                            try {
                                chartData = observationList.getJSONObject(observationLabel);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (chartData.has(datetime)) {
                                JSONArray obsValue = null;
                                try {
                                    obsValue = chartData.getJSONArray(datetime);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                obsValue.put(obs.getDisplayValue());
                                try {
                                    chartData.put(datetime, obsValue);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                JSONArray obsValue = new JSONArray();
                                obsValue.put(obs.getDisplayValue());
                                try {
                                    chartData.put(datetime, obsValue);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        } else {
                            JSONObject chartData = new JSONObject();
                            JSONArray obsValue = new JSONArray();
                            obsValue.put(obs.getDisplayValue());
                            try {
                                chartData.put(datetime, obsValue);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                observationList.put(observationLabel, chartData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
        }

        VitalsListAdapter vitalsListAdapter = new VitalsListAdapter(this.getActivity(), observationList);
        mExpandableListView.setAdapter(vitalsListAdapter);
        mExpandableListView.setGroupIndicator(null);

    }
}
