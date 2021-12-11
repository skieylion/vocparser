package jentus.parser.importer.config;

import jentus.parser.common.model.FormJson;
import jentus.parser.importer.component.CustomItemWriter;
import jentus.parser.importer.service.FormService;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@EnableBatchProcessing
@Configuration
@AllArgsConstructor
public class JobConfig {

    private static final int CHUNK_SIZE = 10;
    private static final String IMPORT_BOOK_JOB_NAME = "job";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final CustomItemWriter customItemWriter;


    @Bean
    public Job job(Step convertStep) {
        return jobBuilderFactory.get(IMPORT_BOOK_JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .flow(convertStep)
                .end()
                .build();
    }

    @Bean
    public JsonItemReader<FormJson> reader(@Value("${pathJsonFile}") String output) {
        return new JsonItemReaderBuilder<FormJson>()
                .name("reader")
                .jsonObjectReader(new JacksonJsonObjectReader<>(FormJson.class))
                .resource(new FileSystemResource(output))
                .build();
    }

    @Bean("parserProcessor")
    public ItemProcessor<FormJson, FormJson> parserProcessor(FormService formService) {
        return formService::convert;
    }

    @Bean
    public Step convertStep(JsonItemReader<FormJson> reader, @Qualifier("parserProcessor") ItemProcessor<FormJson, FormJson> parserProcessor) {
        return stepBuilderFactory.get("convertStep")
                .<FormJson, FormJson>chunk(CHUNK_SIZE)
                .reader(reader)
                .processor(parserProcessor)
                .writer(customItemWriter)
                .build();
    }
}