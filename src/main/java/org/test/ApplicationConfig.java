package org.test;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.test.validator.SampleTableValidator;

/**
 * Application configuration.
 * ( messages resources, validators)
 *
 */
@Configuration
@ComponentScan(basePackageClasses = ApplicationConfig.class)
@EnableWebMvc
public class ApplicationConfig extends RepositoryRestMvcConfiguration {

    /**
     * Message Ressource declaration.
     * @return MessageRessource
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("i18n/messages");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }


   /**
     * Validator declaration for SampleTable
     * @return the SampleTableValidator
     */
    @Bean
    public SampleTableValidator beforeCreateSampleTableValidator() {
        return new SampleTableValidator();
    }



}