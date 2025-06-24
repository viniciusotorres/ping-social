package com.pingsocial.config;

import com.pingsocial.models.Tribe;
import com.pingsocial.repository.TribeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TribeInitializer implements CommandLineRunner {

    private final TribeRepository tribeRepository;

    public TribeInitializer(TribeRepository tribeRepository) {
        this.tribeRepository = tribeRepository;
    }

    @Override
    public void run(String... args) {
        if (!tribeRepository.existsByName("Tribe Red")) {
            tribeRepository.save(new Tribe(null, "Tribe Red", "Red Tribe Description"));
        }
        if (!tribeRepository.existsByName("Tribe Blue")) {
            tribeRepository.save(new Tribe(null, "Tribe Blue", "Blue Tribe Description"));
        }
    }
}