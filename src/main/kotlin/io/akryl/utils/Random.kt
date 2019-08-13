package io.akryl.utils

import kotlin.random.Random

private const val RANDOM_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

internal fun Random.nextString(length: Int)
  = (0 until length).map { RANDOM_CHARS[this.nextInt(RANDOM_CHARS.length)] }.joinToString("")
