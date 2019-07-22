package io.akryl.rx

import kotlin.test.Test
import kotlin.test.assertEquals

class ArrayTest : EmptyReactiveContainer {
  @Test
  fun getSet() {
    val arr = observable(Array<String?>(10) { null })
    val result by computed { "${arr[0]} ${arr[1]}" }
    assertEquals("null null", result)

    arr[0] = "foo"
    assertEquals("foo null", result)

    arr[1] = "bar"
    assertEquals("foo bar", result)
  }

  @Test
  fun map() {
    val arr = observable(Array(3) { 0 })
    val result by computed { arr.map { it + 1 } }
    assertEquals(listOf(1, 1, 1), result)

    arr[0] = 1
    arr[1] = 2
    arr[2] = 3
    assertEquals(listOf(2, 3, 4), result)
  }

  @Test
  fun reduce() {
    val arr = observable(Array(3) { 0 })
    val result by computed { arr.max() }
    assertEquals(0, result)

    arr[1] = 1
    assertEquals(1, result)

    arr[2] = 2
    assertEquals(2, result)

    arr[1] = 3
    assertEquals(3, result)
  }
}