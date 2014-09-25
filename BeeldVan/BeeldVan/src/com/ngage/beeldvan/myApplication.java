package com.ngage.beeldvan;

import android.app.Application;

import com.ngage.beeldvan.model.Locations;

/**
 * Created by daankrijnen on 24/09/14.
 */
public class myApplication extends Application {

    private int selectedLid = 0;
    private Locations selectedLocation;

    public int getSelectedLid() {
        System.out.println("selectedLocation " + selectedLid);
        return selectedLid;
    }

    public void setSelectedLid(int temp) {
        this.selectedLid = temp;
    }


    public void setSelectedLocation(Locations l) {
        this.selectedLocation = l;
    }

    public Locations getSelectedLocation(){
        return selectedLocation;
    }

}