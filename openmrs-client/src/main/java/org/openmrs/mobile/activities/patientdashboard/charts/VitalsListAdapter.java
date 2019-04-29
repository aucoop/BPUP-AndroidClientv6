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

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.mobile.R;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.DateUtils;
import org.openmrs.mobile.utilities.DayAxisValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.openmrs.mobile.utilities.ApplicationConstants.ChartType.BMI;
import static org.openmrs.mobile.utilities.ApplicationConstants.ChartType.DIASTOLIC;
import static org.openmrs.mobile.utilities.ApplicationConstants.ChartType.PULSE;
import static org.openmrs.mobile.utilities.ApplicationConstants.ChartType.SYSTOLIC;

public class VitalsListAdapter extends BaseExpandableListAdapter {

    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    private Context mContext;
    private List<ViewGroup> mChildLayouts;
    private JSONObject mObservationList;
    private List<String> mVitalNameList;

    public VitalsListAdapter(Context context, JSONObject observationList) throws JSONException {
        this.mContext = context;
        this.mObservationList = observationList;
        Iterator<String> keys = mObservationList.keys();
        this.mVitalNameList = Lists.newArrayList(keys);
        this.mChildLayouts = generateChildLayouts();
    }

    private List<ViewGroup> generateChildLayouts() throws JSONException {
        List<ViewGroup> layouts = new ArrayList<>();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        List<ILineDataSet> ILdataSets = new ArrayList<>();

        List<Float> systolicMinValues = new ArrayList<>();
        List<Float> diastolicValues = new ArrayList<>();
        List<Float> pulseValues = new ArrayList<>();

        ArrayList<String> currentDateList = new ArrayList<>();


        for (String vitalName : this.mVitalNameList) {
            ViewGroup convertView = (ViewGroup) inflater.inflate(R.layout.line_chart, null);

                JSONObject chartData = mObservationList.getJSONObject(vitalName);
                Iterator<String> dates = chartData.keys();
                ArrayList<String> dateList = Lists.newArrayList(dates);

                LineChart chart = (LineChart) convertView.findViewById(R.id.linechart);
                List<Entry> entries = new ArrayList<>();

                if (vitalName.equals(SYSTOLIC)){

                    List<Entry> systolicEntries = new ArrayList<>();
                    List<Entry> diastolicEntries = new ArrayList<>();
                    List<Entry> pulseEntries = new ArrayList<>();

                    for (Integer i = 0; i < dateList.size(); i++) {
                        JSONArray dataArray = chartData.getJSONArray(dateList.get(i));

                        //Look for the minimum value and save it on an array.
                        float min = Float.parseFloat((String) dataArray.get(0));
                        int indexMin = 0;
                        for (Integer j = 0; j < dataArray.length(); j++) {
                            if (Float.parseFloat((String) dataArray.get(j)) < min) {
                                min = Float.parseFloat((String) dataArray.get(j));
                                indexMin = j;
                            }
                        }

                        systolicMinValues.add(min);

                        //Add the other values according to the systolic minimum one.
                        diastolicValues.add(Float.parseFloat((String) mObservationList.getJSONObject(mVitalNameList.get(1)).getJSONArray(dateList.get(i)).get(indexMin)));
                        pulseValues.add(Float.parseFloat((String) mObservationList.getJSONObject(mVitalNameList.get(2)).getJSONArray(dateList.get(i)).get(indexMin)));
                    }


                        //Check if there is more than one measurement taken on the same day (no matter the hour)
                        int i = 0;
                        float min;
                        int indexEntry = 0;
                        String currentDate;
                        for (i = 0; i < systolicMinValues.size(); i++) {
                            boolean existsMoreThataForSameDay = false;
                            int j = 1;
                            currentDate = dateList.get(i);
                            min = systolicMinValues.get(i);

                                while ((dateList.get(i).split(" "))[0].equals((dateList.get(i + j).split(" "))[0])) {
                                    existsMoreThataForSameDay = true;

                                    if (min > systolicMinValues.get(i + j)) {
                                        min = systolicMinValues.get(i + j);
                                        currentDate = dateList.get(i + j);
                                    }

                                    j++;

                                    //if i+j is pointing out of the bounds of the array, get out of the loop
                                    if ((i+j) >= dateList.size())
                                        break;
                                }


                            //Add minimum values taking into account the day. Also add the diastolic value according to the systolic pair.
                            currentDateList.add(currentDate);

                            systolicEntries.add(new Entry(indexEntry, min));
                            diastolicEntries.add(new Entry(indexEntry, diastolicValues.get(systolicMinValues.indexOf(min))));
                            pulseEntries.add(new Entry(indexEntry, pulseValues.get(systolicMinValues.indexOf(min))));

                            if(existsMoreThataForSameDay) //Update i
                                i += j;

                            indexEntry++;

                        }

                    ILdataSets.add(new LineDataSet(systolicEntries, vitalName));
                    ILdataSets.add(new LineDataSet(diastolicEntries, mVitalNameList.get(1)));
                    ILdataSets.add(new LineDataSet(pulseEntries, mVitalNameList.get(2)));

                }

                if(vitalName.equals(DIASTOLIC)){

                    LineDataSet sysDataSet = (LineDataSet) ILdataSets.get(0);
                    LineDataSet diasDataSet = (LineDataSet) ILdataSets.get(1);

                    sysDataSet.setColor(Color.BLUE);
                    diasDataSet.setColor(Color.MAGENTA);

                    YAxis leftAxis = chart.getAxisLeft();

                    LimitLine sysLimitLine = new LimitLine(140f, "Critical Systolic Level");
                    sysLimitLine.setLineColor(Color.RED);
                    sysLimitLine.setLineWidth(2f);
                    sysLimitLine.setTextColor(Color.BLACK);
                    sysLimitLine.setTextSize(10f);
                    leftAxis.addLimitLine(sysLimitLine);

                    LimitLine diasLimitLine = new LimitLine(90f, "Critical Diastolic Level");
                    diasLimitLine.setLineColor(Color.RED);
                    diasLimitLine.setLineWidth(2f);
                    diasLimitLine.setTextColor(Color.BLACK);
                    diasLimitLine.setTextSize(10f);
                    leftAxis.addLimitLine(diasLimitLine);

                    ArrayList<ILineDataSet> sysDiasDataSet = new ArrayList<>();
                    sysDiasDataSet.add(sysDataSet);
                    sysDiasDataSet.add(diasDataSet);

                    formatXaxis(currentDateList, chart);

                    chart.setData(new LineData(sysDiasDataSet));
                }


                else if(vitalName.equals(PULSE)){

                    formatXaxis(currentDateList, chart);
                    chart.setData(new LineData(ILdataSets.get(2)));
                }

                else if(vitalName.equals(BMI)){

                    for (Integer j = 0; j < dateList.size(); j++) {
                        JSONArray dataArray = chartData.getJSONArray(dateList.get(j));
                        for (Integer i = 0; i < dataArray.length(); i++) {
                            entries.add(new Entry(j, Float.parseFloat((String) dataArray.get(i))));
                        }
                    }

                    formatXaxis(dateList, chart);
                    ILdataSets.add(new LineDataSet(entries, vitalName));
                    chart.setData(new LineData(ILdataSets.get(3)));
                }

                chart.setScaleEnabled(false);
                chart.setVisibleXRange(5,5);
                chart.getLegend().setEnabled(false);
                chart.getDescription().setEnabled(false);

                YAxis rightAxis = chart.getAxisRight();
                rightAxis.setEnabled(false);
                chart.invalidate();
                layouts.add(convertView);
        }

        return layouts;
    }


