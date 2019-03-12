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

package org.openmrs.mobile.dao;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Select;

import net.sqlcipher.Cursor;

import org.openmrs.mobile.activities.formlist.FormListActivity;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.databases.DBOpenHelper;
import org.openmrs.mobile.databases.OpenMRSDBOpenHelper;
import org.openmrs.mobile.databases.tables.EncounterTable;
import org.openmrs.mobile.databases.tables.ObservationTable;
import org.openmrs.mobile.models.Encounter;
import org.openmrs.mobile.models.EncounterType;
import org.openmrs.mobile.models.Observation;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.FormService;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

import static org.openmrs.mobile.databases.DBOpenHelper.createObservableIO;

public class EncounterDAO {

    public long saveEncounter(Encounter encounter, Long visitID) {
        encounter.setVisitID(visitID);
        return new EncounterTable().insert(encounter);
    }


    public EncounterType getEncounterTypeByFormName(String formname) {
        return new Select()
                .from(EncounterType.class)
                .where("display = ?", formname)
                .executeSingle();
    }

    public void saveLastVitalsEncounter(Encounter encounter, String patientUUID) {
        if (null != encounter) {
            encounter.setPatientUUID(patientUUID);
            long oldLastVitalsEncounterID = getLastVitalsEncounterID(patientUUID);
            if (0 != oldLastVitalsEncounterID) {
                for (Observation obs: new ObservationDAO().findObservationByEncounterID(oldLastVitalsEncounterID)) {
                    new ObservationTable().delete(obs.getId());
                }
                new EncounterTable().delete(oldLastVitalsEncounterID);
            }
            ObservationDAO observationDAO = new ObservationDAO();
            long encounterID = saveEncounter(encounter, null);
            for (Observation obs : encounter.getObservations()) {
                observationDAO.saveObservation(obs, encounterID)
                        .observeOn(Schedulers.io())
                        .subscribe();
            }
        }
    }

    //added by Hector
    public void syncBPUPEncounters(Encounter encounter, String patientUUID) {
        if (null != encounter) {
            encounter.setPatientUUID(patientUUID);
            long oldBPUPEncounter = getEncounterByUUID(encounter.getUuid());
            if (0 != oldBPUPEncounter) {
                for (Observation obs: new ObservationDAO().findObservationByEncounterID(oldBPUPEncounter)) {
                    new ObservationTable().delete(obs.getId());
                }
                new EncounterTable().delete(oldBPUPEncounter);
            }
            ObservationDAO observationDAO = new ObservationDAO();
            long encounterID = saveEncounter(encounter, null);
            for (Observation obs : encounter.getObservations()) {
                observationDAO.saveObservation(obs, encounterID)
                        .observeOn(Schedulers.io())
                        .subscribe();
            }
        }
    }

    public long getLastVitalsEncounterID(String patientUUID) {
        long encounterID = 0;
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        String where = String.format("%s is NULL AND %s = ?", EncounterTable.Column.VISIT_KEY_ID, EncounterTable.Column.PATIENT_UUID);
        String[] whereArgs = new String[]{patientUUID};
        final Cursor cursor = helper.getReadableDatabase().query(EncounterTable.TABLE_NAME, null, where, whereArgs, null, null, null);
        if (null != cursor) {
            try {
                if (cursor.moveToFirst()) {
                    int id_CI = cursor.getColumnIndex(EncounterTable.Column.ID);
                    encounterID = cursor.getLong(id_CI);
                }
            } finally {
                cursor.close();
            }
        }
        return encounterID;
    }

