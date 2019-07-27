package developer.aulia.jasalesprivat;

import android.app.Application;

import developer.aulia.jasalesprivat.chat.User;

public class UserClient extends Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
