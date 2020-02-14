package utils

import kotlin.test.assertEquals

fun assertJsonEquals(expected: dynamic, actual: dynamic) {
    assertEquals(JSON.stringify(expected), JSON.stringify(actual))
}
