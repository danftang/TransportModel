package MicroModel.signs;

public abstract class PrototypeSign {

    public boolean stop;
    public double position;
    public int status;


    public abstract void step (double dt);

}
