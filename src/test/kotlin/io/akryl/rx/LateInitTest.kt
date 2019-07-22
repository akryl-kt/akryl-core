package io.akryl.rx

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LateInitTest {
  @Test
  fun notInitialized() {
    val value by lateInit<Int>()
    assertFailsWith<UninitializedPropertyAccessException> {
      assertEquals(0, value)
    }
  }

  @Test
  fun initialized() {
    var value by lateInit<Int>()
    value = 10
    assertEquals(10, value)
  }
}