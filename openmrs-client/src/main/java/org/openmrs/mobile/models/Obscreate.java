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

import com.activeandroid.Model;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.openmrs.mobile.dao.ConceptDAO;
import org.openmrs.mobile.utilities.DateUtils;

import java.io.Serializable;

@Table(name = "obscreate")
public class Obscreate extends Model implements Serializable,ObservationMethods {

    @SerializedName("person")
    @Expose
    private String person;
    @SerializedName("obsDatetime")
    @Expose
    private String obsDatetime;
    @SerializedName("concept")
    @Expose
    private String concept;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("encounter")
    @Expose
    private String encounter;

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getObsDatetime() {
        return obsDatetime;
    }

    public void setObsDatetime(String obsDatetime) {
        this.obsDatetime = obsDatetime;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getEncounter() {
        return encounter;
    }

    public void setEncounter(String encounter) {
        this.encounter = encounter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getConceptUuid() {
        return concept;
    }

    @Override
    public String getDisplayValue() {
        Concept c = new ConceptDAO().findConceptsByUUID(value);
        if (c != null) return c.getDisplay();
        else
        return value;
    }
    @Override
    public String getDisplay() {
        Concept conceptTmp = new ConceptDAO().findConceptsByUUID(concept);
        if (conceptTmp != null) {
            return new ConceptDAO().findConceptsByUUID(concept).getDisplay();
        }
        return "";
    }

    @Override
    public Long getObservationDateTime() {
        return DateUtils.convertTime(obsDatetime,DateUtils.DATE_WITH_TIME_FORMAT);
    }
}
