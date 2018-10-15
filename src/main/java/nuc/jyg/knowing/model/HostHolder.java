package nuc.jyg.knowing.model;

import org.springframework.stereotype.Component;

/**
 * @author Ji YongGuang.
 * @date  2017/11/03.
 */
@Component
public class HostHolder {
    private static ThreadLocal<User> users = new ThreadLocal<User>();

    public User getUser() {
        return users.get();
    }

    public void setUser(User user) {
        users.set(user);
    }

    public void clear() {
        users.remove();;
    }
}
