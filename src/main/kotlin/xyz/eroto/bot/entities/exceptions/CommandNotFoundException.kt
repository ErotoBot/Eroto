package xyz.eroto.bot.entities.exceptions

class CommandNotFoundException(val cmd: String) : Exception("Command $cmd not found")