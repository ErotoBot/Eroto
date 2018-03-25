package xyz.eroto.bot.entities.schema

import me.aurieh.ares.exposed.pg.pgArray
import org.jetbrains.exposed.sql.Table

object GuildsTable : Table() {
    val id = long("id").primaryKey().uniqueIndex()
    val prefixes = pgArray<String>("prefixes", "VARCHAR")
    val mutedRole = long("mutedRole").nullable()
    val perms = pgArray<String>("perms", "VARCHAR")
}