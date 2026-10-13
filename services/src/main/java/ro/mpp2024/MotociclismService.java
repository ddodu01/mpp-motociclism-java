package ro.mpp2024;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.mpp2024.repository.CursaRepository;
import ro.mpp2024.repository.InscriereRepository;
import ro.mpp2024.repository.ParticipantRepository;
import ro.mpp2024.repository.UserRepository;

public class MotociclismService {

    private static final Logger logger = LogManager.getLogger();

    private final UserRepository userRepository;
    private final CursaRepository cursaRepository;
    private final ParticipantRepository participantRepository;
    private final InscriereRepository inscriereRepository;

    public MotociclismService(UserRepository userRepository,
                              CursaRepository cursaRepository,
                              ParticipantRepository participantRepository,
                              InscriereRepository inscriereRepository) {
        this.userRepository = userRepository;
        this.cursaRepository = cursaRepository;
        this.participantRepository = participantRepository;
        this.inscriereRepository = inscriereRepository;
    }

    public User login(String username, String password) {
        logger.traceEntry("Login attempt for user {}", username);
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            logger.traceExit();
            return user;
        }
        logger.traceExit();
        return null;
    }

    public Iterable<Cursa> getCurseDisponibile() {
        logger.traceEntry("Getting all cursa");
        Iterable<Cursa> curse = cursaRepository.findAll();
        logger.traceExit();
        return curse;
    }

    public Iterable<Participant> cautaParticipanti(String echipa) {
        logger.traceEntry("Searching participants for echipa {}", echipa);
        Iterable<Participant> participanti = participantRepository.findByEchipa(echipa);
        logger.traceExit();
        return participanti;
    }

    public Inscriere inscrie(Participant participant, Cursa cursa) {
        logger.traceEntry("Inscribing participant {} to cursa {}",
                participant.getNume(), cursa.getId());
        Participant savedParticipant = participantRepository.save(participant);
        Inscriere inscriere = new Inscriere(cursa, savedParticipant);
        inscriereRepository.save(inscriere);
        logger.traceExit();
        return inscriere;
    }

    public Participant modificaEchipa(Long participantId, String echipaNoua) {
        logger.traceEntry("Modifying echipa for participant {}", participantId);
        Participant participant = participantRepository.findOne(participantId);
        if (participant != null) {
            participant.setEchipa(echipaNoua);
            participantRepository.update(participant);
        }
        logger.traceExit();
        return participant;
    }

    public Cursa getCursaById(Long id) {
        logger.traceEntry("Getting cursa by id {}", id);
        Cursa cursa = cursaRepository.findOne(id);
        logger.traceExit();
        return cursa;
    }
}