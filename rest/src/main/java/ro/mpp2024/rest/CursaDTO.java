package ro.mpp2024;

public class CursaDTO {
    private int capacitateMotor;

    public CursaDTO() {}

    public CursaDTO(int capacitateMotor) {
        this.capacitateMotor = capacitateMotor;
    }

    public int getCapacitateMotor() { return capacitateMotor; }
    public void setCapacitateMotor(int c) { this.capacitateMotor = c; }
}