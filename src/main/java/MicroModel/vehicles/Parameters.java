package MicroModel.vehicles;

/**
 * Created by daniel on 22/06/17.
 */
public class Parameters {
    public double CC0;
    public double CC1;
    public double CC2;
    public double CC3;
    public double CC4;
    public double CC5;
    public double CC6;
    public double CC7;
    public double CC8;
    public double CC9;

    Parameters() {
        CC0 = 1.5; // Desired front to rear distance at standstill (m)
        CC1 = 1.3; // Spacing time (time to standstill distance at current speed) (s)
        CC2 = 4.0; // Following variation (m)
        CC3 = 12.0; // Threshold to follow - time to recognize a slower vehicle infront (s) (-12)
        CC4 = -0.25; // Negative (closing) following threshold dv (m/s)
        CC5 = 0.35; // Positive (opening) following threshold dv (m/s)
        CC6 = 6.0; // Speed dependency of oscillation rads^-1
        CC7 = 0.25; // Oscillation acceleration
        CC8 = 2.0; // Standstill acceleration
        CC9 = -1.5; // Acceleration at 80 kmh^-1
    }
}
