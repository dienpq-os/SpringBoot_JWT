package dienpq.application.service;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyBean {
    // Annotation này chỉ dùng để đánh dấu về mặt kiến trúc,
    // giúp Spring nhận diện tự động
}
