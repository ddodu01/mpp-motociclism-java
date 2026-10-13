package ro.mpp2024.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.mpp2024.Cursa;
import ro.mpp2024.Inscriere;
import ro.mpp2024.Participant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class InscriereDbRepository implements InscriereRepository {

    private static final Logger logger = LogManager.getLogger();
    private String dbUrl;
    private CursaRepository cursaRepository;
    private ParticipantRepository participantRepository;

    public InscriereDbRepository(Properties props, CursaRepository cursaRepository,
                                 ParticipantRepository participantRepository) {
        logger.info("Initializing InscriereDbRepository with properties: {}", props);
        dbUrl = props.getProperty("jdbc.url");
        this.cursaRepository = cursaRepository;
        this.participantRepository = participantRepository;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    @Override
    public Inscriere findOne(Long id) {
        logger.traceEntry("Finding inscriere with id {}", id);
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "SELECT * FROM Inscrieri WHERE id=?")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Cursa cursa = cursaRepository.findOne(rs.getLong("cursaId"));
                Participant participant = participantRepository.findOne(rs.getLong("participantId"));
                Inscriere inscriere = new Inscriere(cursa, participant);
                inscriere.setId(rs.getLong("id"));
                logger.traceExit();
                return inscriere;
            }
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return null;
    }

    @Override
    public Iterable<Inscriere> findAll() {
        logger.traceEntry("Finding all inscrieri");
        List<Inscriere> inscrieri = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM Inscrieri")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Cursa cursa = cursaRepository.findOne(rs.getLong("cursaId"));
                Participant participant = participantRepository.findOne(rs.getLong("participantId"));
                Inscriere inscriere = new Inscriere(cursa, participant);
                inscriere.setId(rs.getLong("id"));
                inscrieri.add(inscriere);
            }
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return inscrieri;
    }

    @Override
    public Inscriere save(Inscriere entity) {
        logger.traceEntry("Saving inscriere");
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "INSERT INTO Inscrieri (cursaId, participantId) VALUES (?, ?)")) {
            stmt.setLong(1, entity.getCursa().getId());
            stmt.setLong(2, entity.getParticipant().getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return entity;
    }

    @Override
    public Inscriere delete(Long id) {
        logger.traceEntry("Deleting inscriere with id {}", id);
        Inscriere inscriere = findOne(id);
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "DELETE FROM Inscrieri WHERE id=?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return inscriere;
    }

    @Override
    public Inscriere update(Inscriere entity) {
        logger.traceEntry("Updating inscriere {}", entity.getId());
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "UPDATE Inscrieri SET cursaId=?, participantId=? WHERE id=?")) {
            stmt.setLong(1, entity.getCursa().getId());
            stmt.setLong(2, entity.getParticipant().getId());
            stmt.setLong(3, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return entity;
    }
}