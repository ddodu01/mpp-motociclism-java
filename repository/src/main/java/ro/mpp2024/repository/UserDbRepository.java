package ro.mpp2024.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.mpp2024.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class UserDbRepository implements UserRepository {

    private static final Logger logger = LogManager.getLogger();
    private String dbUrl;

    public UserDbRepository(Properties props) {
        logger.info("Initializing UserDbRepository with properties: {}", props);
        dbUrl = props.getProperty("jdbc.url");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    @Override
    public User findOne(Long id) {
        logger.traceEntry("Finding user with id {}", id);
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "SELECT * FROM Users WHERE id=?")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(rs.getString("username"), rs.getString("password"));
                user.setId(rs.getLong("id"));
                logger.traceExit();
                return user;
            }
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return null;
    }

    @Override
    public Iterable<User> findAll() {
        logger.traceEntry("Finding all users");
        List<User> users = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM Users")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User(rs.getString("username"), rs.getString("password"));
                user.setId(rs.getLong("id"));
                users.add(user);
            }
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return users;
    }

    @Override
    public User save(User entity) {
        logger.traceEntry("Saving user {}", entity.getUsername());
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "INSERT INTO Users (username, password) VALUES (?, ?)")) {
            stmt.setString(1, entity.getUsername());
            stmt.setString(2, entity.getPassword());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return entity;
    }

    @Override
    public User delete(Long id) {
        logger.traceEntry("Deleting user with id {}", id);
        User user = findOne(id);
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "DELETE FROM Users WHERE id=?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return user;
    }

    @Override
    public User update(User entity) {
        logger.traceEntry("Updating user {}", entity.getId());
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "UPDATE Users SET username=?, password=? WHERE id=?")) {
            stmt.setString(1, entity.getUsername());
            stmt.setString(2, entity.getPassword());
            stmt.setLong(3, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return entity;
    }

    public User findByUsername(String username) {
        logger.traceEntry("Finding user by username {}", username);
        System.out.println("DB URL: " + dbUrl);
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(
                     "SELECT * FROM Users WHERE username=?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(rs.getString("username"), rs.getString("password"));
                user.setId(rs.getLong("id"));
                logger.traceExit();
                return user;
            }
        } catch (SQLException e) {
            logger.error("Error DB: {}", e.getMessage());
        }
        logger.traceExit();
        return null;
    }
}