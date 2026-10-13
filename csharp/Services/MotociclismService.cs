using System.Collections.Generic;
using log4net;
using Model;
using Repository;

namespace Services;

public class MotociclismService
{
    private static readonly ILog log = LogManager.GetLogger(typeof(MotociclismService));

    private readonly IUserRepository userRepository;
    private readonly ICursaRepository cursaRepository;
    private readonly IParticipantRepository participantRepository;
    private readonly IInscriereRepository inscriereRepository;

    public MotociclismService(IUserRepository userRepository,
        ICursaRepository cursaRepository,
        IParticipantRepository participantRepository,
        IInscriereRepository inscriereRepository)
    {
        this.userRepository = userRepository;
        this.cursaRepository = cursaRepository;
        this.participantRepository = participantRepository;
        this.inscriereRepository = inscriereRepository;
    }

    public User Login(string username, string password)
    {
        log.Debug($"Login attempt for user {username}");
        var user = userRepository.FindByUsername(username);
        if (user != null && user.Password == password)
        {
            log.Debug("Login successful");
            return user;
        }
        log.Debug("Login failed");
        return null;
    }

    public IEnumerable<Cursa> GetCurseDisponibile() => cursaRepository.FindAll();

    public Cursa GetCursaById(long id) => cursaRepository.FindOne(id);

    public IEnumerable<Participant> CautaParticipanti(string echipa) =>
        participantRepository.FindByEchipa(echipa);

    public Inscriere Inscrie(Participant participant, Cursa cursa)
    {
        var savedParticipant = participantRepository.Save(participant);
        var inscriere = new Inscriere(cursa, savedParticipant);
        inscriereRepository.Save(inscriere);
        return inscriere;
    }

    public Participant ModificaEchipa(long participantId, string echipaNoua)
    {
        var participant = participantRepository.FindOne(participantId);
        if (participant == null) return null;
        participant.Echipa = echipaNoua;
        participantRepository.Update(participant);
        return participant;
    }
}