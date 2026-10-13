using System.Net.Sockets;
using System.Text.Json;
using Model;
using Networking;

namespace Server;

public class ClientWorker : IMotociclismObserver
{
    private readonly MotociclismServicesImpl _services;
    private readonly TcpClient _client;
    private readonly NetworkStream _stream;
    private readonly StreamReader _reader;
    private User? _loggedUser;
    private volatile bool _connected = true;

    public ClientWorker(MotociclismServicesImpl services, TcpClient client)
    {
        _services = services;
        _client = client;
        _stream = client.GetStream();
        _reader = new StreamReader(_stream);
    }

    public void Run()
    {
        Console.WriteLine("[ClientWorker] Client conectat.");
        try
        {
            while (_connected)
            {
                string line = JsonUtils.ReceiveJson(_reader);
                var request = JsonSerializer.Deserialize<Request>(line,
                    new JsonSerializerOptions { PropertyNameCaseInsensitive = true });

                if (request == null) break;

                var response = HandleRequest(request);
                JsonUtils.SendJson(_stream, response);
            }
        }
        catch (Exception e)
        {
            Console.WriteLine($"[ClientWorker] Conexiune închisă: {e.Message}");
        }
        finally
        {
            _client.Close();
            Console.WriteLine("[ClientWorker] Client deconectat.");
        }
    }

    private Response HandleRequest(Request request)
    {
        try
        {
            return request.Type switch
            {
                RequestType.LOGIN => HandleLogin(request),
                RequestType.LOGOUT => HandleLogout(),
                RequestType.GET_CURSE => HandleGetCurse(),
                RequestType.GET_CURSA_BY_ID => HandleGetCursaById(request),
                RequestType.CAUTA_PARTICIPANTI => HandleCautaParticipanti(request),
                RequestType.INSCRIE => HandleInscrie(request),
                RequestType.MODIFICA_ECHIPA => HandleModificaEchipa(request),
                _ => new Response(ResponseType.ERROR, "Request necunoscut")
            };
        }
        catch (MotociclismException ex)
        {
            return new Response(ResponseType.ERROR, ex.Message);
        }
        catch (Exception ex)
        {
            return new Response(ResponseType.ERROR, $"Eroare server: {ex.Message}");
        }
    }

    // --- Handlere individuale ---

    private Response HandleLogin(Request request)
    {
        var data = JsonUtils.ParseData<LoginData>(request.Data);
        var user = _services.Login(data!.Username, data.Password, this);
        _loggedUser = user;
        return new Response(ResponseType.OK, user);
    }

    private Response HandleLogout()
    {
        if (_loggedUser != null)
        {
            _services.Logout(_loggedUser);
            _loggedUser = null;
            _connected = false;
        }
        return new Response(ResponseType.OK);
    }

    private Response HandleGetCurse()
    {
        var curse = _services.GetCurse().ToList();
        return new Response(ResponseType.OK, curse);
    }

    private Response HandleGetCursaById(Request request)
    {
        var id = JsonUtils.ParseData<long>(request.Data);
        var cursa = _services.GetCursaById(id);
        return new Response(ResponseType.OK, cursa);
    }

    private Response HandleCautaParticipanti(Request request)
    {
        var echipa = JsonUtils.ParseData<string>(request.Data);
        var participanti = _services.CautaParticipanti(echipa!).ToList();
        return new Response(ResponseType.OK, participanti);
    }

    private Response HandleInscrie(Request request)
    {
        var data = JsonUtils.ParseData<InscriereData>(request.Data);
        var inscriere = _services.Inscrie(data!.NumePilot, data.Cnp, data.Echipa, data.CursaId);
        return new Response(ResponseType.OK, inscriere);
    }

    private Response HandleModificaEchipa(Request request)
    {
        var data = JsonUtils.ParseData<ModificaEchipaData>(request.Data);
        var participant = _services.ModificaEchipa(data!.ParticipantId, data.EchipaNoua);
        return new Response(ResponseType.OK, participant);
    }

    // --- IMotociclismObserver (notificări push către client) ---

    public void InscriereEfectuata(Inscriere inscriere)
    {
        var notif = new Response(ResponseType.INSCRIERE_EFECTUATA, inscriere);
        JsonUtils.SendJson(_stream, notif);
    }

    public void EchipaModificata(Participant participant)
    {
        var notif = new Response(ResponseType.ECHIPA_MODIFICATA, participant);
        JsonUtils.SendJson(_stream, notif);
    }
}

// --- DTO-uri pentru deserializarea Data ---

public record LoginData(string Username, string Password);
public record InscriereData(string NumePilot, string Cnp, string Echipa, long CursaId);
public record ModificaEchipaData(long ParticipantId, string EchipaNoua);