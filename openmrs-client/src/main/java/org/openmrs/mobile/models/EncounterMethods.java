package org.openmrs.mobile.models;

import java.util.List;

/**
 * Created by Hector on 03/04/2018.
 */

public interface EncounterMethods {
    /*gets list of Observations/Obscreate as ObservationMethods*/
    public List<ObservationMethods> getObservationsMethods();

    /*returns the form of the encounter*/
    public Form getForm();

    /*returns a String formed of "encounterType + Date"*/
    public String getDisplay();

    /*returns the name of EncounterType which is the same as the formName*/
    public String getFormName();

    /*return the date of the encounter*/
    public Long getEncounterDatetime();
}