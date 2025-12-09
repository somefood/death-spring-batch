package com.system.batch.config;

import com.system.batch.listener.BigBrotherJobExecutionListener;
import com.system.batch.listener.ServerRackControlListener;
import lombok.RequiredArgsConstructor;
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
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class ListenerConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job systemMonitoringJob(Step monitoringStep) {
        return new JobBuilder("systemMonitoringJob", jobRepository)
                .listener(new BigBrotherJobExecutionListener())
                .start(monitoringStep)
                .build();
    }
    
    @Bean
    public Step monitoringStep() {
        return new StepBuilder("monitoringStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("System Monitoring Step is running...");
                    // 모니터링 로직 구현
                    return null;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step serverRackControlStep(Tasklet destructiveTasklet) {
        return new StepBuilder("serverRackControlStep", jobRepository)
                .tasklet(destructiveTasklet, transactionManager)
                .listener(new ServerRackControlListener()) // 빌더의 listener() 메서드에 전달
                .build();
    }
    
    @Bean
    public Tasklet destructiveTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Executing destructive server rack control tasklet...");
            // 서버랙 제어 로직 구현
            return RepeatStatus.FINISHED;
        };
    }
}
