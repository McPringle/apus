package swiss.fihlon.apus;

import java.io.Serializable;

import org.springframework.stereotype.Service;

@Service
public final class GreetService implements Serializable {

    public String greet(final String name) {
        if (name == null || name.isEmpty()) {
            return "Hello anonymous user";
        } else {
            return "Hello " + name;
        }
    }

}
