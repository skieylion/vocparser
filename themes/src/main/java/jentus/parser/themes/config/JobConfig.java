package jentus.parser.themes.config;

import jentus.parser.common.model.FormJson;
import jentus.parser.themes.component.CustomItemWriter;
import jentus.parser.themes.component.StudyfunReader;
import jentus.parser.themes.domain.WordList;
import jentus.parser.themes.services.ProcessorService;
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

    private static final int CHUNK_SIZE = 1;
    private static final String IMPORT_BOOK_JOB_NAME = "job";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final StudyfunReader studyfunReader;
    private final CustomItemWriter customItemWriter;


    @Bean
    public Job job(Step convertStep) {
        return jobBuilderFactory.get(IMPORT_BOOK_JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .flow(convertStep)
                .end()
                .build();
    }

    @Bean("parserProcessor")
    public ItemProcessor<WordList, WordList> parserProcessor(ProcessorService processorService) {
        return processorService::convert;
    }

    @Bean
    public Step convertStep(@Qualifier("parserProcessor") ItemProcessor<WordList, WordList> parserProcessor) {
        return stepBuilderFactory.get("convertStep")
                .<WordList, WordList>chunk(CHUNK_SIZE)
                .reader(studyfunReader)
                .processor(parserProcessor)
                .writer(customItemWriter)
                .build();
    }
}