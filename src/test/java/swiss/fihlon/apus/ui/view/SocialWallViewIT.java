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
package swiss.fihlon.apus.ui.view;

import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.Test;
import swiss.fihlon.apus.ui.KaribuTest;

import static com.github.mvysny.kaributesting.v10.LocatorJ._assertOne;

class SocialWallViewIT extends KaribuTest {

    @Test
    @SuppressWarnings("java:S2699") // flase positiv: Karibu assertions are not recognized
    void socialWallContainsViews() {
        UI.getCurrent().navigate(SocialWallView.class);
        _assertOne(SocialWallView.class);
        _assertOne(EventView.class);
        _assertOne(SocialView.class);
    }

}
