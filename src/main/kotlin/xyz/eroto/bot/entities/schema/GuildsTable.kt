package xyz.eroto.bot.entities.schema

import me.aurieh.ares.exposed.pg.pgArray
import org.jetbrains.exposed.sql.Table

object GuildsTable : Table() {
    val id = long("id")
    val prefixes = pgArray<String>("prefixes", "VARCHAR")
    val mutedRole = text("mutedRole")
}