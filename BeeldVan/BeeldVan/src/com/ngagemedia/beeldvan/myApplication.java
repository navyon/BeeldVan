package com.ngagemedia.beeldvan;

import android.app.Application;
import android.util.Log;

import com.ngagemedia.beeldvan.model.Locations;

/**
 * Created by daankrijnen on 24/09/14.
 */
public class myApplication extends Application {

    private int selectedLid;
    private int selectedCid;
    private Locations selectedLocation;




    public void setSelectedLocation(Locations l) {
        this.selectedLocation = l;
        Log.d("Location", "saved = "+l.getName());
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