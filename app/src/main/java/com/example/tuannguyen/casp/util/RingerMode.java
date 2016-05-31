package com.example.tuannguyen.casp.util;

import android.widget.Switch;

/**
 * @author Tuan Nguyen
 *
 * Object that handles the silence/vibrate setting (which setting should the phone be switched to in AUTO mode)
 */
public class RingerMode {
    /**
     * The different modes
     * <p>
     *     SILENCE = Silence the phone
     *     VIBRATE = Set the phone to vibrate
     * </p>
     */
    public enum RM {
        SILENCE, VIBRATE
    }

    /**
     * The view object in the app that the user interacts with
     */
    Switch setting;

    /**
     * Constructor for RingerMode Class
     * @param newSetting Switch object that the user interacts with
     * @param defaultMode The default mode (SILENCE/VIBRATE)
     */
    public RingerMode(Switch newSetting, RM defaultMode) {
        setting = newSetting;
        switch(defaultMode) {
            case SILENCE:
                setting.setChecked(true);
                break;
            case VIBRATE:
                setting.setChecked(false);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Returns what the switch object is reading as the user selected option
     * @return The setting that the user has selected
     */
    public RM getMode() {
        if(setting.isChecked())
            return RM.SILENCE;
        else
            return RM.VIBRATE;
    }
}
