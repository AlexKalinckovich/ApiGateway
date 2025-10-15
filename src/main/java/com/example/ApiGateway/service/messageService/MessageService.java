package com.example.ApiGateway.service.messageService;

import com.example.ApiGateway.exception.ErrorMessage;
import lombok.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageService {
    private final MessageSource messageSource;

    public MessageService(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(@NonNull final ErrorMessage errorMessage) {
        return messageSource.getMessage(errorMessage.getKey(), null, Locale.ENGLISH);
    }
}
