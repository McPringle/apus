/*
 * Apus - A social wall for conferences with additional features.
 * Copyright (C) Marcus Fihlon and the individual contributors to Apus.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package swiss.fihlon.apus;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import swiss.fihlon.apus.util.PasswordUtil;

/**
 * <p>The entry point of the Spring Boot application.</p>
 *
 * <p>Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.</p>
 */
@SpringBootApplication
@EnableScheduling
@Push
@PageTitle("Apus – Social Media Wall with Conference Agenda")
@PWA(name = "Apus", shortName = "Apus")
@Theme("apus")
@SuppressWarnings({"FinalClass", "HideUtilityClassConstructor", "RegexpSingleline"})
public class Application implements AppShellConfigurator {

    private static final Option HASH_PASSWORD_OPTION = new Option("p", "password", true, "Password to hash");

    public static void main(@NotNull final String[] args) throws ParseException {
        final var options = new Options();
        options.addOption(HASH_PASSWORD_OPTION);

        final var cmd = new DefaultParser().parse(options, args);
        if (cmd.hasOption(HASH_PASSWORD_OPTION)) {
            final var password = cmd.getOptionValue(HASH_PASSWORD_OPTION);
            System.out.println(PasswordUtil.hashPassword(password));
        } else {
            SpringApplication.run(Application.class, args);
        }
    }

}
