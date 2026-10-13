package ro.mpp2024;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ClientWorker implements Runnable, IMotociclismObserver {

    private static final Logger logger = LogManager.getLogger();
    private static final ObjectMapper mapper = new ObjectMapper();

    private final IMotociclismServices services;
    private final Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private volatile boolean connected = true;

    public ClientWorker(IMotociclismServices services, Socket clientSocket) {
        this.services = services;
        this.clientSocket = clientSocket;
        try {
            //in loc de inputstream si outputstream
            input  = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
            logger.info("ClientWorker created for {}", clientSocket);
        } catch (IOException e) {
            logger.error("Error creating ClientWorker: {}", e.getMessage());
        }
    }

    @Override
    public void run() {
        while (connected) {
            try {
                String line = input.readLine();
                if (line == null) {
                    connected = false;
                    break;
                }
                // Parsam linia JSON ca request
                JsonNode root     = mapper.readTree(line);
                String typeStr    = root.get("type").asText();
                RequestType type  = RequestType.valueOf(typeStr);
                JsonNode dataNode = root.get("data");

                Response response = handleRequest(type, dataNode);
                if (response != null) {
                    sendResponse(response);
                }
            } catch (IOException e) {
                logger.error("Error reading request: {}", e.getMessage());
                connected = false;
            }
        }
        try {
            input.close();
            output.close();
            clientSocket.close();
        } catch (IOException e) {
            logger.error("Error closing connection: {}", e.getMessage());
        }
    }

    private Response handleRequest(RequestType type, JsonNode dataNode) {
        logger.traceEntry("Handling request {}", type);
        try {
            switch (type) {
                case LOGIN: {
                    String username = dataNode.get("username").asText();
                    String password = dataNode.get("password").asText();
                    User user = services.login(username, password, this);
                    return new Response(ResponseType.OK, user);
                }
                case LOGOUT: {
                    User user = mapper.treeToValue(dataNode, User.class);
                    services.logout(user, this);
                    connected = false;
                    return new Response(ResponseType.OK, null);
                }
                case GET_CURSE: {
                    Iterable<Cursa> curse = services.getCurseDisponibile();
                    return new Response(ResponseType.OK, curse);
                }
                case GET_CURSA_BY_ID: {
                    Long id = dataNode.asLong();
                    Cursa cursa = services.getCursaById(id);
                    return new Response(ResponseType.OK, cursa);
                }
                case CAUTA_PARTICIPANTI: {
                    String echipa = dataNode.asText();
                    Iterable<Participant> participanti = services.cautaParticipanti(echipa);
                    return new Response(ResponseType.OK, participanti);
                }
                case INSCRIE: {
                    Participant participant = mapper.treeToValue(dataNode.get("participant"), Participant.class);
                    Cursa cursa = mapper.treeToValue(dataNode.get("cursa"), Cursa.class);
                    // Trimitem OK inainte sa apelam inscrie (care notifica clientii)
                    Response okResponse = new Response(ResponseType.OK, null);
                    sendResponse(okResponse);
                    // Acum inscriem si notificam
                    services.inscrie(participant, cursa);
                    return null; // null = nu mai trimitem alt raspuns
                }
                case MODIFICA_ECHIPA: {
                    Long participantId = dataNode.get("participantId").asLong();
                    String echipaNoua = dataNode.get("echipaNoua").asText();
                    // Trimitem OK inainte sa notificam clientii
                    sendResponse(new Response(ResponseType.OK, null));
                    services.modificaEchipa(participantId, echipaNoua);
                    return null;
                }
                default:
                    return new Response(ResponseType.ERROR, "Unknown request type");
            }
        } catch (Exception e) {
            logger.error("Error handling request: {}", e.getMessage());
            return new Response(ResponseType.ERROR, e.getMessage());
        }
    }

    @Override
    public void inscriereEfectuata(Inscriere inscriere) throws MotociclismException {
        logger.traceEntry("Notifying client about inscriere");
        try {
            sendResponse(new Response(ResponseType.INSCRIERE_EFECTUATA, inscriere));
        } catch (Exception e) {
            logger.error("Error notifying client: {}", e.getMessage());
            throw new MotociclismException("Error notifying client: " + e.getMessage());
        }
        logger.traceExit();
    }

    @Override
    public void echipaModificata(Participant participant) throws MotociclismException {
        logger.traceEntry("Notifying client about echipa modificata");
        try {
            sendResponse(new Response(ResponseType.ECHIPA_MODIFICATA, participant));
        } catch (Exception e) {
            logger.error("Error notifying client: {}", e.getMessage());
            throw new MotociclismException("Error notifying client: " + e.getMessage());
        }
        logger.traceExit();
    }

    private void sendResponse(Response response) throws IOException {
        logger.traceEntry("Sending response {}", response.getType());
        synchronized (output) {
            // Scriem JSON pe o singura linie si flush
            String json = mapper.writeValueAsString(response);
            output.println(json);
            output.flush();
        }
        logger.traceExit();
    }
}