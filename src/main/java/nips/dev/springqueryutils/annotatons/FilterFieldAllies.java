package nips.dev.springqueryutils.annotatons;

import nips.dev.springqueryutils.annotatons.enums.SqlOperator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/*
* пометим класс как наследник query*/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FilterFieldAllies {
    String value() default "";
    SqlOperator operator() default SqlOperator.EQUALS;
}
