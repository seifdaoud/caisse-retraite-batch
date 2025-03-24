package com.example.caisseretraitebatch.processor;

import com.example.caisseretraitebatch.model.CaisseRetraiteRecord;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;

public class BeanValidationProcessor extends BeanValidatingItemProcessor<CaisseRetraiteRecord> {

    public BeanValidationProcessor() {
        super();
         setFilter(true);
    }
}
