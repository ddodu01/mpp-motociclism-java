package ro.mpp2024.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.mpp2024.Participant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ParticipantDbRepository implements ParticipantRepository {

    private static final Logger logger = LogManager.getLogger();
    private String dbUrl;

    public ParticipantDbRepository(Properties props) {
        logger.info("Initializing ParticipantDbRepository with properties: {} ", props);
        dbUrl = props.getProperty("jdbc.url");
    }

    private Connection getConnection() throws SQLException {
        logger.traceEntry("Getting database connection");
        return DriverManager.getConnection(dbUrl);
    }

    @Override
    public Participant save(Participant entity) {
        logger.traceEntry("Saving participant {}", entity.getNume());
        try (Connection con = getConnection();
             PreparedStatement preStmt = con.prepareStatement(
                     "INSERT INTO Participanti (nume, cnp, echipa) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            preStmt.setString(1, entity.getNume());
            preStmt.setString(2, entity.getCnp());
            preStmt.setString(3, entity.getEchipa());
            preStmt.executeUpdate();
            ResultSet rs = preStmt.getGeneratedKeys();
            if (rs.next()) {
                entity.setId(rs.getLong(1)); // seteaza id-ul generat
            }
        } catch (SQLException ex) {
            logger.error("Error DB: {}", ex.getMessage());
        }
        logger.traceExit();
        return entity;
    }

    @Override
    public Participant findOne(Long id) {
        logger.traceEntry("Finding participant with id {}", id);
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "SELECT * FROM Participanti WHERE id=?")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Participant p = new Participant(
                        rs.getString("nume"),
                        rs.getString("cnp"),
                        rs.getString("echipa"));
                p.setId(rs.getLong("id"));
                logger.traceExit();
                return p;
            }
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return null;
    }

    @Override
    public Iterable<Participant> findAll() {
        logger.traceEntry("Finding all participants");
        List<Participant> participanti = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "SELECT * FROM Participanti")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Participant p = new Participant(
                        rs.getString("nume"),
                        rs.getString("cnp"),
                        rs.getString("echipa"));
                p.setId(rs.getLong("id"));
                participanti.add(p);
            }
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return participanti;
    }

    @Override
    public Participant delete(Long id) {
        logger.traceEntry("Deleting participant with id {}", id);
        Participant p = findOne(id);
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "DELETE FROM Participanti WHERE id=?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return p;
    }

    @Override
    public Participant update(Participant entity) {
        logger.traceEntry("Updating participant {}", entity.getId());
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "UPDATE Participanti SET nume=?, cnp=?, echipa=? WHERE id=?")) {
            stmt.setString(1, entity.getNume());
            stmt.setString(2, entity.getCnp());
            stmt.setString(3, entity.getEchipa());
            stmt.setLong(4, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return entity;
    }

    @Override
    public Iterable<Participant> findByEchipa(String echipa) {
        logger.traceEntry("Finding participants by echipa {}", echipa);
        List<Participant> participanti = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "SELECT * FROM Participanti WHERE echipa=?")) {
            stmt.setString(1, echipa);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Participant p = new Participant(
                        rs.getString("nume"),
                        rs.getString("cnp"),
                        rs.getString("echipa"));
                p.setId(rs.getLong("id"));
                participanti.add(p);
            }
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return participanti;
    }
}