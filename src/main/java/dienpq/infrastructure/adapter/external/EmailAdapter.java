package dienpq.infrastructure.adapter.external;

import org.springframework.stereotype.Component;
import dienpq.domain.port.external.EmailServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

@Component
@RequiredArgsConstructor
public class EmailAdapter implements EmailServicePort {

    // JavaMailSender là Bean có sẵn của Spring
    // khi bạn thêm starter-mail vào pom.xml
    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordToUser(String email, String rawPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Mật khẩu mới của bạn - Hệ thống");
        message.setText("Xin chào, mật khẩu đăng nhập hệ thống mới được cấp của bạn là: " + rawPassword);

        mailSender.send(message);
    }
}