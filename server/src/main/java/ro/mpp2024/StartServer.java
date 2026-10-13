package ro.mpp2024;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.mpp2024.repository.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class StartServer {

    private static final Logger logger = LogManager.getLogger();
    private static final int PORT = 55555;

    public static void main(String[] args) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("bd.config"));
        } catch (IOException e) {
            logger.error("Cannot find bd.config: {}", e.getMessage());
            return;
        }

        UserRepository userRepo = new UserDbRepository(props);
        CursaRepository cursaRepo = new CursaHibernateRepository();
        ParticipantRepository participantRepo = new ParticipantHibernateRepository();
        InscriereRepository inscriereRepo = new InscriereDbRepository(props, cursaRepo, participantRepo);

        IMotociclismServices services = new MotociclismServicesImpl(
                userRepo, cursaRepo, participantRepo, inscriereRepo);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server started on port {}", PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("New client connected: {}", clientSocket);
                System.out.println("New client connected: " + clientSocket);

                ClientWorker worker = new ClientWorker(services, clientSocket);
                Thread thread = new Thread(worker);
                thread.start();
            }
        } catch (IOException e) {
            logger.error("Server error: {}", e.getMessage());
        }
    }
}