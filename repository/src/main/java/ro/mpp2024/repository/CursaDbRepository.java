package ro.mpp2024.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.mpp2024.Cursa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CursaDbRepository implements CursaRepository {

    private static final Logger logger = LogManager.getLogger();
    private String dbUrl;

    public CursaDbRepository(Properties props) {
        logger.info("Initializing CursaDbRepository with properties: {}", props);
        dbUrl = props.getProperty("jdbc.url");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    @Override
    public Cursa findOne(Long id) {
        logger.traceEntry("Finding cursa with id {}", id);
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM Curse WHERE id=?")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Cursa cursa = new Cursa(rs.getInt("capacitateMotor"));
                cursa.setId(rs.getLong("id"));
                logger.traceExit();
                return cursa;
            }
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return null;
    }

    @Override
    public Iterable<Cursa> findAll() {
        logger.traceEntry("Finding all curse");
        List<Cursa> curse = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM Curse")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Cursa cursa = new Cursa(rs.getInt("capacitateMotor"));
                cursa.setId(rs.getLong("id"));
                curse.add(cursa);
            }
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return curse;
    }

    @Override
    public Cursa save(Cursa entity) {
        logger.traceEntry("Saving cursa {}", entity.getCapacitateMotor());
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "INSERT INTO Curse (capacitateMotor) VALUES (?)")) {
            stmt.setInt(1, entity.getCapacitateMotor());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return entity;
    }

    @Override
    public Cursa delete(Long id) {
        logger.traceEntry("Deleting cursa with id {}", id);
        Cursa cursa = findOne(id);
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement("DELETE FROM Curse WHERE id=?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return cursa;
    }

    @Override
    public Cursa update(Cursa entity) {
        logger.traceEntry("Updating cursa {}", entity.getId());
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "UPDATE Curse SET capacitateMotor=? WHERE id=?")) {
            stmt.setInt(1, entity.getCapacitateMotor());
            stmt.setLong(2, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return entity;
    }
}