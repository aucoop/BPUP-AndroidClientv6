/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.mobile.models;

import android.support.annotation.NonNull;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.FormService;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@Table(name = "encountercreate")
public class Encountercreate extends Model implements Serializable,EncounterMethods{

    private Gson gson=new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private Type obscreatetype = new TypeToken<List<Obscreate>>(){}.getType();

    @Column(name = "visit")
    @SerializedName("visit")
    @Expose
    private String visit;

    @Column(name = "patient")
    @SerializedName("patient")
    @Expose
    private String patient;

    @Column(name = "patientid")
    private Long patientId;

    @Column(name = "encounterType")
    @SerializedName("encounterType")
    @Expose
    private String encounterType;

    @SerializedName("form")
    @Expose
    private String formUuid;

    @Column(name = "formname")
    private String formname;

    @Column(name = "synced")
    private boolean synced=false;

    @SerializedName("obs")
    @Expose
    private List<Obscreate> observations = new ArrayList<>();

    @Column(name = "obs")
    private String obslist;

   /* @Column(name = "encounterDateTime")
    @Expose
    private String encounterDateTime;*/

    public String getFormUuid() {
        return formUuid;
    }

    @Override
    public Form getForm() {
        return FormService.getFormByUuid(formUuid);
    }

    @Override
    public String getDisplay() {
        return formname + " " + DateUtils.convertTime(getEncounterDatetime(),DateUtils.DATE_WITH_TIME_FORMAT);
    }

    @Override
    public String getFormName() {
        return formname;
    }

    @Override
    public Long getEncounterDatetime() {
        pullObslist();
        if (observations != null) {
            return observations.get(0).getObservationDateTime();
        }
        return null;
    }

    public void setFormUuid(String formUuid) {
        this.formUuid = formUuid;
    }

    public String getVisit() {
        return visit;
    }

    public void setVisit(String visit) {
        this.visit = visit;
    }


    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(String encounterType) {
        this.encounterType = encounterType;
    }

    public String getFormname() {
        return formname;
    }

    public void setFormname(String formname) {
        this.formname = formname;
    }

    public Boolean getSynced() {
        return synced;
    }

    public void setSynced(Boolean synced) {
        this.synced = synced;
    }

    public List<Obscreate> getObservations() {
        return observations;
    }

    public List<ObservationMethods> getObservationsMethods() {
        List<ObservationMethods> list = new ArrayList<ObservationMethods>();
        list.addAll(observations);
        return list;
    }


    public void setObservations(List<Obscreate> observations) {
        this.observations = observations;
    }


    public void setObslist()
    {
        this.obslist=gson.toJson(observations,obscreatetype);
    }

    public void pullObslist() {

        List<Obscreate> obscreateList=gson.fromJson(this.obslist,obscreatetype);
        this.observations=obscreateList;
    }

    /*public void setDateTime (String s) { // added by hector
        encounterDateTime = s;
    }

    public String getDateTime (String s) { // added by hector
        return this.encounterDateTime;
    }*/
}
