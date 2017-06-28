package MicroModel.utilities;

import java.util.Random;

public final class RandomDrawer {

    public static double drawFromGaussian(double mean, double variance) {
        Random random = new Random();
        return mean + random.nextGaussian() * variance;
    }

}
