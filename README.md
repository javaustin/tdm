## TDM
Team Deathmatch (TDM) is a team-based Minecraft minigame in which two opposing teams battle to earn more kills than the other team. This particular version involves starter kits, customizable gun items, and player power-ups. This plugin is built on top of the [CXYZ](https://github.com/javaustin/cxyz) core plugin.

---

## Important Notes
- This project depends on the [CXYZ](https://github.com/javaustin/cxyz) for its core plugin.
- **NO** generative AI was used to write or modify any code in this project.

--- 

## Features
- **Easy Setup:** Use the `/tdm` command to easily create, join, or customize your TDM games.
- **Guns!!**: Use the four pre-made presets or define your own guns to create an awesome shooter experience
- **Player Kits**: Completely customize player kits using configuration files
- **Power Ups**: Reward lucky players with unique power-up pickups around the map
- **Auto Join**: Allow players to automatically join a game upon joining the world or server
- **Scoreboard GUI**: Lobby and in-game scoreboards with configurable info
- **Discord Support:** Set up a Discord webhook integration to post game data directly to your discord!
- **Event-based automation**: Automatically run commands on death, on kill, game start, etc.
- **Click-based automation**: Allow players to right or left click items as a shortcut to commands
- **Unlimited Customization**: Customize every single message in the plugin using the [messages.yml](https://github.com/javaustin/tdm/blob/main/src/main/resources/messages.yml) file

--- 

## Installation

### Requirements

- A Bukkit server running version `1.21` or above
- [Apache Maven](https://projects.apache.org/project.html?maven) on your local machine (if building the project)
- The [**CXYZ**](https://github.com/javaustin/cxyz) plugin installed on the server

### Build
###### *Note: This step is not required. You can use the already-provided jars in the `/target` directory*

Run the following Maven command in the project directory:
```bash
mvn clean package
```

The shaded plugin jar will be created in `target/`.

### Install on a server

1. Build the plugin jar or use the provided jars in `target/`.
2. Copy the generated jar into your server’s `plugins/` directory.
3. Install the **CXYZ** plugin on the same server, since TDM depends on it at runtime.
4. Restart your server to install the plugin – this will generate the required config files.
5. Once finished, modify `config.yml` and `maps.yml` to support your server. Restart to apply changes.

### Configuration
[View config.yml](https://github.com/javaustin/tdm/blob/main/src/main/resources/config.yml)  
[View kits.yml](https://github.com/javaustin/tdm/blob/main/src/main/resources/kits.yml)  
[View maps.yml](https://github.com/javaustin/tdm/blob/main/src/main/resources/maps.yml)  
[View messages.yml](https://github.com/javaustin/tdm/blob/main/src/main/resources/messages.yml)  
