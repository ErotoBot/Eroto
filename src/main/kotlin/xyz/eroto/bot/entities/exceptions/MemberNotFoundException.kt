package xyz.eroto.bot.entities.exceptions

class MemberNotFoundException(val input: String) : Exception("No members found for $input")