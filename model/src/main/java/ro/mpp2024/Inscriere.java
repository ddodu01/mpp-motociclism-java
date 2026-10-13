package ro.mpp2024;

public class Inscriere extends Entity<Long> {
    private Cursa cursa;
    private Participant participant;

    // Constructor gol pentru Jackson
    public Inscriere() {}

    public Inscriere(Cursa cursa, Participant participant) {
        this.cursa = cursa;
        this.participant = participant;
    }

    public Cursa getCursa() { return cursa; }
    public void setCursa(Cursa cursa) { this.cursa = cursa; }
    public Participant getParticipant() { return participant; }
    public void setParticipant(Participant participant) { this.participant = participant; }
}