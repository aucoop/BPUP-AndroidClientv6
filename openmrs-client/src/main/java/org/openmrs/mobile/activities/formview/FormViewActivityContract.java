package org.openmrs.mobile.activities.formview;

/**
 * Created by Hector on 18/04/2018.
 */

public interface FormViewActivityContract {

    public interface Presenter {
        void setAdapter(long encID, boolean sync);
    }

}
