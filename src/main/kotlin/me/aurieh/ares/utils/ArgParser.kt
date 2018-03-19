/*
 *  Copyright 2017 aurieh <me@aurieh.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.aurieh.ares.utils

class UnclosedQuoteError(msg: String) : Exception(msg)

object ArgParser {
    fun tokenize(str: String): List<String> {
        val tokens = mutableListOf<String>()
        var escaping = false
        var quoteChar = ' '
        var quoting = false
        var intermediate = StringBuilder()
        val iter = str.iterator()
        while (iter.hasNext()) {
            val char = iter.next()
            if (escaping) {
                intermediate.append(char)
                escaping = false
            } else if (char == '\\' && !(quoting && quoteChar == '\'')) {
                escaping = true
            } else if (quoting && char == quoteChar) {
                quoting = false
            } else if (!quoting && (char == '\'' || char == '"')) {
                quoting = true
                quoteChar = char
            } else if (!quoting && char.isWhitespace()) {
                if (intermediate.isNotEmpty()) {
                    tokens.add(intermediate.toString())
                    intermediate = StringBuilder()
                }
            } else {
                intermediate.append(char)
            }
        }
        if (quoting) {
            throw UnclosedQuoteError("unclosed quote")
        }
        if (intermediate.isNotEmpty()) {
            tokens.add(intermediate.toString())
        }
        return tokens
    }

    private fun String.splitOnFirst(delimiter: Char): Pair<String, String> {
        val first = StringBuilder()
        val second = StringBuilder()
        var isSecond = false

        for (char in toCharArray()) {
            if (!isSecond && char == delimiter) {
                isSecond = true
                continue
            }
            if (isSecond) {
                second.append(char)
            } else {
                first.append(char)
            }
        }

        return first.toString() to second.toString()
    }

    fun parsePosix(tokens: List<String>): ParsedResult {
        val unmatched = mutableListOf<String>()
        val argMap = mutableMapOf<String, String?>()
        var nextKey: String? = null

        for (token in tokens) {
            if (token.startsWith('-')) {
                if (nextKey != null) {
                    argMap[nextKey] = null
                    nextKey = null
                }

                val cut = if (token.startsWith("--")) 2 else 1
                val kv = token.drop(cut)

                val (k, v) = kv.splitOnFirst('=')
                if (k.isEmpty()) {
                    throw EmptyKeyError("empty key while reading \"$token\"", token)
                }

                if (cut > 1) { // long form
                    if (v.isEmpty()) {
                        nextKey = k
                    } else {
                        argMap[k] = v
                    }
                } else { // short form
                    val lastK = k.last().toString()
                    if (v.isEmpty()) {
                        nextKey = lastK
                    } else {
                        argMap[lastK] = v
                    }
                    val head = k.dropLast(1).map { it.toString() }
                    head.forEach { argMap[it] = null }
                }
            } else if (nextKey != null) {
                argMap[nextKey] = token
                nextKey = null
            } else {
                unmatched.add(token)
            }
        }

        return ParsedResult(unmatched, argMap)
    }

    class EmptyKeyError(msg: String, val token: String) : Exception(msg)

    data class ParsedResult(val unmatched: List<String>, val argMap: Map<String, String?>)
}