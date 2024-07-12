# Apus

[![All Tests](https://github.com/McPringle/apus/actions/workflows/all-tests.yml/badge.svg)](https://github.com/McPringle/apus/actions/workflows/all-tests.yml)

**A social wall for conferences with additional features.**

The name *Apus* is based on Apus Apus, Latin for common swift. This is a bird species that is extremely adapted to a life in the air and can stay in the air for around ten months almost without interruption and can reach speeds of more than 200 km/h during flight maneuvers. This bird breeds in a wall, flies reliably for extremely long periods without crashing and is also extremely fast. Hopefully all of this also applies to Apus: fast execution, uninterrupted and reliable operation!

See:

* https://en.wikipedia.org/wiki/Common_swift
* https://en.wikipedia.org/wiki/Apus_(bird)

## Screenshot

![Apus Screenshot](screenshot.webp)

## Features

### Show posts from Social Media

| Platform                    | Status    |
|-----------------------------|-----------|
| Bluesky                     | - TODO -  |
| Instagram                   | - TODO -  |
| Mastodon                    | SUPPORTED |
| Twitter                     | - TODO -  |

### Show event agenda

| Event                | Plugin           |
|----------------------|------------------|
| BaselOne             | SessionizePlugin |
| CyberLand            | DoagPlugin       |
| Java Forum Stuttgart | - TODO -         |
| JavaLand             | DoagPlugin       |
| Voxxed Days Zürich   | - TODO -         |

### And more

| Feature                   | Status   |
|---------------------------|----------|
| Running text with updates | - TODO - |
| Show sponsor information  | - TODO - |
| Use event based styling   | - TODO - |

## Build

### Maven

*Apus* uses [Maven](https://maven.apache.org/) to build the project. Please use standard Maven commands to build what you need:

| Command          | What it does                                                      |
|------------------|-------------------------------------------------------------------|
| `./mvnw`         | compile and run the app                                           |
| `./mvnw clean`   | cleanup generated files and build artefacts                       |
| `./mvnw compile` | compile the code without running the tests                        |
| `./mvnw test`    | compile and run all tests                                         |
| `./mvnw package` | compile, test, and create a JAR file to run it with Java directly |
| `./mvnw verify`  | compile, test, package, and run analysis tools                    |

There is *no need* to run the `install` or `deploy` tasks. They will just run longer, produce unnecessary output, burn energy, and occupy your disk space. [Don't just blindly run mvn clean install...](https://www.andreaseisele.com/posts/mvn-clean-install/)

### Docker

*Apus* comes with a complete dockerized build for production use. It is not recommended to use the self-contained build for development purposes. Please take a look at the section about [Production Build](#production-build) below.

## Running and debugging

### Running from the command line.

To run from the command line, run `./mvnw` and open http://localhost:8080 in your browser.

### Running and debugging in Intellij IDEA

- Locate the `Application.java` class in the project view. It is in the `src` folder, under the main package's root.
- Right-click on the `Application` class
- Select "Debug 'Application.main()'" from the list

After the server has started, you can view the UI at http://localhost:8080/ in your browser.
You can now also attach breakpoints in code for debugging purposes, by clicking next to a line number in any source file.

### Running and debugging in Eclipse

- Locate the `Application.java` class in the package explorer. It is in `src/main/java`, under the main package.
- Right-click on the file and select `Debug As` --> `Java Application`.

Do not worry if the debugger breaks at a `SilentExitException`. This is a Spring Boot feature and happens on every startup.

After the server has started, you can view it at http://localhost:8080/ in your browser.
You can now also attach breakpoints in code for debugging purposes, by clicking next to a line number in any source file.

## Configuration

*Apus* can be started without any specific configuration. All configuration options have working default values.

### Configuration Options

To modify the default configuration values, just specify environment variables with the following names:

| Environment Variable    | Default | Description                                                                  |
|-------------------------|---------|------------------------------------------------------------------------------|
| ADMIN_PASSWORD          |         | The hashed password to get admin access (empty = disabled).                  |
| CUSTOM_STYLES           |         | Inject custom styles into the user interface (see explanations below).       |
| DEMO_ROOM_COUNT         | 0       | Number of rooms for the event demo plugin (0 = disabled).                    |
| DOAG_EVENT_API          | [1]     | The URL of the DOAG event API to read the conference agenda.                 |
| DOAG_EVENT_ID           | 0       | The ID of the DOAG event to read the conference agenda (0 = disabled).       |
| EVENT_UPDATE_FREQUENCY  | 5       | How often (in minutes) to update event data (0 = disabled).                  |
| FILTER_LENGTH           | 500     | Hide social media posts which exceed this length (0 = disabled).             |
| FILTER_REPLIES          | true    | Hide social media posts which are replies.                                   |
| FILTER_SENSITIVE        | true    | Hide social media posts which contain sensitive information.                 |
| FILTER_WORDS            |         | Hide social media posts which contain these words.                           |
| JFS_DB_URL              |         | The URL of the database file for Java Forum Stuttgart.                       |
| MASTODON_HASHTAG        |         | The hashtag for the mastodon wall (empty = disabled).                        |
| MASTODON_IMAGE_LIMIT    | 1       | Limit number of images per post (0 = no limit).                              |
| MASTODON_IMAGES_ENABLED | true    | Enable or disable images in mastodon posts.                                  |
| MASTODON_INSTANCE       |         | The Mastodon instance used to read the posts from (empty = disabled).        |
| SESSIONIZE_EVENT_API    | [2]     | The URL of the Sessionize event API to read the conference agenda.           |
| SESSIONIZE_EVENT_ID     | 0       | The ID of the Sessionize event to read the conference agenda (0 = disabled). |
| TZ                      | UTC     | The timezone used for date and time calculations.                            |

The environment variables will override the default values. Some default values might be too long to be displayed in this table. They are marked with a number in square brackets and can be looked up in the following list:

1. https://meine.doag.org/api/event/action.getCPEventAgenda/eventId.%d/
2. https://sessionize.com/api/v2/%s/view/Sessions

#### Custom Styles

You can modify the styles of the user interface using CSS variables. The CSS variables must be set using the environment variable `CUSTOM_STYLES` in key and value pairs. The key is the variable to be set and must start with two dashes (`--`). Keys and values must be separated by a colon (`:`). Multiple key and value pairs are separated by a semicolon (`;`). Example:

```
--name-of-variable-one:value1;--name-of-variable-two:value2
```

The following table contains the CSS variables you can modify to change the user interface:

| CSS Variable                   | Default | Description                                                        |
|--------------------------------|---------|--------------------------------------------------------------------|
| --event-background-color       | #007e89 | The color for the background of the event agenda.                  |
| --event-text-color             | #003861 | The color for the text of the event agenda.                        |
| --event-running-session-color  | #9bf4ff | The color for the background of rooms with running sessions.       |
| --event-next-session-color     | #62d7e3 | The color for the background of rooms with sessions starting next. |
| --event-closed-room-color      | #5da6b2 | The color for the background of closed rooms.                      |
| --event-room-border            | none    | The border for the event room.                                     |
| --social-background-color      | #1aa3b1 | The color for the background of the social wall.                   |
| --social-text-color            | #003861 | The color for the text of the social wall.                         |
| --social-post-background-color | #84ddee | The color for the background of social posts.                      |
| --social-post-border           | none    | The border for the social posts.                                   |

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

### Production Build

#### Maven

You can use [Maven](https://maven.apache.org/) to build *Apus* for production. Just specify the `production` profile. Example:

```shell
./mvnw clean package -Pproduction
```

#### Docker

To create a production build for *Apus* it is highly recommended to use [Docker](https://www.docker.com/) or [Podman](https://podman.io/). *Apus* comes with a complete dockerized self-contained build. You don't need to have Maven or Java installed, [Docker](https://www.docker.com/) or [Podman](https://podman.io/) is enough. The Docker build file contains everything needed, just start a standard Docker build with the following command:

```shell
docker build -t apus .
```

This might run for a while and will produce a Docker image tagged `apus` on your local system.

### Run in Production

It is highly recommended to use [Docker](https://www.docker.com/) or [Podman](https://podman.io/) to run *Apus* in production. Use the following command line as an example:

```shell
docker run \
    --name apus \
    -p 80:8080 \
    -v $HOME/.apus:/home/apus/.apus \
    -e ADMIN_PASSWORD=sEcrEt \
    -e DEMO_ROOM_COUNT=12 \
    -e MASTODON_INSTANCE=mastodon.social \
    -e MASTODON_HASHTAG=java \
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

## Plugin Support

### Event Plugins

*Apus* uses a simple plugin technology to import the agenda of various events. Plugins are currently available for the following events:

| Plugin       | Supported Events                             |
|--------------|----------------------------------------------|
| `DemoPlugin` | Creates fake session data for demo purposes. |
| `DoagPlugin` | CloudLand, CyberLand, JavaLand, KI Navigator |

Plugins for other events are planned.

### Social Plugins

*Apus* uses a simple plugin technology to import posts from various social media services. Plugins are currently available for the following services:

| Plugin           | Supported Services |
|------------------|--------------------|
| `MastodonPlugin` | Mastodon           |

Plugins for other social media services are planned.

### Plugin Development

Everyone is welcome to contribute a plugin themselves. The implementation is very simple. There are two types of plugins: `EventPlugin` and `SocialPlugin`. For a new plugin, a new package is created under `swiss.fihlon.apus.plugin.event` or `swiss.fihlon.apus.plugin.social`, based on the plugin type. The implementation is carried out in this new package. Implement one of these two interfaces depending on the plugin type you want to contribute.

If your implementation requires a configuration, the `Configuration` class must be extended accordingly. Add a property and corresponding setters and getters in the marked sections. Implement the settings object as a `record` in your new plugin package. Take one of the existing plugins as a template. Default settings belong in the file `application.properties` and the corresponding schema is stored in `additional-spring-configuration-metadata.json`. Of course, this `README.md` must also be adapted.

## Communication

### Matrix Chat

There is a channel at Matrix for quick and easy communication. This is publicly accessible for everyone. For developers as well as users. The communication in this chat is to be regarded as short-lived and has no documentary character.

You can find our Matrix channel here: [@project-apus:ijug.eu](https://matrix.to/#/%23project-apus:ijug.eu)

### GitHub Discussions

We use the corresponding GitHub function for discussions. The discussions held here are long-lived and divided into categories for the sake of clarity. One important category, for example, is that for questions and answers.

Discussions on GitHub: https://github.com/McPringle/apus/discussions  
Questions and Answers: https://github.com/McPringle/apus/discussions/categories/q-a

## Contributing

### Good First Issues

To find possible tasks for your first contribution to *Apus*, we tagged some of the hopefully easier to solve issues as [good first issue](https://github.com/McPringle/apus/labels/good%20first%20issue).

If you prefer to meet people in real life to contribute to *Apus* together, we recommend to visit a [Hackergarten](https://www.hackergarten.net/) event. *Apus* is often selected as a contribution target in [Lucerne](https://www.meetup.com/hackergarten-luzern/), [Zurich](https://www.meetup.com/hackergarten-zurich/), and at the [JavaLand](https://www.javaland.eu/) conference.

Please join our developer community using our [Matrix chat](#matrix-chat) to get support and help for contributing to *Apus*.

### Sign-off your commits

It is important to sign-off *every* commit. That is a de facto standard way to ensure that *you* have the right to submit your content and that you agree to the [DCO](DCO.md) (Developer Certificate of Origin).

You can find more information about why this is important and how to do it easily in a very good [blog post](https://dev.to/janderssonse/git-signoff-and-signing-like-a-champ-41f3)  by Josef Andersson.

### Add an emoji to your commit

We love to add an emoji to the beginning of every commit message which relates to the nature of the change. You can find a searchable list of possible emojis and their meaning in the overview on the [gitmoji](https://gitmoji.dev/) website. If you prefer, you can also install one of the plugins that are available for almost all common IDEs.

### AI Generated Code

AI generated source code is based on real existing source code, which is copied in whole or in part into the generated code. The license of the original source code with which the AI was trained is not taken into account. It is therefore not clear which license conditions apply and how these can be complied with. For legal reasons, we therefore do not use AI-generated source code at all.

## Contributors

Special thanks for all these wonderful people who had helped this project so far ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/McPringle"><img src="https://avatars.githubusercontent.com/u/1254039?v=4?s=100" width="100px;" alt="Marcus Fihlon"/><br /><sub><b>Marcus Fihlon</b></sub></a><br /><a href="#projectManagement-McPringle" title="Project Management">📆</a> <a href="#ideas-McPringle" title="Ideas, Planning, & Feedback">🤔</a> <a href="https://github.com/McPringle/apus/commits?author=McPringle" title="Code">💻</a> <a href="#design-McPringle" title="Design">🎨</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/jcgueriaud1"><img src="https://avatars.githubusercontent.com/u/51313578?v=4?s=100" width="100px;" alt="Jean-Christophe Gueriaud"/><br /><sub><b>Jean-Christophe Gueriaud</b></sub></a><br /><a href="https://github.com/McPringle/apus/commits?author=jcgueriaud1" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/myyxl"><img src="https://avatars.githubusercontent.com/u/22593897?v=4?s=100" width="100px;" alt="Marlon"/><br /><sub><b>Marlon</b></sub></a><br /><a href="https://github.com/McPringle/apus/issues?q=author%3Amyyxl" title="Bug reports">🐛</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/tlangdun"><img src="https://avatars.githubusercontent.com/u/51236478?v=4?s=100" width="100px;" alt="tlangdun"/><br /><sub><b>tlangdun</b></sub></a><br /><a href="#tool-tlangdun" title="Tools">🔧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/MarkusBarthlen"><img src="https://avatars.githubusercontent.com/u/13293680?v=4?s=100" width="100px;" alt="MarkusBarthlen"/><br /><sub><b>MarkusBarthlen</b></sub></a><br /><a href="#tool-MarkusBarthlen" title="Tools">🔧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/eins78"><img src="https://avatars.githubusercontent.com/u/134942?v=4?s=100" width="100px;" alt="Max Albrecht"/><br /><sub><b>Max Albrecht</b></sub></a><br /><a href="#infra-eins78" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/StefanMallia"><img src="https://avatars.githubusercontent.com/u/5004438?v=4?s=100" width="100px;" alt="Stefan Mallia"/><br /><sub><b>Stefan Mallia</b></sub></a><br /><a href="https://github.com/McPringle/apus/commits?author=StefanMallia" title="Code">💻</a></td>
    </tr>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/jzfrank"><img src="https://avatars.githubusercontent.com/u/77217626?v=4?s=100" width="100px;" alt="jzfrank"/><br /><sub><b>jzfrank</b></sub></a><br /><a href="https://github.com/McPringle/apus/commits?author=jzfrank" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/Interactiondesigner"><img src="https://avatars.githubusercontent.com/u/17220369?v=4?s=100" width="100px;" alt="Interactiondesigner"/><br /><sub><b>Interactiondesigner</b></sub></a><br /><a href="#design-Interactiondesigner" title="Design">🎨</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/1tchy"><img src="https://avatars.githubusercontent.com/u/678713?v=4?s=100" width="100px;" alt="Itchy"/><br /><sub><b>Itchy</b></sub></a><br /><a href="https://github.com/McPringle/apus/commits?author=1tchy" title="Code">💻</a> <a href="#design-1tchy" title="Design">🎨</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/lokalesnetzwerk"><img src="https://avatars.githubusercontent.com/u/35575304?v=4?s=100" width="100px;" alt="Max"/><br /><sub><b>Max</b></sub></a><br /><a href="#design-lokalesnetzwerk" title="Design">🎨</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://adrianperez.me/"><img src="https://avatars.githubusercontent.com/u/9467708?v=4?s=100" width="100px;" alt="Adrian Perez"/><br /><sub><b>Adrian Perez</b></sub></a><br /><a href="#infra-adpe" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/patlecat"><img src="https://avatars.githubusercontent.com/u/422020?v=4?s=100" width="100px;" alt="patlecat"/><br /><sub><b>patlecat</b></sub></a><br /><a href="https://github.com/McPringle/apus/commits?author=patlecat" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/haladamateusz"><img src="https://avatars.githubusercontent.com/u/26378632?v=4?s=100" width="100px;" alt="Mateusz Halada"/><br /><sub><b>Mateusz Halada</b></sub></a><br /><a href="https://github.com/McPringle/apus/commits?author=haladamateusz" title="Code">💻</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

## Copyright and License

[AGPL License](https://www.gnu.org/licenses/agpl-3.0.de.html)

*Copyright (C) Marcus Fihlon and the individual contributors to **Apus**.*

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