    public Observable<Encounter> getLastVitalsEncounter(String patientUUID) {  //changed
        return createObservableIO(() -> {
            DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
            Encounter encounter = null;

            String where = String.format("%s = ? AND %s = ? ORDER BY %s DESC LIMIT 1", EncounterTable.Column.PATIENT_UUID, EncounterTable.Column.ENCOUNTER_TYPE, EncounterTable.Column.ENCOUNTER_DATETIME);
            String[] whereArgs = new String[]{patientUUID, EncounterType.VITALS};
            final Cursor cursor = helper.getReadableDatabase().query(EncounterTable.TABLE_NAME, null, where, whereArgs, null, null, null);
            if (null != cursor) {
                try {
                    if (cursor.moveToFirst()) {
                        int id_CI = cursor.getColumnIndex(EncounterTable.Column.ID);
                        int uuid_CI = cursor.getColumnIndex(EncounterTable.Column.UUID);
                        int display_CI = cursor.getColumnIndex(EncounterTable.Column.DISPLAY);
                        int datetime_CI = cursor.getColumnIndex(EncounterTable.Column.ENCOUNTER_DATETIME);
                        int formUuid_CI = cursor.getColumnIndex(EncounterTable.Column.FORM_UUID);
                        int patientUuid_CI = cursor.getColumnIndex(EncounterTable.Column.PATIENT_UUID);
                        Long id = cursor.getLong(id_CI);
                        String uuid = cursor.getString(uuid_CI);
                        String display = cursor.getString(display_CI);
                        Long datetime = cursor.getLong(datetime_CI);
                        String formUuid = cursor.getString(formUuid_CI);
                        String patientUuid = cursor.getString(patientUuid_CI);
                        encounter = new Encounter();
                        encounter.setId(id);
                        encounter.setUuid(uuid);
                        encounter.setDisplay(display);
                        encounter.setEncounterDatetime(DateUtils.convertTime(datetime, DateUtils.OPEN_MRS_REQUEST_FORMAT));
                        encounter.setEncounterType((EncounterType) new Select().from(EncounterType.class).where("display = ?", EncounterType.VITALS).executeSingle());
                        encounter.setObservations(new ObservationDAO().findObservationByEncounterID(id));
                        encounter.setForm(FormService.getFormByUuid(formUuid));
                        encounter.setPatient(new PatientDAO().findPatientByUUID(patientUuid));
                    }
                } finally {
                    cursor.close();
                }
            }
            return encounter;
        });
    }


    public Observable<Encounter> getStaticFormEncounter(String patientUUID, String encounterType) {  //changed
        return createObservableIO(() -> {
            DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
            Encounter encounter = null;

            String where = String.format("%s = ? AND %s = ? ORDER BY %s DESC LIMIT 1", EncounterTable.Column.PATIENT_UUID, EncounterTable.Column.ENCOUNTER_TYPE, EncounterTable.Column.ENCOUNTER_DATETIME);
            String[] whereArgs = new String[]{patientUUID, encounterType};
            final Cursor cursor = helper.getReadableDatabase().query(EncounterTable.TABLE_NAME, null, where, whereArgs, null, null, null);
            if (null != cursor) {
                try {
                    if (cursor.moveToFirst()) {
                        int id_CI = cursor.getColumnIndex(EncounterTable.Column.ID);
                        int uuid_CI = cursor.getColumnIndex(EncounterTable.Column.UUID);
                        int display_CI = cursor.getColumnIndex(EncounterTable.Column.DISPLAY);
                        int datetime_CI = cursor.getColumnIndex(EncounterTable.Column.ENCOUNTER_DATETIME);
                        int formUuid_CI = cursor.getColumnIndex(EncounterTable.Column.FORM_UUID);
                        int patientUuid_CI = cursor.getColumnIndex(EncounterTable.Column.PATIENT_UUID);
                        Long id = cursor.getLong(id_CI);
                        String uuid = cursor.getString(uuid_CI);
                        String display = cursor.getString(display_CI);
                        Long datetime = cursor.getLong(datetime_CI);
                        String formUuid = cursor.getString(formUuid_CI);
                        String patientUuid = cursor.getString(patientUuid_CI);
                        encounter = new Encounter();
                        encounter.setId(id);
                        encounter.setUuid(uuid);
                        encounter.setDisplay(display);
                        encounter.setEncounterDatetime(DateUtils.convertTime(datetime, DateUtils.OPEN_MRS_REQUEST_FORMAT));
                        encounter.setEncounterType((EncounterType) new Select().from(EncounterType.class).where("display = ?", EncounterType.VITALS).executeSingle());
                        encounter.setObservations(new ObservationDAO().findObservationByEncounterID(id));
                        encounter.setForm(FormService.getFormByUuid(formUuid));
                        encounter.setPatient(new PatientDAO().findPatientByUUID(patientUuid));
                    }
                } finally {
                    cursor.close();
                }
            }
            return encounter;
        });
    }

    public boolean updateEncounter(long encounterID, Encounter encounter, long visitID) {
        encounter.setVisitID(visitID);
        return new EncounterTable().update(encounterID, encounter) > 0;
    }

