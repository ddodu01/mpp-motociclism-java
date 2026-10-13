using System.Collections.Concurrent;
using System.Security.Cryptography;
using Model;
using Networking;
using Services;

namespace Server;

public class MotociclismServicesImpl : IMotociclismServices
{
    private readonly MotociclismService _service;
    private readonly ConcurrentDictionary<string, IMotociclismObserver> _loggedClients = new();

    public MotociclismServicesImpl(MotociclismService service)
    {
        _service = service;
    }

    public User Login(string username, string password, IMotociclismObserver observer)
    {
        var user = _service.Login(username, password);
        if (_loggedClients.ContainsKey(username))
            throw new MotociclismException("Utilizator deja logat!");

        _loggedClients[username] = observer;
        Console.WriteLine($"[Server] Client logat: {username}");
        return user;
    }

    public void Logout(User user)
    {
        if (!_loggedClients.TryRemove(user.Username, out _))
            throw new MotociclismException("Utilizatorul nu era logat!");

        Console.WriteLine($"[Server] Client delogat: {user.Username}");
    }

    public IEnumerable<Cursa> GetCurse() => _service.GetCurseDisponibile();

    public Cursa GetCursaById(long id) => _service.GetCursaById(id);

    public IEnumerable<Participant> CautaParticipanti(string echipa) =>
        _service.CautaParticipanti(echipa);

    public Inscriere Inscrie(string numePilot, string cnp, string echipa, long cursaId)
    {
        var cursa = _service.GetCursaById(cursaId);
        if (cursa == null)
            throw new MotociclismException($"Cursa cu id {cursaId} nu există!");

        var participant = new Participant(numePilot, cnp, echipa);
        var inscriere = _service.Inscrie(participant, cursa);
        NotifyInscriereEfectuata(inscriere);
        return inscriere;
    }

    public Participant ModificaEchipa(long participantId, string echipaNoua)
    {
        var participant = _service.ModificaEchipa(participantId, echipaNoua);
        NotifyEchipaModificata(participant);
        return participant;
    }

    private void NotifyInscriereEfectuata(Inscriere inscriere)
    {
        foreach (var observer in _loggedClients.Values)
        {
            Task.Run(() =>
            {
                try { observer.InscriereEfectuata(inscriere); }
                catch (Exception e) { Console.WriteLine($"[Server] Eroare notificare: {e.Message}"); }
            });
        }
    }

    private void NotifyEchipaModificata(Participant participant)
    {
        foreach (var observer in _loggedClients.Values)
        {
            Task.Run(() =>
            {
                try { observer.EchipaModificata(participant); }
                catch (Exception e) { Console.WriteLine($"[Server] Eroare notificare: {e.Message}"); }
            });
        }
    }
}