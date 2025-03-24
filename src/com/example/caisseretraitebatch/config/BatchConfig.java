package com.example.caisseretraitebatch.config;

import com.example.caisseretraitebatch.model.CaisseRetraiteRecord;
import com.example.caisseretraitebatch.processor.BeanValidationProcessor;
import com.example.caisseretraitebatch.writer.MultiSheetExcelItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;

import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.SkipPolicy;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.DataBinder;
import org.springframework.beans.propertyeditors.CustomDateEditor;

import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @org.springframework.beans.factory.annotation.Value("${input.file:./big_input.csv}")
    private String inputFilePath;

    @org.springframework.beans.factory.annotation.Value("${output.file:./output.xlsx}")
    private String outputFilePath;

    @Autowired
    public BatchConfig(JobBuilderFactory jobBuilderFactory,
                       StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }


    // --- 1) Reader CSV ---
    @Bean
    public FlatFileItemReader<CaisseRetraiteRecord> reader() {
        FlatFileItemReader<CaisseRetraiteRecord> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(inputFilePath));
        reader.setLinesToSkip(1); // Ignorer l’en‑tête

        // Configuration du LineMapper
        org.springframework.batch.item.file.mapping.DefaultLineMapper<CaisseRetraiteRecord> lineMapper =
                new org.springframework.batch.item.file.mapping.DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = getDelimitedLineTokenizer();

        lineMapper.setLineTokenizer(tokenizer);

        BeanWrapperFieldSetMapper<CaisseRetraiteRecord> fieldSetMapper = getCaisseRetraiteRecordBeanWrapperFieldSetMapper();

        lineMapper.setFieldSetMapper(fieldSetMapper);
        reader.setLineMapper(lineMapper);

        return reader;
    }

    private static BeanWrapperFieldSetMapper<CaisseRetraiteRecord> getCaisseRetraiteRecordBeanWrapperFieldSetMapper() {
        BeanWrapperFieldSetMapper<CaisseRetraiteRecord> fieldSetMapper =
                new BeanWrapperFieldSetMapper<CaisseRetraiteRecord>() {
                    @Override
                    protected void initBinder(DataBinder binder) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
                    }
                };
        fieldSetMapper.setTargetType(CaisseRetraiteRecord.class);
        return fieldSetMapper;
    }

    private static DelimitedLineTokenizer getDelimitedLineTokenizer() {
        DelimitedLineTokenizer tokenizer =
                new DelimitedLineTokenizer();
// Dans votre configuration (BatchConfig par ex.)
        tokenizer.setNames(
                "numeroSecuriteSociale",
                "nom",
                "prenom",
                "dateNaissance",
                "adresse",
                "codePostal",
                "ville",
                "pays",
                "nomConjoint",
                "nombreEnfants",
                "montantCotisation"
        );
        tokenizer.setDelimiter(",");
        return tokenizer;
    }

    // --- 2) Processor de validation (JSR-303) ---
    @Bean
    public BeanValidationProcessor beanValidationProcessor() {
        return new BeanValidationProcessor();
    }

    // --- 3) Writer multi-fichiers ---
    @Bean
    public MultiSheetExcelItemWriter multiFileWriter() {
        MultiSheetExcelItemWriter writer = new MultiSheetExcelItemWriter();
        writer.setBaseOutputPath(outputFilePath);
        return writer;
    }

    // --- 4) SkipPolicy ---
    @Bean
    public SkipPolicy skipPolicy() {
        return new MySkipPolicy(); // Gère ValidationException (50 lignes max)
    }

    // --- 5) TaskExecutor pour multi-threading ---
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("batch-thread-");
        executor.initialize();
        return executor;
    }

    // --- 6) Step : Reader + Processor + Writer + skip + multi-thread ---
    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<CaisseRetraiteRecord, CaisseRetraiteRecord>chunk(1000)
                .reader(reader())
                .processor(beanValidationProcessor())
                .writer(multiFileWriter())
                // Autorise à ignorer (skipper) les ValidationException
                .faultTolerant()
                .skip(ValidationException.class)
                .skipLimit(500) // limite de 50 items invalides
                // Multi-threading si besoin
                //.taskExecutor(taskExecutor())
                //.throttleLimit(4)
                .build();
    }

    // --- 7) Job ---
    @Bean
    public Job exportJob() {
        return jobBuilderFactory.get("exportJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1())
                .end()
                .build();
    }
}
