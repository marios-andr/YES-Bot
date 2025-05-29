# Y.E.S. Bot
A multifunctional discord application built using JDA.

Y.E.S. Bot provides multiple commands primarly for fun, and also for server management.

## Features

#### Dashboard
- This application hosts a web server on port 7070 with an administrative dashboard.
- Through the dashboard, any administrator, with the correct credentials can perform operations such as:
  - Sending messages and pinging in any guild.
  - Joining/Leaving voice channels
  - Muting/Unmuting all users in the current voice channel.
  - Viewing the current or past console logs
  - Locking/Unlocking all bot interactions
  - Shutting down the bot
  - Resetting all schedules such as game announcements.
#### Databases
- Has built-in databases, using either MongoDB or a local json file, depending on the settings.
#### Chess!
- The application provides several commands allowing you to play chess with your friends, completely simulated within the bot.
#### Free Game Announcements
- Epic games often provides free games every week. The application can detect such games and send a pretty message in the provided text channel to inform you of the games.
#### Commands
- Most commands have support for both legacy prefix (!) and the newest (/) command system, with some exceptions.
- Descriptions for all available commands can be acquired using the /help command.
- Utility Commands:
  - /help [command]: Get all commands or alternatively put a command of your choice after 'help' to get a description of that command.
  - /ping : The ping of the bot.
  - /profile [target]: Displays relevant information about the player or yourself.
  - /rng {min} {max}: Get a pseudorandom number between the two numbers.
  - /add_announcement {announcement_type} {announcement_channel}: Adds an announcement type in the given channel to be announced whenever an update comes.
  - !spam [message]: Spam your favorite person!
  - /stop : Stop any repeating commands. Only works for owner.
- Voice Commands:
  - /join : Make the bot join the voice channel you're in.
  - /leave : Make the bot leave the voice channel you're in.
  - /mute_all : Mute all people in the voice channel.
  - /unmute_all : Unmute all people in the voice channel.
- Fun Commands:
  - /pickup {target} : sends funny pickup lines.
  - /roast {target} : sends funny roast lines.

## Setting Up
### Prerequisites
- Java 18+
- Gradle
- A Discord bot token ([how to get one](https://discord.com/developers/applications))

### Clone and Build
```bash
#Clone the repository
git clone https://github.com/marios-andr/YES-Bot.git
#Build the gradle project and produce a jar file.
#Note: when building, two jar files will be produced, one contains all libraries and the other does not.
#      typically you'll want the one with the -all suffix.
./gradlew build
```

### Launching

At first launch, the program will create a settings.json file in the folder in which the .jar file exists,
and will then exit. You must fill out the information in each field of the settings.json in order for the application
to work as intended.

#### Example settings.json
```json
{
   "token": "Token acquired from https://discord.com/developers/applications",
   "bot_snowflake": "Your bot's snowflake ID, can be acquired by right clicking the bot while in Developer Mode.",
   "owner_snowflake": "Your own snowflake ID, same as above.",
   "mongo_link": "If you have a mongo db you can put its link here, otherwise a local database will be used instead.",
   "reddit_username": "Currently unavailable",
   "reddit_password": "Currently unavailable",
   "reddit_client": "Currently unavailable",
   "reddit_secret": "Currently unavailable",
   "steam_token": "Currently unavailable",
   "credentials": [
      {
         "name": "The credentials list is a list of all username and passwords",
         "password": "That will be accepted when entered in the prompt in the dashboard"
      }
   ]
}
```