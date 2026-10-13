package ro.mpp2024;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.mpp2024.repository.CursaRepository;
import ro.mpp2024.repository.InscriereRepository;
import ro.mpp2024.repository.ParticipantRepository;
import ro.mpp2024.repository.UserRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MotociclismServicesImpl implements IMotociclismServices {

    private static final Logger logger = LogManager.getLogger();

    private final UserRepository userRepository;
    private final CursaRepository cursaRepository;
    private final ParticipantRepository participantRepository;
    private final InscriereRepository inscriereRepository;

    // tinem evidenta clientilor conectati: user -> observer
    private final Map<String, IMotociclismObserver> loggedClients = new ConcurrentHashMap<>();

    public MotociclismServicesImpl(UserRepository userRepository,
                                   CursaRepository cursaRepository,
                                   ParticipantRepository participantRepository,
                                   InscriereRepository inscriereRepository) {
        this.userRepository = userRepository;
        this.cursaRepository = cursaRepository;
        this.participantRepository = participantRepository;
        this.inscriereRepository = inscriereRepository;
    }

    @Override
    public User login(String username, String password, IMotociclismObserver client)
            throws MotociclismException {
        logger.traceEntry("Login attempt for user {}", username);
        User user = userRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new MotociclismException("Username sau parola incorecte!");
        }
        if (loggedClients.containsKey(username)) {
            throw new MotociclismException("Userul este deja logat!");
        }
        loggedClients.put(username, client);
        logger.traceExit();
        return user;
    }

    @Override
    public void logout(User user, IMotociclismObserver client)
            throws MotociclismException {
        logger.traceEntry("Logout for user {}", user.getUsername());
        IMotociclismObserver localClient = loggedClients.remove(user.getUsername());
        if (localClient == null) {
            throw new MotociclismException("Userul nu este logat!");
        }
        logger.traceExit();
    }

    @Override
    public Iterable<Cursa> getCurseDisponibile() throws MotociclismException {
        logger.traceEntry();
        Iterable<Cursa> curse = cursaRepository.findAll();
        logger.traceExit();
        return curse;
    }

    @Override
    public Cursa getCursaById(Long id) throws MotociclismException {
        logger.traceEntry();
        Cursa cursa = cursaRepository.findOne(id);
        logger.traceExit();
        return cursa;
    }

    @Override
    public Iterable<Participant> cautaParticipanti(String echipa) throws MotociclismException {
        logger.traceEntry();
        Iterable<Participant> participanti = participantRepository.findByEchipa(echipa);
        logger.traceExit();
        return participanti;
    }

    @Override
    public void inscrie(Participant participant, Cursa cursa) throws MotociclismException {
        logger.traceEntry("Inscribing participant {} to cursa {}", participant.getNume(), cursa.getId());
        Participant savedParticipant = participantRepository.save(participant);
        Inscriere inscriere = new Inscriere(cursa, savedParticipant);
        inscriereRepository.save(inscriere);
        // notificam toti clientii conectati
        notifyClients(inscriere);
        logger.traceExit();
    }

    private void notifyClientsModificare(Participant participant) {
        logger.traceEntry("Notifying {} clients about modificare", loggedClients.size());
        for (IMotociclismObserver client : loggedClients.values()) {
            try {
                client.echipaModificata(participant);
            } catch (MotociclismException e) {
                logger.error("Error notifying client: {}", e.getMessage());
            }
        }
        logger.traceExit();
    }


    @Override
    public void modificaEchipa(Long participantId, String echipaNoua) throws MotociclismException {
        logger.traceEntry();
        Participant participant = participantRepository.findOne(participantId);
        if (participant == null) {
            throw new MotociclismException("Participantul nu exista!");
        }
        participant.setEchipa(echipaNoua);
        participantRepository.update(participant);
        notifyClientsModificare(participant); // adauga notificarea
        logger.traceExit();
    }

    private void notifyClients(Inscriere inscriere) {
        logger.traceEntry("Notifying {} clients", loggedClients.size());
        for (IMotociclismObserver client : loggedClients.values()) {
            try {
                client.inscriereEfectuata(inscriere);
            } catch (MotociclismException e) {
                logger.error("Error notifying client: {}", e.getMessage());
            }
        }
        logger.traceExit();
    }
}