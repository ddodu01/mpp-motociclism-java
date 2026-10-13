package ro.mpp2024.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.mpp2024.Participant;

import java.util.List;

public class ParticipantHibernateRepository implements ParticipantRepository {

    private static final Logger logger = LogManager.getLogger();
    private final SessionFactory sessionFactory;

    public ParticipantHibernateRepository() {
        // Incarca configuratia din hibernate.cfg.xml
        sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();
        logger.info("ParticipantHibernateRepository initialized");
    }

    @Override
    public Participant findOne(Long id) {
        logger.traceEntry("Finding participant by id {}", id);
        try (Session session = sessionFactory.openSession()) {
            Participant participant = session.get(Participant.class, id);
            logger.traceExit();
            return participant;
        }
    }

    @Override
    public Iterable<Participant> findAll() {
        logger.traceEntry("Finding all participanti");
        try (Session session = sessionFactory.openSession()) {
            List<Participant> participanti = session
                    .createQuery("FROM Participant", Participant.class)
                    .list();
            logger.traceExit();
            return participanti;
        }
    }

    @Override
    public Participant save(Participant participant) {
        logger.traceEntry("Saving participant {}", participant.getNume());
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(participant);
            tx.commit();
            logger.traceExit();
            return participant;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error saving participant: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Participant update(Participant participant) {
        logger.traceEntry("Updating participant {}", participant.getNume());
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.merge(participant);
            tx.commit();
            logger.traceExit();
            return participant;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error updating participant: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Participant delete(Long id) {
        logger.traceEntry("Deleting participant by id {}", id);
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            Participant participant = session.get(Participant.class, id);
            if (participant != null) session.remove(participant);
            tx.commit();
            logger.traceExit();
            return participant;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error deleting participant: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Iterable<Participant> findByEchipa(String echipa) {
        logger.traceEntry("Finding participants by echipa {}", echipa);
        try (Session session = sessionFactory.openSession()) {
            List<Participant> participanti = session
                    .createQuery("FROM Participant p WHERE p.echipa = :echipa", Participant.class)
                    .setParameter("echipa", echipa)
                    .list();
            logger.traceExit();
            return participanti;
        }
    }
}