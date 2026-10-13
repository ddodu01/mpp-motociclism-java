package ro.mpp2024;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerProxy implements IMotociclismServices {

    private static final Logger logger = LogManager.getLogger();

    private final String host;
    private final int port;

    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Socket connection;

    private final BlockingQueue<Response> responses = new LinkedBlockingQueue<>();
    private volatile boolean finished = false;

    private IMotociclismObserver client;

    public ServerProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void initializeConnection() throws MotociclismException {
        try {
            connection = new Socket(host, port);
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            finished = false;
            startReader();
        } catch (IOException e) {
            throw new MotociclismException("Error connecting to server: " + e.getMessage());
        }
    }

    private void startReader() {
        Thread readerThread = new Thread(() -> {
            while (!finished) {
                try {
                    Response response = (Response) input.readObject();
                    logger.info("Received response: {}", response.getType());
                    if (response.getType() == ResponseType.INSCRIERE_EFECTUATA) {
                        // notificare de la server - o trimitem la client
                        try {
                            client.inscriereEfectuata((Inscriere) response.getData());
                        } catch (MotociclismException e) {
                            logger.error("Error notifying client: {}", e.getMessage());
                        }
                    } else if (response.getType() == ResponseType.ECHIPA_MODIFICATA) {
                        try {
                            client.echipaModificata((Participant) response.getData());
                        } catch (MotociclismException e) {
                            logger.error("Error notifying client: {}", e.getMessage());
                        }
                    } else {
                        responses.put(response);
                    }
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    logger.error("Error reading response: {}", e.getMessage());
                    finished = true;
                }
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void closeConnection() {
        finished = true;
        try {
            input.close();
            output.close();
            connection.close();
        } catch (IOException e) {
            logger.error("Error closing connection: {}", e.getMessage());
        }
    }

    private void sendRequest(Request request) throws MotociclismException {
        try {
            output.writeObject(request);
            output.flush();
        } catch (IOException e) {
            throw new MotociclismException("Error sending request: " + e.getMessage());
        }
    }

    private Response readResponse() throws MotociclismException {
        try {
            return responses.take();
        } catch (InterruptedException e) {
            throw new MotociclismException("Error reading response: " + e.getMessage());
        }
    }

    @Override
    public User login(String username, String password, IMotociclismObserver client)
            throws MotociclismException {
        initializeConnection();
        this.client = client;
        sendRequest(new Request(RequestType.LOGIN, new String[]{username, password}));
        Response response = readResponse();
        if (response.getType() == ResponseType.ERROR) {
            throw new MotociclismException((String) response.getData());
        }
        return (User) response.getData();
    }

    @Override
    public void logout(User user, IMotociclismObserver client) throws MotociclismException {
        sendRequest(new Request(RequestType.LOGOUT, user));
        Response response = readResponse();
        closeConnection();
        if (response.getType() == ResponseType.ERROR) {
            throw new MotociclismException((String) response.getData());
        }
    }

    @Override
    public Iterable<Cursa> getCurseDisponibile() throws MotociclismException {
        sendRequest(new Request(RequestType.GET_CURSE, null));
        Response response = readResponse();
        if (response.getType() == ResponseType.ERROR) {
            throw new MotociclismException((String) response.getData());
        }
        return (Iterable<Cursa>) response.getData();
    }

    @Override
    public Cursa getCursaById(Long id) throws MotociclismException {
        sendRequest(new Request(RequestType.GET_CURSA_BY_ID, id));
        Response response = readResponse();
        if (response.getType() == ResponseType.ERROR) {
            throw new MotociclismException((String) response.getData());
        }
        return (Cursa) response.getData();
    }

    @Override
    public Iterable<Participant> cautaParticipanti(String echipa) throws MotociclismException {
        sendRequest(new Request(RequestType.CAUTA_PARTICIPANTI, echipa));
        Response response = readResponse();
        if (response.getType() == ResponseType.ERROR) {
            throw new MotociclismException((String) response.getData());
        }
        return (Iterable<Participant>) response.getData();
    }

    @Override
    public void inscrie(Participant participant, Cursa cursa) throws MotociclismException {
        sendRequest(new Request(RequestType.INSCRIE, new Object[]{participant, cursa}));
        Response response = readResponse();
        if (response.getType() == ResponseType.ERROR) {
            throw new MotociclismException((String) response.getData());
        }
    }

    @Override
    public void modificaEchipa(Long participantId, String echipaNoua) throws MotociclismException {
        sendRequest(new Request(RequestType.MODIFICA_ECHIPA, new Object[]{participantId, echipaNoua}));
        Response response = readResponse();
        if (response.getType() == ResponseType.ERROR) {
            throw new MotociclismException((String) response.getData());
        }
    }
}