    public List<Encounter> findEncountersByVisitID(Long visitID) {
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        List<Encounter> encounters = new ArrayList<Encounter>();

        String where = String.format("%s = ?", EncounterTable.Column.VISIT_KEY_ID);
        String[] whereArgs = new String[]{visitID.toString()};
        final Cursor cursor = helper.getReadableDatabase().query(EncounterTable.TABLE_NAME, null, where, whereArgs, null, null, null);
        if (null != cursor) {
            try {
                while (cursor.moveToNext()) {
                    int id_CI = cursor.getColumnIndex(EncounterTable.Column.ID);
                    int uuid_CI = cursor.getColumnIndex(EncounterTable.Column.UUID);
                    int display_CI = cursor.getColumnIndex(EncounterTable.Column.DISPLAY);
                    int datetime_CI = cursor.getColumnIndex(EncounterTable.Column.ENCOUNTER_DATETIME);
                    int encounterType_CI = cursor.getColumnIndex(EncounterTable.Column.ENCOUNTER_TYPE);
                    int formUuid_CI = cursor.getColumnIndex(EncounterTable.Column.FORM_UUID);
                    Long id = cursor.getLong(id_CI);
                    String uuid = cursor.getString(uuid_CI);
                    String display = cursor.getString(display_CI);
                    Long datetime = cursor.getLong(datetime_CI);
                    String formUuid = cursor.getString(formUuid_CI);
                    String typeDisplay = cursor.getString(encounterType_CI);
                    Encounter encounter = new Encounter();
                    encounter.setEncounterType(new EncounterType(typeDisplay));
                    encounter.setId(id);
                    encounter.setVisitID(visitID);
                    encounter.setUuid(uuid);
                    encounter.setDisplay(display);
                    encounter.setEncounterDatetime(DateUtils.convertTime(datetime, DateUtils.OPEN_MRS_REQUEST_FORMAT));
                    encounter.setObservations(new ObservationDAO().findObservationByEncounterID(id));
                    encounter.setForm(FormService.getFormByUuid(formUuid));
                    encounters.add(encounter);
                }
            } finally {
                cursor.close();
            }
        }

        return encounters;
    }

    public Observable<List<Encounter>> getAllEncountersByType(Long patientID, EncounterType type) {
        return createObservableIO(() -> {
            List<Encounter> encounters = new ArrayList<Encounter>();
            DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
            String query = "SELECT e.* FROM observations AS o JOIN encounters AS e ON o.encounter_id = e._id " +
                    "JOIN visits AS v on e.visit_id = v._id WHERE v.patient_id = ? AND e.type = ? ORDER BY e.encounterDatetime DESC";
            String type1 = type.getDisplay();
            String[] whereArgs = new String[]{patientID.toString(), type1};
            final Cursor cursor = helper.getReadableDatabase().rawQuery(query, whereArgs);

            if (null != cursor) {
                try {
                    while (cursor.moveToNext()) {
                        int id_CI = cursor.getColumnIndex(EncounterTable.Column.ID);
                        int uuid_CI = cursor.getColumnIndex(EncounterTable.Column.UUID);
                        int display_CI = cursor.getColumnIndex(EncounterTable.Column.DISPLAY);
                        int datetime_CI = cursor.getColumnIndex(EncounterTable.Column.ENCOUNTER_DATETIME);
                        int formUuid_CI = cursor.getColumnIndex(EncounterTable.Column.FORM_UUID);
                        Long id = cursor.getLong(id_CI);
                        String uuid = cursor.getString(uuid_CI);
                        String display = cursor.getString(display_CI);
                        Long datetime = cursor.getLong(datetime_CI);
                        String formUuid = cursor.getString(formUuid_CI);
                        Encounter encounter = new Encounter();
                        encounter.setId(id);
                        encounter.setUuid(uuid);
                        encounter.setDisplay(display);
                        encounter.setEncounterDatetime(DateUtils.convertTime(datetime, DateUtils.OPEN_MRS_REQUEST_FORMAT));
                        encounter.setEncounterType(type);
                        encounter.setObservations(new ObservationDAO().findObservationByEncounterID(id));
                        encounter.setForm(FormService.getFormByUuid(formUuid));
                        encounters.add(encounter);
                    }
                } finally {
                    cursor.close();
                }
            }


            return encounters;
        });
    }

