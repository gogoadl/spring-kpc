package com.ssafy.kpcbatch.config;

import com.ssafy.kpcbatch.dto.Region;
import com.ssafy.kpcbatch.reader.RealEstateRegionReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class RestAPIJobConfiguration {
    private static final String PROPERTY_REST_API_URL = "rest.api.url"; // api 요청 url

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job simpleJob(Step collectStep) { // 이런식으로 의존성 주입을 받을 수도 있구나
        return jobBuilderFactory.get("simpleJob")
                .start(collectStep)
                .build();
    }

    @Bean
    @JobScope
    public Step collectStep(ItemReader<Region> reader, JpaItemWriter<Region> writer) {
        return stepBuilderFactory.get("collectStep")
                .<Region, Region>chunk(10)
                .reader(reader)
                .processor(jpaItemProcessor())
                .writer(writer)
                .build();
    }

    @Bean
    public ItemReader<Region> reader(Environment environment, RestTemplate restTemplate) {
        // Rest API 로 데이터를 가져온다.
        return new RealEstateRegionReader(environment.getRequiredProperty(PROPERTY_REST_API_URL),
                restTemplate);
    }

    @Bean
    public ItemProcessor<Region, Region> jpaItemProcessor() {
        // 가져온 데이터를 적절히 가공해준다.
//        return region -> Region.builder()
//                .cortarNo(region.getCortarNo())
//                .cortarName(region.getCortarName())
//                .build();
        return null;
    }

    @Bean
    public JpaItemWriter<Region> writer() {
        // 데이터베이스에 저장한다.
        JpaItemWriter<Region> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
