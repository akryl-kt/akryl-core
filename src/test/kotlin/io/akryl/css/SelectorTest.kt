package io.akryl.css

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class SelectorTest : TagSelectors {
  @Test
  fun tagSelector() {
    assertEquals("div", div.selector)
  }

  @Test
  fun classSelector() {
    assertEquals(".foo", ClassName("foo").selector)
  }

  @Test
  fun classListSelector() {
    val foo = ClassName("foo")
    val bar = ClassName("bar")
    assertEquals(".foo.bar", (foo + bar).selector)
  }

  @Test
  fun tagAndClassSelector() {
    val foo = ClassName("foo")
    val bar = ClassName("bar")
    assertEquals("div.foo.bar", div[foo + bar].selector)
  }

  @Test
  fun randomClassSelector() {
    val name = ClassName.random("foo", "-", Random(123))
    assertEquals("foo-EDw39I", name.value)
  }
}