    public List<Encounter> getAllEncountersByType2(Long patientID, EncounterType type) {
            List<Encounter> encounters = new ArrayList<Encounter>();
            DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
            String query = "SELECT e.* FROM observations AS o JOIN encounters AS e ON o.encounter_id = e._id " +
                    "JOIN visits AS v on e.visit_id = v._id WHERE v.patient_id = ? AND e.type = ? ORDER BY e.encounterDatetime DESC";
            String type1 = type.getDisplay();
            String[] whereArgs = new String[]{patientID.toString(), type1};
            final Cursor cursor = helper.getReadableDatabase().rawQuery(query, whereArgs);

            if (null != cursor) {
                try {
                    while (cursor.moveToNext()) {
                        int id_CI = cursor.getColumnIndex(EncounterTable.Column.ID);
                        int uuid_CI = cursor.getColumnIndex(EncounterTable.Column.UUID);
                        int display_CI = cursor.getColumnIndex(EncounterTable.Column.DISPLAY);
                        int datetime_CI = cursor.getColumnIndex(EncounterTable.Column.ENCOUNTER_DATETIME);
                        int formUuid_CI = cursor.getColumnIndex(EncounterTable.Column.FORM_UUID);
                        Long id = cursor.getLong(id_CI);
                        String uuid = cursor.getString(uuid_CI);
                        String display = cursor.getString(display_CI);
                        Long datetime = cursor.getLong(datetime_CI);
                        String formUuid = cursor.getString(formUuid_CI);
                        Encounter encounter = new Encounter();
                        encounter.setId(id);
                        encounter.setUuid(uuid);
                        encounter.setDisplay(display);
                        encounter.setEncounterDatetime(DateUtils.convertTime(datetime, DateUtils.OPEN_MRS_REQUEST_FORMAT));
                        encounter.setEncounterType(type);
                        encounter.setObservations(new ObservationDAO().findObservationByEncounterID(id));
                        encounter.setForm(FormService.getFormByUuid(formUuid));
                        encounters.add(encounter);
                    }
                } finally {
                    cursor.close();
                }
            }
            return encounters;
        }

    public long getEncounterByUUID(final String encounterUUID) {
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();

        String where = String.format("%s = ?", EncounterTable.Column.UUID);
        String[] whereArgs = new String[]{encounterUUID};
        long encounterID = 0;
        final Cursor cursor = helper.getReadableDatabase().query(EncounterTable.TABLE_NAME, null, where, whereArgs, null, null, null);
        if (null != cursor) {
            try {
                if (cursor.moveToFirst()) {
                    int encounterID_CI = cursor.getColumnIndex(EncounterTable.Column.ID);
                    encounterID = cursor.getLong(encounterID_CI);
                }
            } finally {
                cursor.close();
            }
        }
        return encounterID;
    }

// added by Hector
    public List<Encounter> findEncountersByPatientID(String patientUUID) {
        DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
        List<Encounter> encounters = new ArrayList<Encounter>();

        String where = String.format("%s = ?", EncounterTable.Column.PATIENT_UUID);
        String[] whereArgs = new String[]{patientUUID};
        final Cursor cursor = helper.getReadableDatabase().query(EncounterTable.TABLE_NAME, null, where , whereArgs,
                null, null, EncounterTable.Column.ENCOUNTER_DATETIME + " DESC");
        if (null != cursor) {
            try {
                while (cursor.moveToNext()) {
                    int id_CI = cursor.getColumnIndex(EncounterTable.Column.ID);
                    int uuid_CI = cursor.getColumnIndex(EncounterTable.Column.UUID);
                    int display_CI = cursor.getColumnIndex(EncounterTable.Column.DISPLAY);
                    int datetime_CI = cursor.getColumnIndex(EncounterTable.Column.ENCOUNTER_DATETIME);
                   // int location_CI = cursor.getColumnIndex(EncounterTable.Column.)
                    int encounterType_CI = cursor.getColumnIndex(EncounterTable.Column.ENCOUNTER_TYPE);
                    int formUuid_CI = cursor.getColumnIndex(EncounterTable.Column.FORM_UUID);
                    int visit_CI = cursor.getColumnIndex(EncounterTable.Column.VISIT_KEY_ID);
                    int patientUuid_CI = cursor.getColumnIndex(EncounterTable.Column.PATIENT_UUID);
                    Long id = cursor.getLong(id_CI);
                    String uuid = cursor.getString(uuid_CI);
                    String display = cursor.getString(display_CI);
                    Long datetime = cursor.getLong(datetime_CI);
                    String formUuid = cursor.getString(formUuid_CI);
                    String typeDisplay = cursor.getString(encounterType_CI);
                    Long visitID = cursor.getLong(visit_CI);
                    String patientUuid = cursor.getString(patientUuid_CI);
                    Encounter encounter = new Encounter();
                    encounter.setEncounterType(new EncounterType(typeDisplay));
                    encounter.setId(id);
                    encounter.setVisitID(visitID);
                    encounter.setUuid(uuid);
                    encounter.setDisplay(display);
                    encounter.setEncounterDatetime(DateUtils.convertTime(datetime, DateUtils.OPEN_MRS_REQUEST_FORMAT));
                    encounter.setObservations(new ObservationDAO().findObservationByEncounterID(id));
                    encounter.setForm(FormService.getFormByUuid(formUuid));
                    encounter.setPatientUUID(patientUuid);
                    encounters.add(encounter);
                }
            } finally {
                cursor.close();
            }
        }

        return encounters;
    }


