package com.ngage.beeldvan;

import android.app.Application;

/**
 * Created by daankrijnen on 24/09/14.
 */
public class myApplication extends Application {

    private int selectedLocation = 0;

    public int getSelectedLocation() {
        System.out.println("selectedLocation " +selectedLocation);
        return selectedLocation;
    }

    public void setSelectedLocation(int temp) {
        this.selectedLocation = temp;
    }
}