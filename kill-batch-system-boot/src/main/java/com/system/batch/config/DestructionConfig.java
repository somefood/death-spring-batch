package com.system.batch.config;

import com.system.batch.validator.SystemDestructionValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

//./gradlew bootRun --args='--spring.batch.job.name=systemDestructionJob destructionPower=1,java.lang.Long'
@Slf4j
@Configuration
public class DestructionConfig {

    @Bean
    public Job systemDestructionJob(
            JobRepository jobRepository,
            Step systemDestructionStep,
            SystemDestructionValidator validator
    ) {
        return new JobBuilder("systemDestructionJob", jobRepository)
                .validator(validator)
                .start(systemDestructionStep)
                .build();
    }

    @Bean
    public Step systemDestructionStep(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            Tasklet systemDestructionTasklet2) {  // Tasklet 주입

        return new StepBuilder("systemDestructionStep", jobRepository)
                .tasklet(systemDestructionTasklet2, platformTransactionManager)
                .build();
    }

    @Bean
    @StepScope  // Tasklet에 @StepScope 적용
    public Tasklet systemDestructionTasklet2(
            @Value("#{jobParameters['destructionPower']}") String destructionPowerStr) {

        return (contribution, chunkContext) -> {
            Long destructionPower = Long.parseLong(destructionPowerStr);
            log.info("hello {}", destructionPower);
            return RepeatStatus.FINISHED;
        };
    }
}
