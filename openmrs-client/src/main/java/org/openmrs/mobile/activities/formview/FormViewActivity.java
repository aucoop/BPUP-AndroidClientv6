package org.openmrs.mobile.activities.formview;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.utilities.ApplicationConstants;

/**
 * Created by Hector on 21/03/2018.
 */

public class FormViewActivity extends ACBaseActivity {

    RecyclerView recycler;
    long encID;
    Boolean sync;
    FormViewActivityPresenter mFormViewActivityPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_encounter_viewer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recycler = (RecyclerView) findViewById(R.id.encounter_recycle_view);
        recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        Bundle bundle = getIntent().getExtras();
        encID = bundle.getLong(ApplicationConstants.BundleKeys.ENCOUNTER_ID);
        sync = bundle.getBoolean(ApplicationConstants.BundleKeys.ENCOUNTER_SYNC);
        mFormViewActivityPresenter = new FormViewActivityPresenter(recycler);
        mFormViewActivityPresenter.setAdapter(encID,sync);

    }

}
