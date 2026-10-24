package org.edupage.brighperc.a48175.chargehub;

public class Slot {
    private int slotNumber;
    private boolean isOccupied;

    public Slot(int slotNumber, boolean isOccupied) {
        this.slotNumber = slotNumber;
        this.isOccupied = isOccupied;
    }

    public int getSlotNumber() { return slotNumber; }
    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }
}