package de.otto.prototype.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.otto.prototype.controller.handlers.ControllerValidationHandler;
import de.otto.prototype.controller.representation.ValidationEntryRepresentation;
import de.otto.prototype.controller.representation.ValidationRepresentation;
import de.otto.prototype.model.User;
import org.mockito.Mock;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetrics;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

abstract class BaseControllerTest {

    static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final Locale LOCALE = LocaleContextHolder.getLocale();

    private static MessageSource messageSource;
    MockMvc mvc;
    @Mock
    private WebMvcMetrics metrics;

    void setupDefaultMockMvc(Object controller) {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setHandlerExceptionResolvers(createExceptionResolver())
                .build();
    }

    static void initMessageSource() {
        if (messageSource == null) {
            ReloadableResourceBundleMessageSource messageBundle = new ReloadableResourceBundleMessageSource();
            messageBundle.setBasename("classpath:messages/messages");
            messageBundle.setDefaultEncoding("UTF-8");
            messageSource = messageBundle;
        }
    }

    static ValidationEntryRepresentation buildUVERep(String msgCode, String attribute) {
        initMessageSource();
        String msg = messageSource.getMessage(msgCode, null, LOCALE);
        return ValidationEntryRepresentation.builder().attribute(attribute).errorMessage(msg).build();
    }

    static ValidationRepresentation<User> buildUVRep(List<ValidationEntryRepresentation> errors) {
        return ValidationRepresentation.<User>builder().errors(errors).build();
    }

    private ExceptionHandlerExceptionResolver createExceptionResolver() {
        ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver() {
            protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
                Method method = new ExceptionHandlerMethodResolver(ControllerValidationHandler.class).resolveMethod(exception);
                return new ServletInvocableHandlerMethod(new ControllerValidationHandler(messageSource, metrics), method);
            }
        };
        exceptionResolver.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        exceptionResolver.afterPropertiesSet();
        return exceptionResolver;
    }
}
