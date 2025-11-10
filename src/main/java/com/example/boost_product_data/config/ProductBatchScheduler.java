package com.example.boost_product_data.config;

import com.example.boost_product_data.Repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job createProductJob;  ///스프링에서 @Bean 어노테이션은 기본적으로
                                         /// **메소드 이름(createProductsJob)**을 그
                                         ///  빈의 **고유 ID(이름)**로 사용합니다.

    @Scheduled(fixedDelay = 1000)  ///  이전작업 시작 1초후 다시시작.
//    @SchedulerLock(
//            name = "createProductJobLock", // 락 이름 (고유해야 함)
//            lockAtMostFor = "1m",  // 최대 10분간 락 유지 (Job이 10분 이상 걸리면 강제 해제)
//            lockAtLeastFor = "32s" // 최소 30초간 락 유지 (Job이 5초 만에 끝나도 30초간은 락 유지)
//    )
    public void runJob() {
        try{
            log.info("start scheduled job");
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("totalItems",50000L)
                    .addString("runtime", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();

            jobLauncher.run(createProductJob, jobParameters);

        } catch (Exception e) {
            log.error("throw batchScheduler exception {} ", e.getMessage());
            throw new RuntimeException(e);
        }
    }



}
