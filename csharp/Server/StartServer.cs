using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using Repository;
using Services;

namespace Server;

class StartServer
{
    static void Main(string[] args)
    {
        
        string dbPath = "Data Source=D:\\Proiecte MPP\\mpp-proiect-java-dodudaniel\\motociclism.db;Version=3;";

        var props = new Dictionary<string, string>
        {
            { "ConnectionString", dbPath }
        };

        // Creăm repository-urile în ordinea corectă
        // (InscriereDbRepository depinde de celelalte două)
        var cursaRepo = new CursaDbRepository(props);
        var participantRepo = new ParticipantDbRepository(props);
        var inscriereRepo = new InscriereDbRepository(props, cursaRepo, participantRepo);
        var userRepo = new UserDbRepository(props);

        // Creăm serviciul cu ordinea corectă a parametrilor
        var service = new MotociclismService(userRepo, cursaRepo, participantRepo, inscriereRepo);
        var servicesImpl = new MotociclismServicesImpl(service);

        var listener = new TcpListener(IPAddress.Any, 55555);
        listener.Start();
        Console.WriteLine("[Server] Server pornit pe portul 55555. Astept clienti...");

        while (true)
        {
            var client = listener.AcceptTcpClient();
            Console.WriteLine("[Server] Client nou conectat!");

            var worker = new ClientWorker(servicesImpl, client);
            var thread = new Thread(worker.Run) { IsBackground = true };
            thread.Start();
        }
    }
}