package ro.mpp2024;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "Participanti")
public class Participant extends ro.mpp2024.Entity<Long> {
    @Column(name = "nume")
    private String nume;

    @Column(name = "cnp")
    private String cnp;

    @Column(name = "echipa")
    private String echipa;

    public Participant() {}

    public Participant(String nume, String cnp, String echipa) {
        this.nume = nume;
        this.cnp = cnp;
        this.echipa = echipa;
    }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }
    public String getCnp() { return cnp; }
    public void setCnp(String cnp) { this.cnp = cnp; }
    public String getEchipa() { return echipa; }
    public void setEchipa(String echipa) { this.echipa = echipa; }
}