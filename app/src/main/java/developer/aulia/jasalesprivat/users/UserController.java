package developer.aulia.jasalesprivat.users;

import java.util.Observer;

public class UserController {
    private User user;

    public UserController(User user) {
        this.user = user;
    }

    public void registerUserObserver(Observer observer) throws RuntimeException {
        if (observer == null) {
            throw new RuntimeException("Tidak bisa mendaftarkan pengamat ke ");
        }
        user.addObserver(observer);
    }

    public User getUser() {
        return user;
    }
}