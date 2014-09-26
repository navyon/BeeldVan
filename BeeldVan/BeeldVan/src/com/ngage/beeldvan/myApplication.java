package com.ngage.beeldvan;

import android.app.Application;

import com.ngage.beeldvan.model.Locations;
import com.ngage.beeldvan.utilities.Utilities;

/**
 * Created by daankrijnen on 24/09/14.
 */
public class myApplication extends Application {

    private int selectedLid;
    private Locations selectedLocation;



    public void setSelectedLocation(Locations l) {
        this.selectedLocation = l;
        System.out.println("saved location = "+l.getName());
    }

    public Locations getSelectedLocation(){
        return selectedLocation;
    }

}