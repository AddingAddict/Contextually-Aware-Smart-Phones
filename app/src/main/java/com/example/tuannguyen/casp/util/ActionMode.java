package com.example.tuannguyen.casp.util;

import android.widget.Switch;

/**
 * @author Tuan Nguyen
 *
 * Object that handles the notification/auto setting
 */
public class ActionMode {
    /**
     * The different modes
     * <p>
     *     AUTO = change setting automatically
     *     MANUAL = push a notification
     * </p>
     */
    public enum AM {
        AUTO, NOTIFY
    }

    /**
     * The view object in the app that the user interacts with
     */
    Switch setting;

    /**
     * Constructor for ActionMode Class
     * @param newSetting Switch object that the user interacts with
     * @param defaultMode The default mode (AUTO/MANUAL)
     */
    public ActionMode(Switch newSetting, AM defaultMode) {
        setting = newSetting;
        switch(defaultMode) {
            case AUTO:
                setting.setChecked(true);
                break;
            case NOTIFY:
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
    public AM getMode() {
        if(setting.isChecked())
            return AM.AUTO;
        else
            return AM.NOTIFY;
    }
}
