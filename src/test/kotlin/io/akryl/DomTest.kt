package io.akryl

import io.akryl.html.Div
import io.akryl.html.Span
import io.akryl.html.classMap
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class DomTest : Styled {
  override val prefix: String? = null
  override fun style(): Style? = null

  @Test
  fun mount() {
    val ref = fakeMount(Div())
    assertHtml("<div/>", ref)

    ref.rebuild(Span())
    assertHtml("<span/>", ref)
  }

  @Test
  fun preserveElementWithSameTag() {
    val ref = fakeMount(Div(clazz = "foo"))
    val element1 = ref.element

    ref.rebuild(Div(clazz = "bar"))
    val element2 = ref.element

    assertSame(element1, element2)
  }

  @Test
  fun recreateElementWithDifferentTag() {
    val ref = fakeMount(Div(clazz = "foo"))
    val element1 = ref.element

    ref.rebuild(Span(clazz = "foo"))
    val element2 = ref.element

    assertNotSame(element1, element2)
  }

  @Test
  fun preserveElementWithSameKey() {
    val ref = fakeMount(Div(key = ValueKey("foo")))
    val element1 = ref.element

    ref.rebuild(Div(key = ValueKey("foo")))
    val element2 = ref.element

    assertSame(element1, element2)
  }

  @Test
  fun recreateElementWithDifferentKey() {
    val ref = fakeMount(Div(key = ValueKey("foo")))
    val element1 = ref.element

    ref.rebuild(Div(key = ValueKey("bar")))
    val element2 = ref.element

    assertNotSame(element1, element2)
  }

  @Test
  fun setAttribute() {
    val ref = fakeMount(Div(id = "test", title = "some title"))
    assertHtml("<div id='test' title='some title'/>", ref)
  }

  @Test
  fun setStyle() {
    val ref = fakeMount(Div(style = mapOf("width" to "30px", "height" to "50%")))
    assertHtml("<div style='width: 30px; height: 50%'/>", ref)
  }

  @Test
  fun testClasses() {
    val ref = fakeMount(Div(
      classes = classMap(
        "foo" to true,
        "bar" to false,
        "baz" to true
      ),
      clazz = "base-class"
    ))

    assertHtml("<div class='foo baz base-class'/>", ref)

    ref.rebuild(Div(
      classes = classMap(
        "foo" to false,
        "bar" to true,
        "baz" to true
      )
    ))

    assertHtml("<div class='bar baz'/>", ref)
  }
}