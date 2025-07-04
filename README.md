# Menmu Discord Bot

A mostly music discord bot written in Java using the Discord4J library.

![Menma](https://cdn.menma.dev/menmu/assets/images/menma.1bfb9c572a77fe08409cdbefb4b16e2f.jpg)

### [Invite Menmu to your discord server](https://discord.com/oauth2/authorize?client_id=340909847312596992&permissions=8&integration_type=0&scope=bot)

## Installation

For easy installation you can use `Docker` to download the image from Amazon ECR like below:

```bash
docker pull public.ecr.aws/i4h9h7p4/menmu-discord-bot:latest
```

## Running with Docker

To run the bot you need to set the following environment variables:

- `BOT_TOKEN` - Discord Bot Token
- `YT_API_KEY` - Youtube API Key

After setting them, run the docker container image:

```bash
docker run -d -e BOT_TOKEN -e YT_API_KEY public.ecr.aws/i4h9h7p4/menmu-discord-bot:latest
```

Or directly assign the variables on the command:

```bash
docker run -d -e BOT_TOKEN=YourBotToken -e YT_API_KEY=YourYoutubeApiKey public.ecr.aws/i4h9h7p4/menmu-discord-bot:latest
```
