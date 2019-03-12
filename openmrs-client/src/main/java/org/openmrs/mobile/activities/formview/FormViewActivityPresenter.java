package org.openmrs.mobile.activities.formview;

import android.support.v7.widget.RecyclerView;

import org.openmrs.mobile.dao.ObservationDAO;
import org.openmrs.mobile.models.Encountercreate;

/**
 * Created by Hector on 18/04/2018.
 */

public class FormViewActivityPresenter implements FormViewActivityContract.Presenter {

    EncounterAdapter mAdapter;
    RecyclerView mRecycle;

    public FormViewActivityPresenter (RecyclerView rec){
        this.mRecycle=rec;
    }

    @Override
    public void setAdapter(long encID, boolean sync) {
         ObservationDAO observationDAO = new ObservationDAO();
         if (sync)
            mAdapter = new EncounterAdapter(observationDAO.
                    findObservationByEncounterID(encID),null);
        else {
            Encountercreate encounter = new Encountercreate().load(Encountercreate.class, encID);
            encounter.pullObslist();
            mAdapter = new EncounterAdapter(null,encounter.getObservations());
        }
        mRecycle.setAdapter(mAdapter);
    }
}
