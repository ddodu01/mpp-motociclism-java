using Model;

namespace Networking;

public interface IMotociclismServices
{
    User Login(string username, string password, IMotociclismObserver observer);
    void Logout(User user);

    IEnumerable<Cursa> GetCurse();
    Cursa GetCursaById(long id);

    IEnumerable<Participant> CautaParticipanti(string echipa);
    Inscriere Inscrie(string numePilot, string cnp, string echipa, long cursaId);
    Participant ModificaEchipa(long participantId, string echipaNoua);
}