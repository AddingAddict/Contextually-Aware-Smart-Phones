package com.example.tuannguyen.casp.util;

import android.support.v4.app.NotificationCompat;
import android.widget.EditText;

/**
 * @author Tuan Nguyen
 *
 * Object that handles the vibration notification settings
 */
public class VibrationNotification {
    /**
     * EditText object that contains the number of notification buzzes
     */
    EditText numBuzzes;

    /**
     * EditText object that contains the length of each notification buzz
     */
    EditText lenBuzzes;

    /**
     * EditText object that contains the length between each notification buzz
     */
    EditText lenSilence;

    /**
     * Default number of buzzes given to constructor
     */
    int defNumBuzzes;

    /**
     * Default length per buzz given to constructor
     */
    int defLenBuzzes;

    /**
     * Default length between buzzes given to constructor
     */
    int defLenSilence;

    /**
     * Constructor for the VibrationNofication object
     * @param newNumBuzzes EditText object that contains the number of buzzes the user wants during a notification
     * @param newLenBuzzes EditText object that contains the length of each buzz the user wants
     * @param newLenSilence EditText object that contains the length of time between each buzz the user wants
     * @param defaultNumBuzzes The default number of buzzes
     * @param defaultLenBuzzes The default length (in milliseconds) per buzz
     * @param defaultLenSilence The default length (in milliseconds) between buzzes
     */
    public VibrationNotification(EditText newNumBuzzes, EditText newLenBuzzes, EditText newLenSilence,
                                 int defaultNumBuzzes, int defaultLenBuzzes, int defaultLenSilence) {
        numBuzzes = newNumBuzzes;
        numBuzzes.setText(Integer.toString(defaultNumBuzzes));
        lenBuzzes = newLenBuzzes;
        lenBuzzes.setText(Integer.toString(defaultLenBuzzes));
        lenSilence = newLenSilence;
        lenSilence.setText(Integer.toString(defaultLenSilence));

        defNumBuzzes = defaultNumBuzzes;
        defLenBuzzes = defaultLenBuzzes;
        defLenSilence = defaultLenSilence;
    }

    public void setVibrationPattern(NotificationCompat.Builder nBuilder) {
        int numBuzz;
        if (numBuzzes.getText().toString().equals("")) {
            numBuzz = defNumBuzzes;
        } else {
            numBuzz = Integer.parseInt(
                    numBuzzes.getText().toString());
        }

        int lenBuzz;
        if (lenBuzzes.getText().toString().equals("")) {
            lenBuzz = defLenBuzzes;
        } else {
            lenBuzz = Integer.parseInt(
                    lenBuzzes.getText().toString());
        }

        int lenSilent;
        if (lenSilence.getText().toString().equals("")) {
            lenSilent = defLenSilence;
        } else {
            lenSilent = Integer.parseInt(
                    lenSilence.getText().toString());
        }

        long[] buzzPattern = new long[2 * numBuzz + 1];

        buzzPattern[0] = lenSilent;
        for (int i = 0; i < numBuzz; i++) {
            buzzPattern[2 * i + 1] = lenBuzz;
            buzzPattern[2 * i + 2] = lenSilent;
        }

        nBuilder.setVibrate(buzzPattern);
    }
}
