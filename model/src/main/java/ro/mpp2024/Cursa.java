package ro.mpp2024;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "Curse")
public class Cursa extends ro.mpp2024.Entity<Long> {
    @Column(name = "capacitateMotor")
    private int capacitateMotor;

    public Cursa() {}

    public Cursa(int capacitateMotor) {
        this.capacitateMotor = capacitateMotor;
    }

    public int getCapacitateMotor() { return capacitateMotor; }
    public void setCapacitateMotor(int capacitateMotor) { this.capacitateMotor = capacitateMotor; }
}