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

package org.openmrs.mobile.activities.patientdashboard.encounters;

import org.openmrs.mobile.activities.patientdashboard.PatientDashboardContract;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardMainPresenterImpl;
import org.openmrs.mobile.api.retrofit.VisitApi;
import org.openmrs.mobile.dao.EncounterDAO;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.listeners.retrofit.DefaultResponseCallbackListener;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.NetworkUtils;

import rx.android.schedulers.AndroidSchedulers;

public class PatientDashboardEncountersPresenter extends PatientDashboardMainPresenterImpl implements PatientDashboardContract.PatientVisitsPresenter {

    private PatientDashboardContract.ViewPatientEncounters mPatientEncountersView;
    private EncounterDAO encounterDAO;
    private VisitApi visitApi;

    public PatientDashboardEncountersPresenter(String id, PatientDashboardContract.ViewPatientEncounters mPatientEncountersView) {
        this.mPatient = new PatientDAO().findPatientByID(id);
        this.mPatientEncountersView = mPatientEncountersView;
        this.mPatientEncountersView.setPresenter(this);
        this.encounterDAO = new EncounterDAO();
        this.visitApi = new VisitApi();
    }

    public PatientDashboardEncountersPresenter(Patient patient,
                                               PatientDashboardContract.ViewPatientEncounters mPatientEncountersView,
                                               EncounterDAO encounterDAO,
                                               VisitApi visitApi) {
        this.mPatient = patient;
        this.mPatientEncountersView = mPatientEncountersView;
        this.visitApi = visitApi;
        this.encounterDAO = encounterDAO;
        this.mPatientEncountersView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        addSubscription(encounterDAO.findEncountersByPatientUuid(mPatient.getUuid())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(patientEncounters -> {
                    if (patientEncounters !=null && patientEncounters.isEmpty() && mPatient.getEncountercreates().isEmpty()) {
                        mPatientEncountersView.toggleRecyclerListVisibility(false);
                    }
                    else {
                        mPatientEncountersView.toggleRecyclerListVisibility(true);
                        mPatientEncountersView.setEncountersToDisplay(patientEncounters,mPatient.getEncountercreates());
                    }
                }));
        //getEncounterFromDB();
       // getEncounterFromServer();
    }


    public void getEncounterFromDB(){
        encounterDAO.findEncountersByPatientUuid(mPatient.getUuid())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(patientEncounters -> {
                    if (patientEncounters !=null && patientEncounters.isEmpty() && mPatient.getEncountercreates().isEmpty()) {
                        mPatientEncountersView.toggleRecyclerListVisibility(false);
                    }
                    else {
                        mPatientEncountersView.toggleRecyclerListVisibility(true);
                        mPatientEncountersView.setEncountersToDisplay(patientEncounters,mPatient.getEncountercreates());
                    }
                });
    }

    public void getEncounterFromServer(){
        if (NetworkUtils.isOnline()) {
            new VisitApi().syncBPUPEncounters(mPatient.getUuid(), new DefaultResponseCallbackListener() {
                @Override
                public void onResponse() {
                    getEncounterFromDB();
                }

                @Override
                public void onErrorResponse(String errorMessage) {
                    mPatientEncountersView.showErrorToast(errorMessage);
                }
            });
        }
    }

    @Override
    public void showStartVisitDialog() {
      /*  addSubscription(encounterDAO.getActiveVisitByPatientId(mPatient.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(visit -> {
                    if(visit != null){
                        mPatientEncountersView.showStartVisitDialog(false);
                    } else if (!NetworkUtils.isOnline()) {
                        mPatientEncountersView.showErrorToast("Cannot start a visit manually in offline mode." +
                                "If you want to add encounters please do so in the Form Entry section, " +
                                "they will be synced with an automatic new visit.");
                    } else {
                        mPatientEncountersView.showStartVisitDialog(true);
                    }
                }));*/
    }

    @Override
    public void syncVisits() {
        /*
        mPatientEncountersView.showStartVisitProgressDialog();
        visitApi.syncVisitsData(mPatient, new DefaultResponseCallbackListener() {
            @Override
            public void onResponse() {
                addSubscription(encounterDAO.getVisitsByPatientID(mPatient.getId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(visList -> {
                            mPatientEncountersView.dismissCurrentDialog();
                            mPatientEncountersView.setEncountersToDisplay(visList);
                            showStartVisitDialog();
                        }));
            }
            @Override
            public void onErrorResponse(String errorMessage) {
                mPatientEncountersView.dismissCurrentDialog();
                mPatientEncountersView.showErrorToast(errorMessage);
            }
        });*/
    }

    @Override
    public void startVisit() {
        /*
        mPatientEncountersView.showStartVisitProgressDialog();
        visitApi.startVisit(mPatient, new StartVisitResponseListenerCallback() {
            @Override
            public void onStartVisitResponse(long id) {
                mPatientEncountersView.goToFormViewActivity(id);
                mPatientEncountersView.dismissCurrentDialog();
            }
            @Override
            public void onResponse() {
                // This method is intentionally empty
            }
            @Override
            public void onErrorResponse(String errorMessage) {
                mPatientEncountersView.showErrorToast(errorMessage);
                mPatientEncountersView.dismissCurrentDialog();
            }
        });*/
    }
}
