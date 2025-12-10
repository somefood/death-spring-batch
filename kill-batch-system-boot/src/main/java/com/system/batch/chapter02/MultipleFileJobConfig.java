package com.system.batch.chapter02;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

// bootRun --args='--spring.batch.job.name=multipleFileJob inputFilePath=/Users/seokju/study/death-spring-batch/kill-batch-system-boot/src/main/resources/chapter02-files'
@Slf4j
@Configuration
public class MultipleFileJobConfig {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job multipleFileJob(Step multipleFileStep) {
        return new JobBuilder("multipleFileJob", jobRepository)
                .start(multipleFileStep)
                .build();
    }

    @Bean
    public Step multipleFileStep(
            MultiResourceItemReader<SystemFailure> multiSystemFailureItemReader,
            SystemFailureStdoutItemWriter systemFailureStdoutItemWriter
    ) {
        return new StepBuilder("systemFailureStep", jobRepository)
                .<SystemFailure, SystemFailure>chunk(10, transactionManager)
                .reader(multiSystemFailureItemReader)
                .writer(systemFailureStdoutItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public MultiResourceItemReader<SystemFailure> multiSystemFailureItemReader(
            @Value("#{jobParameters['inputFilePath']}") String inputFilePath) {

        return new MultiResourceItemReaderBuilder<SystemFailure>()
                .name("multiSystemFailureItemReader")
                .resources(new Resource[]{
                        new FileSystemResource(inputFilePath + "/critical-failures.csv"),
                        new FileSystemResource(inputFilePath + "/normal-failures.csv")
                })
                .delegate(multipleFileReader())
                .build();
    }

    @Bean
    public FlatFileItemReader<SystemFailure> multipleFileReader() {
        return new FlatFileItemReaderBuilder<SystemFailure>()
                .name("multipleFileReader")
                .delimited()
                .delimiter(",")
                .names("errorId", "errorDateTime", "severity", "processId", "errorMessage")
                .targetType(SystemFailure.class)
                .linesToSkip(1)
                .customEditors(Map.of(LocalDateTime.class, dateTimeEditor()))
                .build();
    }

    private PropertyEditor dateTimeEditor() {
        return new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                setValue(LocalDateTime.parse(text, formatter));
            }
        };
    }

    @Bean
    public SystemFailureStdoutItemWriter multipleFileStdoutItemWriter() {
        return new SystemFailureStdoutItemWriter();
    }

    public static class SystemFailureStdoutItemWriter implements ItemWriter<SystemFailure> {
        @Override
        public void write(Chunk<? extends SystemFailure> chunk) throws Exception {
            for (SystemFailure failure : chunk) {
                log.info("Processing system failure: {}", failure);
            }
        }
    }

    @Data
    public static class SystemFailure {
        private String errorId;
        private LocalDateTime errorDateTime;
        private String severity;
        private Integer processId;
        private String errorMessage;
    }
}
