package me.bombom.api.v1.common.resolver;


import io.swagger.v3.oas.annotations.Hidden;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Hidden
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginMember {

    boolean anonymous() default false;
}
