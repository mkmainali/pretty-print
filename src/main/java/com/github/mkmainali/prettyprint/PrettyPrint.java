package com.github.mkmainali.prettyprint;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface PrettyPrint {

    String header() default "";
}
