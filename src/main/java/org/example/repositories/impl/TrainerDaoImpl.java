package org.example.repositories.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.repositories.TrainerDao;
import org.example.repositories.entities.Trainee;
import org.example.repositories.entities.Trainer;
import org.example.repositories.entities.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class TrainerDaoImpl implements TrainerDao {
    private SessionFactory sessionFactory;

    @Autowired
    public TrainerDaoImpl(@Qualifier("sessionFactory") SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    @Transactional
    public Trainer save(Trainer entity) {
        try {
            if (entity.getId() != null) {
                throw new IllegalArgumentException("Trainee must not have an id");
            }

            sessionFactory.getCurrentSession().persist(entity);
            log.info("Saved new Trainer: {}", entity);

            return entity;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public Trainer update(Trainer entity) {
        try {
            if (findById(entity.getId()).isEmpty()) {
                throw new IllegalArgumentException("Trainer with id " + entity.getId() + " not found");
            }

            entity = sessionFactory.getCurrentSession().merge(entity);
            log.info("Updated Trainer: {}", entity);

            return entity;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Trainer> findById(long id) {
        try (Session session = sessionFactory.openSession()) {
            Trainer entity = session.get(Trainer.class, id);
            log.info("Found Trainer: {}", entity);
            return Optional.of(entity);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Optional.empty();

    }

    @Override
    public Collection<Trainer> findAll() {
        try (Session session = sessionFactory.openSession()) {
            Collection<Trainer> entities = session.createQuery("select t from Trainer t", Trainer.class).list();
            log.info("Found {} Trainers", entities.size());
            return entities;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ArrayList<>();
    }


    @Override
    public boolean isUsernameExist(String username) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.createQuery("from User u where u.username=:username", User.class).setParameter("username", username).uniqueResult();
            return user != null;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            Trainer entity = session.createQuery(
                            "FROM Trainer t where t.username = :username", Trainer.class)
                    .setParameter("username", username)
                    .uniqueResult();

            return Optional.of(entity);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Collection<Trainer> findTrainersNotAssignedToTrainee(String traineeUsername) {
        try (Session session = sessionFactory.openSession()) {
            List<Trainer> trainers = session.createQuery("from Trainer t where t.id Not in (Select trainer.id from Training trainer join Trainee trainee where trainee.username!=:username  )", Trainer.class)
                    .setParameter("username", traineeUsername)
                    .list();

            return trainers;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ArrayList<>();
    }

}