    // added by Hector
    public Observable<List<Encounter>> findEncountersByPatientUuid(String patientUUID) {
        return createObservableIO(() -> {
            return findEncountersByPatientID(patientUUID);
        });
    }

    public Observable<Boolean> deleteEncounterByPatientUUID(String uuid) { //added by hector
        return createObservableIO(() -> {
            OpenMRS.getInstance().getOpenMRSLogger().w("Encounters deleted for patient with UUID: " + uuid);
            DBOpenHelper openHelper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
            openHelper.getReadableDatabase().delete(EncounterTable.TABLE_NAME, EncounterTable.Column.PATIENT_UUID
                    + " = " + uuid, null);
            return true;
        });
    }
    public Encounter getStaticFormEncounter1(String patientUUID, String encounterType) {  //changed added by hector
            DBOpenHelper helper = OpenMRSDBOpenHelper.getInstance().getDBOpenHelper();
            Encounter encounter = null;

            String where = String.format("%s = ? AND %s = ? ORDER BY %s DESC LIMIT 1",
                    EncounterTable.Column.PATIENT_UUID, EncounterTable.Column.ENCOUNTER_TYPE, EncounterTable.Column.ENCOUNTER_DATETIME);
            String[] whereArgs = new String[]{patientUUID, encounterType};
            final Cursor cursor = helper.getReadableDatabase().query(EncounterTable.TABLE_NAME,
                    null, where, whereArgs, null, null, null);
            if (null != cursor) {
                try {
                    if (cursor.moveToFirst()) {
                        int id_CI = cursor.getColumnIndex(EncounterTable.Column.ID);
                        int uuid_CI = cursor.getColumnIndex(EncounterTable.Column.UUID);
                        int display_CI = cursor.getColumnIndex(EncounterTable.Column.DISPLAY);
                        int datetime_CI = cursor.getColumnIndex(EncounterTable.Column.ENCOUNTER_DATETIME);
                        int formUuid_CI = cursor.getColumnIndex(EncounterTable.Column.FORM_UUID);
                        int patientUuid_CI = cursor.getColumnIndex(EncounterTable.Column.PATIENT_UUID);
                        Long id = cursor.getLong(id_CI);
                        String uuid = cursor.getString(uuid_CI);
                        String display = cursor.getString(display_CI);
                        Long datetime = cursor.getLong(datetime_CI);
                        String formUuid = cursor.getString(formUuid_CI);
                        String patientUuid = cursor.getString(patientUuid_CI);
                        encounter = new Encounter();
                        encounter.setId(id);
                        encounter.setUuid(uuid);
                        encounter.setDisplay(display);
                        encounter.setEncounterDatetime(DateUtils.convertTime(datetime, DateUtils.OPEN_MRS_REQUEST_FORMAT));
                        encounter.setEncounterType((EncounterType) new Select().from(EncounterType.class).where("display = ?", EncounterType.VITALS).executeSingle());
                        encounter.setObservations(new ObservationDAO().findObservationByEncounterID(id));
                        encounter.setForm(FormService.getFormByUuid(formUuid));
                        encounter.setPatient(new PatientDAO().findPatientByUUID(patientUuid));
                    }
                } finally {
                    cursor.close();
                }
            }
            return encounter;
        }



}
