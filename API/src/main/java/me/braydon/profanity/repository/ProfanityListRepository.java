package me.braydon.profanity.repository;

import me.braydon.profanity.model.ProfanityList;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Braydon
 */
public interface ProfanityListRepository extends MongoRepository<ProfanityList, String> {
    /**
     * Get the profanity list.
     *
     * @return the profanity list
     */
    default ProfanityList getProfanityList() {
        return findById("primary").orElse(null);
    }
}