    @Override
    public int getGroupCount() {
        return mVitalNameList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mVitalNameList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildLayouts.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (null == convertView) {
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_vital_group, null);
        }
        final TextView vitalName = (TextView) rowView.findViewById(R.id.listVisitGroupVitalName);
        final TextView detailsSelector = (TextView) rowView.findViewById(R.id.listVisitGroupDetailsSelector);
        String vitalLabel = String.valueOf(mVitalNameList.get(groupPosition));
        vitalName.setText(vitalLabel);
        if (isExpanded) {
            detailsSelector.setText(mContext.getString(R.string.list_vital_selector_hide));
            bindDrawableResources(R.drawable.exp_list_hide_details, detailsSelector, RIGHT);
        } else {
            detailsSelector.setText(mContext.getString(R.string.list_vital_selector_show));
            bindDrawableResources(R.drawable.exp_list_show_details, detailsSelector, RIGHT);
        }
        return rowView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return (ViewGroup) getChild(groupPosition, childPosition);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private void bindDrawableResources(int drawableID, TextView textView, int direction) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        Drawable image = mContext.getResources().getDrawable(drawableID);
        if (direction == LEFT) {
            image.setBounds(0, 0, (int) (40 * scale + 0.5f), (int) (40 * scale + 0.5f));
            textView.setCompoundDrawablePadding((int) (13 * scale + 0.5f));
            textView.setCompoundDrawables(image, null, null, null);
        } else {
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            textView.setCompoundDrawablePadding((int) (10 * scale + 0.5f));
            textView.setCompoundDrawables(null, null, image, null);
        }
    }

    private void formatXaxis (ArrayList<String> currentDateList, LineChart chart){
        //Formatting X Axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(true);
        xAxis.setGranularity(1);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(currentDateList.size() - 1);
        xAxis.setValueFormatter(new DayAxisValueFormatter(currentDateList));
        xAxis.setSpaceMax(6);
    }
}