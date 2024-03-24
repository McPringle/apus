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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.html.testbench.ParagraphElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.BrowserTest;
import com.vaadin.testbench.BrowserTestBase;

public class MainViewIT extends BrowserTestBase {

    /**
     * If running on CI, get the host name from environment variable HOSTNAME
     *
     * @return the host name
     */
    private static String getDeploymentHostname() {
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && !hostname.isEmpty()) {
            return hostname;
        }
        return "localhost";
    }

    @BeforeEach
    public void open() {
        getDriver().get("http://"+getDeploymentHostname()+":8080/");
    }

    @BrowserTest
    public void clickingButtonShowsNotification() {
        Assertions.assertFalse($(ParagraphElement.class).exists());
        $(ButtonElement.class).waitForFirst().click();
        Assertions.assertTrue($(ParagraphElement.class).exists());
    }

    @BrowserTest
    public void clickingButtonTwiceShowsTwoNotifications() {
        Assertions.assertFalse($(ParagraphElement.class).exists());
        ButtonElement button = $(ButtonElement.class).waitForFirst();
        button.click();
        button.click();
        $(ParagraphElement.class).waitForFirst();
        Assertions.assertEquals(2, $(ParagraphElement.class).all().size());
    }

    @BrowserTest
    public void testClickButtonShowsHelloAnonymousUserNotificationWhenUserNameIsEmpty() {
        ButtonElement button = $(ButtonElement.class).waitForFirst();
        button.click();
        ParagraphElement msg = $(ParagraphElement.class).waitForFirst();
        Assertions.assertEquals("Hello anonymous user", msg.getText());
    }

    @BrowserTest
    public void testClickButtonShowsHelloUserNotificationWhenUserIsNotEmpty() {
        TextFieldElement textField = $(TextFieldElement.class).waitForFirst();
        textField.setValue("Vaadiner");
        $(ButtonElement.class).waitForFirst().click();
        ParagraphElement msg = $(ParagraphElement.class).waitForFirst();
        Assertions.assertEquals("Hello Vaadiner", msg.getText());
    }

    @BrowserTest
    public void testEnterShowsHelloUserNotificationWhenUserIsNotEmpty() {
        TextFieldElement textField = $(TextFieldElement.class).waitForFirst();
        textField.setValue("Vaadiner");
        textField.sendKeys(Keys.ENTER);
        ParagraphElement msg = $(ParagraphElement.class).waitForFirst();
        Assertions.assertEquals("Hello Vaadiner", msg.getText());
    }
}
