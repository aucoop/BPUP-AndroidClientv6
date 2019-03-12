package org.openmrs.mobile.activities.formview;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openmrs.mobile.R;
import org.openmrs.mobile.dao.ConceptDAO;
import org.openmrs.mobile.models.Concept;
import org.openmrs.mobile.models.Obscreate;
import org.openmrs.mobile.models.Observation;
import org.openmrs.mobile.models.ObservationMethods;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.StringUtils;

import java.util.List;

/**
 * Created by Hector on 21/03/2018.
 */

public class EncounterAdapter extends RecyclerView.Adapter<EncounterAdapter.EncounterData> {

    List<Observation> mObservations;
    List<Obscreate> mObsCreate;

    public EncounterAdapter(List<Observation> mObservations,List<Obscreate> mObsCreate) {
        this.mObservations = mObservations;
        this.mObsCreate = mObsCreate;
    }

    @Override
    public EncounterAdapter.EncounterData onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.encounter_list,null,false);
        return new EncounterData(view);
    }

    @Override
    public void onBindViewHolder(EncounterAdapter.EncounterData holder, int position) {
        if (mObservations != null) {
            holder.assignData(mObservations.get(position));
        } else if (mObsCreate != null)
            holder.assignData(mObsCreate.get(position));
    }

    @Override
    public int getItemCount() {
        if (mObservations != null) {
            return mObservations.size();
        } else if (mObsCreate != null)
            return mObsCreate.size();
        else
            return 0;
    }

    public class EncounterData extends RecyclerView.ViewHolder {

        TextView obsname;
        TextView obsvalue;

        public EncounterData(View itemView) {
            super(itemView);
            obsname = (TextView) itemView.findViewById(R.id.obsname);
            obsname.setEms(10);
            obsvalue = (TextView) itemView.findViewById(R.id.obsvalue);
        }

        private void textOptions(ObservationMethods obs) {
            if (obs.getConceptUuid().equals(ApplicationConstants.ConceptUuids.BMI)) {
                Float bmi = Float.valueOf(obs.getDisplayValue());
                if (bmi>40 || bmi<20) {
                    obsvalue.setTextColor(Color.RED);
                }
                else if (bmi>30) {
                    obsvalue.setTextColor(Color.rgb(255,165,0));
                }
                else if (bmi>25) {
                    obsvalue.setTextColor(Color.rgb(255,220,0));
                }
                else
                    obsvalue.setTextColor(Color.GREEN);
            }
        }

        public void assignData(Observation obs) {
            obsname.setText(obs.getDisplay());
            obsvalue.setText(obs.getDisplayValue());
            textOptions(obs);
        }

        public void assignData(Obscreate obs) {
            Concept concept = new ConceptDAO().findConceptsByUUID(obs.getConcept());
            obsname.setText(concept.getDisplay());
            obsvalue.setText(obs.getDisplayValue());
            textOptions(obs);
        }
    }
}
