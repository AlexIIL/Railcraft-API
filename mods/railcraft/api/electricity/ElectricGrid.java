package mods.railcraft.api.electricity;

import java.util.List;

/**
 * Created by CovertJaguar on 2/29/2016.
 */
public class ElectricGrid {

    private ElectricGrid replacement;
    private double charge;
    private int maxCharge;
    private List<IElectricGridObject> gridObjects;

    public ElectricGrid getReplacement() {
        return replacement;
    }

    public double getCharge() {
        return charge;
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public int getMaxCharge() {
        return maxCharge;
    }

    public void addCharge(double charge) {
        this.charge += charge;
    }
}
