package com.training.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@SpringBootApplication
public class TrainingApplication {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TrainingApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(ParticipantRepo repo) {
        return args -> {

            List<Participant> participants = List.of(
                new Participant(null, "test1", Sex.MALE),
                new Participant(null, "test2", Sex.FEMALE),
                new Participant(null, "test3", Sex.FEMALE),
                new Participant(null, "test4", Sex.MALE),
                new Participant(null, "test5", Sex.FEMALE),
                new Participant(null, "test6", Sex.MALE)
            );

            Mono.just(participants)
                .flatMapMany(repo::saveAll)
                .doOnEach(System.out::println)
//                .then(repo.deleteAll())
                .thenMany(
                    Flux
                        .fromIterable(participants)
                        .mergeWith(Mono.just(participants.get(0)))
                )
                .flatMap(repo::save)
//                .thenMany(repo.findAll())
//                .doOnComplete(() -> repo.deleteAll().subscribe())
                .subscribe(participant -> LOG.info("saving {}", participant));
        };
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(
        ReactiveMongoDatabaseFactory databaseFactory,
        MappingMongoConverter mongoConverter,
        MongoMappingContext mappingContext
    ) {
        mongoConverter.setTypeMapper(
            new DefaultMongoTypeMapper(
                null,
                mappingContext,
                mongoConverter::getWriteTarget
            ));
        return new ReactiveMongoTemplate(databaseFactory, mongoConverter);
    }
}


@Repository
interface ParticipantRepo extends ReactiveMongoRepository<Participant, String> {

}

enum Sex {
    MALE,
    FEMALE
}

@Document
record Participant(@Id String id, String name, Sex sex) {
}