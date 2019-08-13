package io.akryl.css

import kotlin.test.Test
import kotlin.test.assertEquals

class CssTest {
  @Test
  fun simpleClass() {
    val actual = cssString(".foo") {
      padding(16.px)
      margin(32.px, 1.em)
    }
    val expected = """
      .foo {
        padding: 16px;
        margin: 32px 1em;
      }
    """.trimIndent()
    assertEquals(expected, actual)
  }

  @Test
  fun innerClassSelector() {
    val bar = ClassName("bar")

    val actual = cssString(".foo") {
      width(100.px)
      bar {
        width(200.px)
      }
    }
    val expected = """
      .foo {
        width: 100px;
      }
      .foo .bar {
        width: 200px;
      }
    """.trimIndent()
    assertEquals(expected, actual)
  }

  @Test
  fun innerPseudoSelector() {
    val actual = cssString(".foo") {
      width(100.px)
      hover {
        width.initial()
      }
    }
    val expected = """
      .foo {
        width: 100px;
      }
      .foo:hover {
        width: initial;
      }
    """.trimIndent()
    assertEquals(expected, actual)
  }

  @Test
  fun innerStringSelector() {
    val actual = cssString(".foo") {
      width(100.px)
      "&.bar :hover" {
        width.initial()
      }
    }
    val expected = """
      .foo {
        width: 100px;
      }
      .foo.bar :hover {
        width: initial;
      }
    """.trimIndent()
    assertEquals(expected, actual)
  }

  @Test
  fun topLevelSelectors() {
    val foo = ClassName("foo")
    val bar = ClassName("bar")

    val actual = cssString("") {
      foo {
        width(100.px)
      }
      bar {
        height(200.px)
      }
      div {
        width(300.px)
      }
      div[foo] {
        width(400.px)
      }
      div[foo + bar] {
        width(500.px)
      }
    }
    val expected = """
      .foo {
        width: 100px;
      }
      .bar {
        height: 200px;
      }
      div {
        width: 300px;
      }
      div.foo {
        width: 400px;
      }
      div.foo.bar {
        width: 500px;
      }
    """.trimIndent()
    assertEquals(expected, actual)
  }

  @Test
  fun important() {
    val actual = cssString(".foo") {
      padding(16.px).important()
    }
    val expected = """
      .foo {
        padding: 16px !important;
      }
    """.trimIndent()
    assertEquals(expected, actual)
  }

  @Test
  fun complexProps() {
    val actual = cssString(".foo") {
      boxShadow(1.px, 2.px, 3.px, 4.px, Color.red)
      boxShadow(4.px, 3.px, 2.px, 1.px, Color.green)

      transform
        .translate(10.px, 20.px)
        .rotate(45.deg)
    }
    val expected = """
      .foo {
        box-shadow: 1px 2px 3px 4px red, 4px 3px 2px 1px green;
        transform: translate(10px, 20px) rotate(45deg);
      }
    """.trimIndent()
    assertEquals(expected, actual)
  }
}