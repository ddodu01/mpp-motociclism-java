package ro.mpp2024;

public interface IMotociclismServices {

    // Login/Logout
    User login(String username, String password, IMotociclismObserver client)
            throws MotociclismException;

    void logout(User user, IMotociclismObserver client)
            throws MotociclismException;

    // Curse
    Iterable<Cursa> getCurseDisponibile()
            throws MotociclismException;

    Cursa getCursaById(Long id)
            throws MotociclismException;

    // Participanti
    Iterable<Participant> cautaParticipanti(String echipa)
            throws MotociclismException;

    // Inscriere
    void inscrie(Participant participant, Cursa cursa)
            throws MotociclismException;

    // Modificare
    void modificaEchipa(Long participantId, String echipaNoua)
            throws MotociclismException;
}