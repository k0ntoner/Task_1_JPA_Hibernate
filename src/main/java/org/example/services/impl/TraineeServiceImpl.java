package org.example.services.impl;

import lombok.extern.slf4j.Slf4j;

import org.example.models.TraineeDto;
import org.example.repositories.TraineeDao;

import org.example.repositories.entities.Trainee;
import org.example.services.TraineeService;
import org.example.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TraineeServiceImpl implements TraineeService {

    @Autowired
    @Qualifier("traineeDaoImpl")
    private TraineeDao traineeDao;

    @Autowired
    private ConversionService conversionService;

    @Override
    public TraineeDto add(TraineeDto traineeDto) {
        log.info("Request to save trainee");

        Trainee trainee = conversionService.convert(traineeDto, Trainee.class);

        if (!UserUtils.verifyPassword(trainee.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        trainee.setPassword(UserUtils.hashPassword(trainee.getPassword()));
        trainee.setUsername(UserUtils.generateUserName(trainee, traineeDao::isUsernameExist));

        Trainee savedTrainee = traineeDao.save(trainee);
        return savedTrainee == null ? null : conversionService.convert(savedTrainee, TraineeDto.class);
    }

    @Override
    public Optional<TraineeDto> findById(long id) {
        log.info("Request to find trainee by ID: {}", id);
        return traineeDao.findById(id).map(trainee -> conversionService.convert(trainee, TraineeDto.class));
    }

    @Override
    public Collection<TraineeDto> findAll() {
        log.info("Request to find all trainees");
        return traineeDao.findAll()
                .stream()
                .map(trainee -> conversionService.convert(trainee, TraineeDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(TraineeDto traineeDto) {
        Trainee trainee = conversionService.convert(traineeDto, Trainee.class);
        log.info("Request to delete trainee with ID: {}", trainee.getId());
        traineeDao.delete(trainee);
    }


    @Override
    public TraineeDto update(TraineeDto traineeDto) {
        Trainee trainee = conversionService.convert(traineeDto, Trainee.class);
        log.info("Request to update trainee with ID: {}", trainee.getId());

        Trainee updatedTrainee = traineeDao.update(trainee);

        return updatedTrainee == null ? null : conversionService.convert(updatedTrainee, TraineeDto.class);
    }

    @Override
    public void deleteByUsername(String username) {
        log.info("Request to delete trainee with username: {}", username);
        traineeDao.delete(traineeDao.findByUsername(username).get());
    }

    @Override
    public Optional<TraineeDto> findByUsername(String username) {
        return traineeDao.findByUsername(username).map(trainee -> conversionService.convert(trainee, TraineeDto.class));
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        if (traineeDao.findByUsername(username).isPresent()) {
            Trainee trainee = traineeDao.findByUsername(username).get();

            if (UserUtils.passwordMatch(oldPassword, trainee.getPassword())) {
                trainee.setPassword(UserUtils.hashPassword(newPassword));
                traineeDao.update(trainee);
                return;
            }

            throw new IllegalArgumentException("Invalid password");
        }
        throw new IllegalArgumentException("Invalid username");
    }

    @Override
    public void activate(TraineeDto traineeDto) {
        Trainee entity = conversionService.convert(traineeDto, Trainee.class);
        entity.setActive(true);
        traineeDao.update(entity);
    }

    @Override
    public void deactivate(TraineeDto traineeDto) {
        Trainee entity = conversionService.convert(traineeDto, Trainee.class);
        entity.setActive(false);
        traineeDao.update(entity);
    }

}