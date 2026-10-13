package ro.mpp2024.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.mpp2024.Cursa;

import java.util.List;

public class CursaHibernateRepository implements CursaRepository {

    private static final Logger logger = LogManager.getLogger();
    private final SessionFactory sessionFactory;

    public CursaHibernateRepository() {
        // Incarca configuratia din hibernate.cfg.xml
        sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();
        logger.info("CursaHibernateRepository initialized");
    }

    @Override
    public Cursa findOne(Long id) {
        logger.traceEntry("Finding cursa by id {}", id);
        try (Session session = sessionFactory.openSession()) {
            Cursa cursa = session.get(Cursa.class, id);
            logger.traceExit();
            return cursa;
        }
    }

    @Override
    public Iterable<Cursa> findAll() {
        logger.traceEntry("Finding all curse");
        try (Session session = sessionFactory.openSession()) {
            List<Cursa> curse = session.createQuery("FROM Cursa", Cursa.class).list();
            logger.traceExit();
            return curse;
        }
    }

    @Override
    public Cursa save(Cursa cursa) {
        logger.traceEntry("Saving cursa");
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(cursa);
            tx.commit();
            logger.traceExit();
            return cursa;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error saving cursa: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Cursa update(Cursa cursa) {
        logger.traceEntry("Updating cursa");
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.merge(cursa);
            tx.commit();
            logger.traceExit();
            return cursa;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error updating cursa: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Cursa delete(Long id) {
        logger.traceEntry("Deleting cursa by id {}", id);
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            Cursa cursa = session.get(Cursa.class, id);
            if (cursa != null) session.remove(cursa);
            tx.commit();
            logger.traceExit();
            return cursa;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error deleting cursa: {}", e.getMessage());
            throw e;
        }
    }
}