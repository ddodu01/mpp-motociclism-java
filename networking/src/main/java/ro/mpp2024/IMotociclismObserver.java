package ro.mpp2024;

public interface IMotociclismObserver {
    void inscriereEfectuata(Inscriere inscriere) throws MotociclismException;
    void echipaModificata(Participant participant) throws MotociclismException;
}