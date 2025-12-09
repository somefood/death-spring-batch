package com.system.batch.chapter01.config;

import com.system.batch.chapter01.tasklet.DeleteOldFilesTasklet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class FileCleanupBatchConfig {
    private final JdbcTemplate jdbcTemplate;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public FileCleanupBatchConfig(JdbcTemplate jdbcTemplate, JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Tasklet deleteOldFilesTasklet() {
        // "temp" 디렉토리에서 30일 이상 지난 파일 삭제
        return new DeleteOldFilesTasklet("/path/to/temp", 30);
    }

    @Bean
    public Step deleteOldFilesStep() {
        return new StepBuilder("deleteOldFilesStep", jobRepository)
                .tasklet(deleteOldFilesTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Job deleteOldFilesJob() {
        return new JobBuilder("deleteOldFilesJob", jobRepository)
                .start(deleteOldFilesStep())
                .build();
    }

    @Bean
    public Step deleteOldRecordsStep() {
        return new StepBuilder("deleteOldRecordsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    int deleted = jdbcTemplate.update("DELETE FROM logs WHERE created < NOW() - INTERVAL 7 DAY");
                    log.info("🗑️ {}개의 오래된 레코드가 삭제되었습니다.", deleted);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Job deleteOldRecordsJob() {
        return new JobBuilder("deleteOldRecordsJob", jobRepository)
                .start(deleteOldRecordsStep())  // Step을 Job에 등록
                .build();
    }
}
