

/**
 * Created by daankrijnen on 25/11/14.
 *//*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ngagemedia.beeldvan.utilities;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.common.ConnectionResult;
import com.ngagemedia.beeldvan.R;

/**
 * Defines app-wide constants and utilities
 */
public final class LocationUtils {

    // Debugging tag for the application
    public static final String APPTAG = "Beeldvan";

    // Name of shared preferences repository that stores persistent state
    public static final String SHARED_PREFERENCES =
            "com.example.android.location.SHARED_PREFERENCES";

    // Key for storing the "updates requested" flag in shared preferences
    public static final String KEY_UPDATES_REQUESTED =
            "com.example.android.location.KEY_UPDATES_REQUESTED";

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    public static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

    // Create an empty string for initializing strings
    public static final String EMPTY_STRING = new String();

//    /**
//     * Get the latitude and longitude from the Location object returned by
//     * Location Services.
//     *
//     * @param currentLocation A Location object containing the current location
//     * @return The latitude and longitude of the current location, or null if no
//     * location is available.
//     */
//    public static String getLatLng(Context context, Location currentLocation) {
//        // If the location is valid
//        if (currentLocation != null) {
//
//            // Return the latitude and longitude as strings
//            return context.getString(
//                    R.string.latitude_longitude,
//                    currentLocation.getLatitude(),
//                    currentLocation.getLongitude());
//        } else {
//
//            // Otherwise, return the empty string
//            return EMPTY_STRING;
//        }
//    }

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


    /**
     * Map error codes to error messages.
     */


        public static String getErrorString(Context context, int errorCode) {

            // Get a handle to resources, to allow the method to retrieve messages.
            Resources mResources = context.getResources();

            // Define a string to contain the error message
            String errorString;

            // Decide which error message to get, based on the error code.
            switch (errorCode) {
                case ConnectionResult.DEVELOPER_ERROR:
                    errorString = mResources.getString(R.string.connection_error_misconfigured);
                    break;

                case ConnectionResult.INTERNAL_ERROR:
                    errorString = mResources.getString(R.string.connection_error_internal);
                    break;

                case ConnectionResult.INVALID_ACCOUNT:
                    errorString = mResources.getString(R.string.connection_error_invalid_account);
                    break;

                case ConnectionResult.LICENSE_CHECK_FAILED:
                    errorString = mResources.getString(R.string.connection_error_license_check_failed);
                    break;

                case ConnectionResult.NETWORK_ERROR:
                    errorString = mResources.getString(R.string.connection_error_network);
                    break;

                case ConnectionResult.RESOLUTION_REQUIRED:
                    errorString = mResources.getString(R.string.connection_error_needs_resolution);
                    break;

                case ConnectionResult.SERVICE_DISABLED:
                    errorString = mResources.getString(R.string.connection_error_disabled);
                    break;

                case ConnectionResult.SERVICE_INVALID:
                    errorString = mResources.getString(R.string.connection_error_invalid);
                    break;

                case ConnectionResult.SERVICE_MISSING:
                    errorString = mResources.getString(R.string.connection_error_missing);
                    break;

                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                    errorString = mResources.getString(R.string.connection_error_outdated);
                    break;

                case ConnectionResult.SIGN_IN_REQUIRED:
                    errorString = mResources.getString(
                            R.string.connection_error_sign_in_required);
                    break;

                default:
                    errorString = mResources.getString(R.string.connection_error_unknown);
                    break;
            }

            // Return the error message
            return errorString;
        }

}
