package com.system.batch.chapter02;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class RecordLogJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public RecordLogJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job recordLogJob(Step recordLogStep) {
        return new JobBuilder("recordLogJob", jobRepository)
                .start(recordLogStep)
                .build();
    }

    @Bean
    public Step recordLogStep() {
        return new StepBuilder("recordLogStep", jobRepository)
                .<SystemDeath, SystemDeath>chunk(10, transactionManager)
                .reader(systemDeathReader(null))
                .writer(systemDeathWriter())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<SystemDeath> systemDeathReader(
            @Value("#{jobParameters['inputFile']}") String inputFile
    ) {
        return new FlatFileItemReaderBuilder<SystemDeath>()
                .name("systemKillReader")
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .names("command", "cpu", "status")
                .targetType(SystemDeath.class)
                .linesToSkip(1)
                .build();
    }

    @Bean
    public ItemWriter<SystemDeath> systemDeathWriter() {
        return items -> {
            for (SystemDeath item : items) {
                log.info("{}", item);
            }
        };
    }

    public record SystemDeath(String command, int cpu, String status) {
    }
}
