package com.ngage.beeldvan;

import android.app.Application;

import com.ngage.beeldvan.model.CityData;
import com.ngage.beeldvan.model.Locations;
import com.ngage.beeldvan.utilities.Utilities;

/**
 * Created by daankrijnen on 24/09/14.
 */
public class myApplication extends Application {

    private int selectedLid;
    private int selectedCid;
    private Locations selectedLocation;




    public void setSelectedLocation(Locations l) {
        this.selectedLocation = l;
        System.out.println("saved location = "+l.getName());
    }

    public Locations getSelectedLocation(){
        return selectedLocation;
    }

    public void setSelectedLid(int l){
        this.selectedLid = l;
    }
    public int getSelectedLid(){
        return selectedLid;
    }


    public void setSelectedCid(int c){
        this.selectedCid = c;
    }
    public int getSelectedCid(){
        return selectedCid;
    }

}