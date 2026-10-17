package com.membership.member;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.test.context.TestPropertySource;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestPropertySource(properties = {
        "coffee.admin.email=admin@example.invalid",
        "coffee.admin.password=test-admin-password"
})
public @interface WithTestAdmin {
    String EMAIL = "admin@example.invalid";
    String PASSWORD = "test-admin-password";
}
