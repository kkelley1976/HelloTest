package edu.wmich.cs.kkelley.hellotest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

/**
 * Created by kkelley on 4/6/2015.
 */
public class SensingTextView extends TextView implements SensorEventListener {
    public SensingTextView(Context context) {
        super(context);
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
/*
Sensor.TYPE_ORIENTATION is deprecated.
The documentation says you should use SensorManager.getOrientation() instead.
So you should read from Sensor.TYPE_ACCELEROMETER as well as from Sensor.TYPE_MAGNETIC_FIELD,
 and then call SensorManager.getRotationMatrix() and finally SensorManager.getOrientation()
  which will return you the orientation of the phone. From there if you see this diagram it is
   trivial to get the phone's orientation. This is probably what your second example does, but I
    don't know because you didn't show me what it is.
 */

    float[] mGravity;
    float[] mGeomagnetic;
    float[] mGeomagnetic_W = new float[3];

    public void onSensorChanged(SensorEvent event) {
        float orientation[] = new float[3];
        float R[] = new float[9];
        float I[] = new float[9];

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values.clone();
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values.clone();
        if (mGravity != null && mGeomagnetic != null) {

            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                SensorManager.getOrientation(R, orientation);
                //azimut = orientation[0]; // orientation contains: azimut, pitch and roll
            }
        }

        String newstr = String.format("magnetic field\nx: %f\ny:%f\nz:%f",
                new Object[] { mGeomagnetic[0],mGeomagnetic[1],mGeomagnetic[2] } );

        newstr=newstr.concat(String.format("\norientation\nazimut: %f\npitch: %f\nroll:%f\n",orientation[0],orientation[1],orientation[2]));

        mGeomagnetic_W[0] = R[0] * mGeomagnetic[0] + R[1] * mGeomagnetic[1] + R[2] * mGeomagnetic[2];
        mGeomagnetic_W[1] = R[3] * mGeomagnetic[0] + R[4] * mGeomagnetic[1] + R[5] * mGeomagnetic[2];
        mGeomagnetic_W[2] = R[6] * mGeomagnetic[0] + R[7] * mGeomagnetic[1] + R[8] * mGeomagnetic[2];

        newstr=newstr.concat(String.format("world magnetic field\nx: %f\ny:%f\nz:%f",
                new Object[] { mGeomagnetic_W[0],mGeomagnetic_W[1],mGeomagnetic_W[2] } ));

       /* not the best way
        float[] rotationMatrixFromOrientation = getRotationMatrixFromOrientation(orientation);
        newstr=newstr.concat(String.format("\norientation matrix\n%f %f %f\n%f %f %f\n%f %f %f\n",
                rotationMatrixFromOrientation[0][0],
                rotationMatrixFromOrientation[0][1],
                rotationMatrixFromOrientation[0][2],
                rotationMatrixFromOrientation[1][0],
                rotationMatrixFromOrientation[1][1],
                rotationMatrixFromOrientation[1][2],
                rotationMatrixFromOrientation[2][0],
                rotationMatrixFromOrientation[2][1],
                rotationMatrixFromOrientation[2][2],
                ));
                */
        // better way http://stackoverflow.com/questions/15315129/convert-magnetic-field-x-y-z-values-from-device-into-global-reference-frame/15317814#15317814
        //http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])
        //now, get the rotation matrix from the sensormanager
        //then, do a few dot products to put the microteslas in world coordinates
        //maybe like this http://simple.wikipedia.org/wiki/Dot_product
        //or more directly like this http://stackoverflow.com/questions/14963190/calculate-acceleration-in-reference-to-true-north/14988559#14988559
        //except with magnetic rather than accceleration like this http://stackoverflow.com/questions/15315129/convert-magnetic-field-x-y-z-values-from-device-into-global-reference-frame
        /*
_ R = rotation matrix obtained by calling getRotationMatrix
_ mGeomagnetic = accelerator vector return by sensor ( mGeomagnetic = event.values.clone )
_ mGeomagnetic_W = R * mGeomagnetic is the same acceleration vector in the world coordinate system.

_ mGeomagnetic_W is an array of dimension 3
_ mGeomagnetic_W[0] is magnetic field in X.
_ mGeomagnetic_W[1] is magnetic field in Y.

_ float[] R = new float[9];
_ float[] I = new float[9];
_ SensorManager.getRotationMatrix(R, I, gravity, magnetic);
_float [] mGeomagnetic = values.clone();
__float [] mGeomagnetic_W = new float[3];
            mGeomagnetic_W[0] = R[0] * mGeomagnetic[0] + R[1] * mGeomagnetic[1] + R[2] * mGeomagnetic[2];
            mGeomagnetic_W[1] = R[3] * mGeomagnetic[0] + R[4] * mGeomagnetic[1] + R[5] * mGeomagnetic[2];
            mGeomagnetic_W[2] = R[6] * mGeomagnetic[0] + R[7] * mGeomagnetic1] + R[8] * mGeomagnetic[2];

         */
        setText(newstr);
    }
    /*
    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }
    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }
    */
}
