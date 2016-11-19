package org.test.validator;

import org.test.bean.SampleTable ;
import org.test.common.MessagesKeys;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * Validator for entity SampleTable
 * 
 *
 */
public class SampleTableValidator extends AbstractValidator<SampleTable>{

    @Override
    public void validateEntity(SampleTable entity, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "id", MessagesKeys.ID_MUST_DEFINE);

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", MessagesKeys.NOT_BLANK);

    }
}