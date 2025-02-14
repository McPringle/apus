# Apus

[![All Tests](https://github.com/McPringle/apus/actions/workflows/all-tests.yml/badge.svg)](https://github.com/McPringle/apus/actions/workflows/all-tests.yml)
[![codecov](https://codecov.io/gh/McPringle/apus/graph/badge.svg?token=OPDR66ID7D)](https://codecov.io/gh/McPringle/apus)

**A social wall for conferences with additional features.**

The name *Apus* is based on Apus Apus, Latin for common swift. This is a bird species that is extremely adapted to a life in the air and can stay in the air for around ten months almost without interruption and can reach speeds of more than 200 km/h during flight maneuvers. This bird breeds in a wall, flies reliably for extremely long periods without crashing and is also extremely fast. Hopefully all of this also applies to Apus: fast execution, uninterrupted and reliable operation!

See:

* https://en.wikipedia.org/wiki/Common_swift
* https://en.wikipedia.org/wiki/Apus_(bird)

## Features

### Show posts from Social Media

| Platform  | Status    |
|-----------|-----------|
| BlueSky   | SUPPORTED |
| Instagram | - TODO -  |
| LinkedIn  | - TODO -  |
| Mastodon  | SUPPORTED |
| Threads   | - TODO -  |
| Twitter   | - TODO -  |

### Show event agenda

| Event                | Plugin                     |
|----------------------|----------------------------|
| BaselOne             | `SessionizePlugin`         |
| CloudLand            | `DoagPlugin`               |
| CyberLand            | `DoagPlugin`               |
| Devoxx               | `DevoxxPlugin`             |
| Java Forum Nord      | `SessionizePlugin`         |
| Java Forum Stuttgart | `JavaForumStuttgartPlugin` |
| JavaLand             | `DoagPlugin`               |
| KI Navigator         | `DoagPlugin`               |
| Voxxed Days          | `DevoxxPlugin`             |

### And more

| Feature                   | Status    |
|---------------------------|-----------|
| Running text with updates | - TODO -  |
| Show sponsor information  | SUPPORTED |
| Use event based styling   | SUPPORTED |

## Configuration

*Apus* can be started without any specific configuration. All configuration options have working default values.

### Configuration Options

To modify the default configuration values, just specify environment variables with the following names:

| Environment Variable            | Default      | Description                                                                   |
|---------------------------------|--------------|-------------------------------------------------------------------------------|
| APUS_ADMIN_PASSWORD             |              | The hashed password to get admin access (empty = disabled).                   |
| APUS_BLUESKY_INSTANCE           | api.bsky.app | The BlueSky instance used to read the posts from (empty = disabled).          |
| APUS_BLUESKY_POST_API           | [1]          | The URL of the BlueSky API to read the posts (empty = disabled).              |
| APUS_BLUESKY_POST_LIMIT         | 30           | The limit for the number of BlueSky posts when accessing the API.             |
| APUS_DEVOXX_EVENT_API           | [2]          | The URL of the Devoxx API to read the conference agenda.                      |
| APUS_DEVOXX_EVENT_ID            |              | The ID of the Devoxx event to read the conference agenda.                     |
| APUS_DEVOXX_WEEKDAY             |              | The day of the week of the Devoxx event to read the conference agenda.        |
| APUS_DOAG_EVENT_API             | [3]          | The URL of the DOAG event API to read the conference agenda.                  |
| APUS_DOAG_EVENT_ID              | 0            | The ID of the DOAG event to read the conference agenda (0 = disabled).        |
| APUS_EVENT_DATE_ADJUST          |              | Adjust the date of the event in days, ISO-8601 formatted (empty = disabled).  |
| APUS_EVENT_DEMO_ROOM_COUNT      | 0            | Number of rooms for the event demo plugin (0 = disabled).                     |
| APUS_EVENT_IMAGE_URL            |              | The URL of the image to be shown below the event agenda (empty = no image).   |
| APUS_EVENT_NEXT_SESSION_TIMEOUT | 60           | Number of minutes a session is shown before it starts (0 = disabled).         |
| APUS_EVENT_SHOW_EMPTY_ROOMS     | true         | Show (true) or hide (false) empty event rooms.                                |
| APUS_EVENT_SHOW_LEGEND          | true         | Show (true) or hide (false) the event room legend.                            |
| APUS_EVENT_UPDATE_FREQUENCY     | 5            | How often (in minutes) to update event data (0 = disabled).                   |
| APUS_FILTER_LENGTH              | 500          | Hide social media posts which exceed this length (0 = disabled).              |
| APUS_FILTER_REPLIES             | true         | Hide social media posts which are replies.                                    |
| APUS_FILTER_SENSITIVE           | true         | Hide social media posts which contain sensitive information.                  |
| APUS_FILTER_WORDS               |              | Hide social media posts which contain these words.                            |
| APUS_HASHTAGS                   |              | A list of comma separated hashtags for social media posts (empty = disabled). |
| APUS_JFS_DB_URL                 |              | The URL of the database file for Java Forum Stuttgart.                        |
| APUS_LANGUAGE                   | en           | Language code of the language used for the UI.                                |
| APUS_MASTODON_IMAGE_LIMIT       | 1            | Limit number of images per post (0 = no limit).                               |
| APUS_MASTODON_IMAGES_ENABLED    | true         | Enable or disable images in mastodon posts.                                   |
| APUS_MASTODON_INSTANCE          |              | The Mastodon instance used to read the posts from (empty = disabled).         |
| APUS_MASTODON_POST_API          | [4]          | The URL of the Mastodon API to read the posts (empty = disabled).             |
| APUS_MASTODON_POST_LIMIT        | 30           | The limit for the number of Mastodon posts when accessing the API.            |
| APUS_SOCIAL_COLUMNS             | 3            | How many columns to be used for social media posts.                           |
| APUS_SOCIAL_DEMO_POST_COUNT     | 0            | Number of posts for the social demo plugin (0 = disabled).                    |
| APUS_SOCIAL_HEADLINE            |              | Overwrite the headline for social media posts (empty = don't overwrite).      |
| APUS_SESSIONIZE_EVENT_API       | [5]          | The URL of the Sessionize API to read the conference agenda.                  |
| APUS_SESSIONIZE_EVENT_ID        | 0            | The ID of the Sessionize event to read the conference agenda (0 = disabled).  |
| APUS_SESSIONIZE_SPEAKER_API     | [6]          | The URL of the Sessionize API to read the speaker information.                |
| APUS_STYLES                     |              | Inject custom styles into the user interface (see explanations below).        |
| TZ                              | UTC          | The timezone used for date and time calculations.                             |

> [!NOTE]  
> The `TZ` environment variable is not specific to *Apus*, it is a system variable and therefore has no `APUS` prefix!

The environment variables will override the default values. Some default values might be too long to be displayed in this table. They are marked with a number in square brackets and can be looked up in the following list:

1. `https://%s/xrpc/app.bsky.feed.searchPosts?q=%s`
2. `https://%s.cfp.dev/api/public/schedules/%s`
3. `https://meine.doag.org/api/event/action.getCPEventAgenda/eventId.%d/`
4. `https://%s/api/v1/timelines/tag/%s?limit=%d`
5. `https://sessionize.com/api/v2/%s/view/Sessions`
6. `https://sessionize.com/api/v2/%s/view/Speakers`

The `APUS_EVENT_DATE_ADJUST` option uses the ISO-8601 period formats `PnYnMnD` and `PnW`. Examples:

| Example     | Description                                                  |
|-------------|--------------------------------------------------------------|
| `P5D`       | +5 days                                                      |
| `P3M`       | +3 months                                                    |
| `P2Y`       | +2 years                                                     |
| `P4W`       | +4 weeks                                                     |
| `P1Y2M3D`   | +1 year, +2 months, +3 days                                  |
| `P1Y2M3W4D` | +1 year, +2 months, +25 days (3 weeks and 4 days)            |
| `P-1Y2M`    | -1 year, +2 months (minus is valid for the year only)        |
| `-P1Y2M`    | -1 year, -2 months (minus is valid for the whole expression) |

#### Custom Styles

You can modify the styles of the user interface using CSS variables. The CSS variables must be set using the environment variable `APUS_STYLES` in key and value pairs. The key is the variable to be set and must start with two dashes (`--`). Keys and values must be separated by a colon (`:`). Multiple key and value pairs are separated by a semicolon (`;`). Example:

```
--name-of-variable-one:value1;--name-of-variable-two:value2
```

The following table contains the CSS variables you can modify to change the user interface and their default values:

| CSS Variable                      | Default           | Description                                                        |
|-----------------------------------|-------------------|--------------------------------------------------------------------|
| --event-background-color          | #e7eaee           | The color for the background of the event agenda.                  |
| --event-title-color               | #262626           | The color for the title of the event agenda.                       |
| --event-text-color                | #262626           | The color for the text of the event agenda.                        |
| --event-running-session-color     | #ffffff           | The color for the background of rooms with running sessions.       |
| --event-next-session-color        | #eeeeee           | The color for the background of rooms with sessions starting next. |
| --event-closed-room-color         | #cccccc           | The color for the background of closed rooms.                      |
| --event-room-border               | 1px solid #909090 | The border for the event room.                                     |
| --event-image-position-bottom     | 10px              | The position of the optional event image relative to the bottom.   |
| --event-image-position-left       | 10px              | The position of the optional event image relative to the left.     |
| --event-image-width               | auto              | The width of the optional event image.                             |
| --event-image-height              | auto              | The height of the optional event image.                            |
| --social-background-color         | #e7eaee           | The color for the background of the social wall.                   |
| --social-title-color              | #262626           | The color for the title of the social wall.                        |
| --social-text-color               | #262626           | The color for the text of the social wall.                         |
| --social-post-background-color    | #ffffff           | The color for the background of social posts.                      |
| --social-post-border              | 1px solid #909090 | The border for the social posts.                                   |
| --speaker-avatar-background-color | transparent       | The color for the background of speaker avatars.                   |
| --speaker-avatar-border           | none              | The border for the speaker avatars.                                |

Default values may change in newer versions of *Apus*. The custom styles of some events are documented below.

### Create Hashed Password

For security reasons the password is not stored in cleartext. *Apus* requires the password to be hashed using [bcrypt](https://en.wikipedia.org/wiki/Bcrypt). Of course, *Apus* can do this for you. Start the JAR file providing the parameter `-p` followed by the password you want to create a hash for. The output will show you two lines of code. The first line contains the hashed password and the second line contains the same hashed password, but with the dollar signs escaped ready to copy and paste it into a `docker-compose.yaml`. Example:

```
java -jar apus.jar -p 12345
Hashed password for environment variable: $2a$10$nybQbl/iY8SRJkfHJVncS.L5.OC3KJ6VRBYVAID7qnUqwylmn/BtK
Hashed password for Docker Compose file: $$2a$$10$$nybQbl/iY8SRJkfHJVncS.L5.OC3KJ6VRBYVAID7qnUqwylmn/BtK
```

### Configuration Files

All configuration files are completely optional and stored in an `.apus` subdirectory of the home directory of the user running *Apus*.

| File              | Description                                           |
|-------------------|-------------------------------------------------------|
| `blockedProfiles` | This file contains blocked profiles, one per line.    |
| `hiddenPostIds`   | This file contains IDs of hidden posts, one per line. |

## Production

### Run in Production

It is highly recommended to use [Docker](https://www.docker.com/) or [Podman](https://podman.io/) to run *Apus* in production. Use the following command line as an example:

```shell
docker run \
    --name apus \
    -p 80:8080 \
    -v $HOME/.apus:/home/apus/.apus \
    -e APUS_ADMIN_PASSWORD=sEcrEt \
    -e APUS_DEMO_ROOM_COUNT=12 \
    -e APUS_HASHTAGS=java \
    -e APUS_MASTODON_INSTANCE=mastodon.social \
    -e TZ=CET \
    -d \
    --rm \
    mcpringle/apus
```

Short explanation, consult the Docker or Podman documentation for more information about all available options for running an image.

| Option         | Explanation                                   |
|----------------|-----------------------------------------------|
| --name apus    | Specify the name for the running instance.    |
| -p 80:8080     | Make *Apus* available on host port 80         |
| -e KEY=value   | Configure *Apus* using environment variables. |
| -d             | Run *Apus* in daemon mode (background).       |
| --rm           | Remove the container when stopping *Apus*.    |
| mcpringle/apus | The Docker image to be started.               |

Modify this command according your needs and consult the [configuration section](#configuration) above for more information about how to configure *Apus*. The Docker image of *Apus* will be pulled from [Docker Hub](https://hub.docker.com/) automatically when not available locally.

## Communication

### Matrix Chat

There is a channel at Matrix for quick and easy communication. This is publicly accessible for everyone. For developers as well as users. The communication in this chat is to be regarded as short-lived and has no documentary character.

You can find our Matrix channel here: [@project-apus:ijug.eu](https://matrix.to/#/%23project-apus:ijug.eu)

### GitHub Discussions

We use the corresponding GitHub function for discussions. The discussions held here are long-lived and divided into categories for the sake of clarity. One important category, for example, is that for questions and answers.

Discussions on GitHub: https://github.com/McPringle/apus/discussions  
Questions and Answers: https://github.com/McPringle/apus/discussions/categories/q-a

## Copyright and License

[AGPL License](https://www.gnu.org/licenses/agpl-3.0.de.html)

*Copyright (C) Marcus Fihlon and the individual contributors to **Apus**.*

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
