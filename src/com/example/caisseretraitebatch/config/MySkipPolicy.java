package com.example.caisseretraitebatch.config;

import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.validator.ValidationException;

public class MySkipPolicy implements SkipPolicy {

    @Override
    public boolean shouldSkip(Throwable t, int skipCount) {
        // Si c’est une ValidationException ET que skipCount < 50, on skip
        if (t instanceof ValidationException && skipCount < 50) {
            return true;
        }
        return false;
    }
}
