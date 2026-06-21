package dienpq.application.service;

import dienpq.application.dto.LoginRequest;
import dienpq.application.dto.LoginResponse;
import dienpq.application.dto.UserDTO;
import dienpq.domain.model.DomainFile;
import dienpq.domain.model.User;
import java.util.List;
import java.io.IOException;

public interface UserAppService {
    LoginResponse login(LoginRequest loginRequest);

    public void changePassword(String usernameOrEmail, String oldPassword, String newPassword, String confirmPassword);

    public User create(String userName, UserDTO dto, DomainFile avatar) throws IOException;

    public void delete(String userName, Integer id);

    public List<User> getAllUsers();

    public User getUserById(Integer id);

    public User getUserByIdentity(String identity);

    public User update(String userName, UserDTO dto, DomainFile avatar) throws IOException;

}
