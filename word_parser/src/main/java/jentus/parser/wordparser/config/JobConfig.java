package jentus.parser.wordparser.config;

import jentus.parser.common.model.FormJson;
import jentus.parser.wordparser.domain.ContextFile;
import jentus.parser.wordparser.component.ContextFileReader;
import jentus.parser.wordparser.services.ContextService;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@EnableBatchProcessing
@Configuration
@AllArgsConstructor
public class JobConfig {

    private static final int CHUNK_SIZE = 50;
    private static final String IMPORT_BOOK_JOB_NAME = "job";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final ContextFileReader contextFileReader;


    @Bean
    public Job job(Step convertStep) {
        return jobBuilderFactory.get(IMPORT_BOOK_JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .flow(convertStep)
                .end()
                .build();
    }

    @Bean
    public JsonFileItemWriter<FormJson> writer(@Value("${pathJsonFile}") String output) {
        return new JsonFileItemWriterBuilder<FormJson>()
                .name("writer")
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .resource(new FileSystemResource(output))
                .build();
    }

    @Bean("parserProcessor")
    public ItemProcessor<ContextFile, FormJson> parserProcessor(ContextService contextService) {
        return contextService::convert;
    }

    @Bean
    public Step convertStep(JsonFileItemWriter<FormJson> writer, @Qualifier("parserProcessor") ItemProcessor<ContextFile, FormJson> parserProcessor) {
        return stepBuilderFactory.get("convertStep")
                .<ContextFile, FormJson>chunk(CHUNK_SIZE)
                .reader(contextFileReader)
                .processor(parserProcessor)
                .writer(writer)
                .build();
    }
}