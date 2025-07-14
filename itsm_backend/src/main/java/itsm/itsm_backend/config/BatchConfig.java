package itsm.itsm_backend.config;

import itsm.itsm_backend.ticket.Ticket;
import itsm.itsm_backend.ticket.jpa.TicketRepository;
import itsm.itsm_backend.user.User;
import itsm.itsm_backend.ticket.jpa.UserRepository;
import jakarta.validation.constraints.NotEmpty;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final TicketRepository repository;
    private final UserRepository userRepository;

    @Bean
    @StepScope
    public FlatFileItemReader<Ticket> reader(@Value("#{jobParameters['csvPath']}") String csvPath) {
        FlatFileItemReader<Ticket> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource(csvPath));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }


    @Bean
    @StepScope
    public TicketProcessor processor(@Value("#{jobParameters['userId']}") Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return new TicketProcessor(user);
    }



    @Bean
    public RepositoryItemWriter<Ticket> writer() {
        RepositoryItemWriter<Ticket> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step step1(FlatFileItemReader<Ticket> reader, TicketProcessor processor) {
        return new StepBuilder("csvImport", jobRepository)
                .<Ticket, Ticket>chunk(1000, platformTransactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer())
                .taskExecutor(taskExecutor())
                .build();
    }


    @Bean
    public Job runJob(Step step1) {
        return new JobBuilder("importTickets", jobRepository)
                .start(step1)
                .build();
    }


    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }

    private LineMapper<Ticket> lineMapper() {
        DefaultLineMapper<Ticket> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("title", "description", "priority", "status","category","type");

        BeanWrapperFieldSetMapper<Ticket> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Ticket.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }
}