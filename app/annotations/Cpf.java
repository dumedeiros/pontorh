package annotations;

import net.sf.oval.configuration.annotation.Constraint;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(checkWith = CPFCheck.class)
public @interface Cpf {

    String message() default CPFCheck.message;
}
