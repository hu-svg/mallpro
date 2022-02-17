package com.macro.mall.validator;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author hu
 * @create 2022/2/15
 */

public class FlagValidatorClass implements ConstraintValidator<FlagValidator,Integer> {
    private String[] values;
    @Override
    public void initialize(FlagValidator constraintAnnotation) {
        this.values=constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        boolean boolFlag=false;
        if(integer==null){
            return true;
        }
        for (String value : values) {
        if(value.equals(String.valueOf(integer))){
                boolFlag=true;
            }
        }


        return boolFlag;
    }
}