using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Net.Sockets;
using System.Text.Json;
using System.Threading;
using Model;
using Networking;

namespace Client;

public class ServerProxy : IMotociclismServices
{
    private readonly string _host;
    private readonly int _port;

    private TcpClient _client;
    private NetworkStream _stream;
    private StreamWriter _writer;
    private StreamReader _reader;

    private IMotociclismObserver? _observer;
    private volatile bool _connected = false;

    // Coada pentru raspunsuri normale (non-notificari)
    private readonly BlockingCollection<Response> _responses = new();

    public ServerProxy(string host, int port)
    {
        _host = host;
        _port = port;
    }

    private void Connect()
    {
        _client = new TcpClient(_host, _port);
        _stream = _client.GetStream();
        _reader = new StreamReader(_stream);
        _writer = new StreamWriter(_stream) { AutoFlush = true };
        _connected = true;

        // Singurul thread care citeste din stream
        var t = new Thread(ReadLoop) { IsBackground = true };
        t.Start();
    }

    private void ReadLoop()
    {
        while (_connected)
        {
            try
            {
                string? line = _reader.ReadLine();
                if (line == null) break;

                Response? response;
                try
                {
                    response = JsonSerializer.Deserialize<Response>(line, JsonUtils.Options);
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"[ServerProxy] Eroare deserializare: {ex.Message}");
                    continue;
                }

                if (response == null) continue;

                if (response.Type == ResponseType.INSCRIERE_EFECTUATA)
                {
                    var inscriere = JsonUtils.ParseData<Inscriere>(response.Data);
                    _observer?.InscriereEfectuata(inscriere!);
                }
                else if (response.Type == ResponseType.ECHIPA_MODIFICATA)
                {
                    var participant = JsonUtils.ParseData<Participant>(response.Data);
                    _observer?.EchipaModificata(participant!);
                }
                else
                {
                    _responses.Add(response);
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[ServerProxy] Eroare ReadLoop: {ex.Message}");
                break;
            }
        }
    }

    private Response SendRequest(Request request)
    {
        string json = JsonSerializer.Serialize(request, JsonUtils.Options);
        Console.WriteLine($"[ServerProxy] Trimit: {json}");
        _writer.WriteLine(json);
        Console.WriteLine("[ServerProxy] Astept raspuns...");
        var response = _responses.Take();
        Console.WriteLine($"[ServerProxy] Raspuns primit: {response.Type}");

        if (response.Type == ResponseType.ERROR)
            throw new MotociclismException(response.Data?.ToString() ?? "Eroare server");

        return response;
    }

    public User Login(string username, string password, IMotociclismObserver observer)
    {
        _observer = observer;
        Connect();

        var request = new Request(RequestType.LOGIN,
            new { username = username, password = password });
        var response = SendRequest(request);
        return JsonUtils.ParseData<User>(response.Data)!;
    }

    public void Logout(User user)
    {
        SendRequest(new Request(RequestType.LOGOUT, user));
        _connected = false;
        _client.Close();
    }

    public IEnumerable<Cursa> GetCurse()
    {
        var response = SendRequest(new Request(RequestType.GET_CURSE));
        return JsonUtils.ParseData<List<Cursa>>(response.Data) ?? new List<Cursa>();
    }

    public Cursa GetCursaById(long id)
    {
        var response = SendRequest(new Request(RequestType.GET_CURSA_BY_ID, id));
        return JsonUtils.ParseData<Cursa>(response.Data)!;
    }

    public IEnumerable<Participant> CautaParticipanti(string echipa)
    {
        var response = SendRequest(new Request(RequestType.CAUTA_PARTICIPANTI, echipa));
        return JsonUtils.ParseData<List<Participant>>(response.Data) ?? new List<Participant>();
    }

    public Inscriere Inscrie(string numePilot, string cnp, string echipa, long cursaId)
    {
        var request = new Request(RequestType.INSCRIE, new
        {
            participant = new { nume = numePilot, cnp = cnp, echipa = echipa },
            cursa = new { id = cursaId }
        });
        var response = SendRequest(request);
        return JsonUtils.ParseData<Inscriere>(response.Data)!;
    }

    public Participant ModificaEchipa(long participantId, string echipaNoua)
    {
        var request = new Request(RequestType.MODIFICA_ECHIPA,
            new { participantId = participantId, echipaNoua = echipaNoua });
        var response = SendRequest(request);
        return JsonUtils.ParseData<Participant>(response.Data)!;
    }
}