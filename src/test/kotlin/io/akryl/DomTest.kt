package io.akryl

import io.akryl.html.*
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

  @Test
  fun children() {
    val ref = fakeMount(Div(
      Label(`for` = "name", text = "name"),
      Input(id = "name", type = "text"),
      Button(type = "submit")
    ))
    assertHtml("<div><label for='name'>name</label><input id='name' type='text'/><button type='submit'/></div>", ref)

    ref.rebuild(Div(
      Span(text = "success!")
    ))
    assertHtml("<div><span>success!</span></div>", ref)
  }

  @Test
  fun rebuildChildren1() {
    val ref = mountTest(Div(
      Div(clazz = "prepend"),
      Label(key = ValueKey("label"), `for` = "name", text = "name"),
      Input(key = ValueKey("input"), id = "name", type = "text"),
      Button(key = ValueKey("button"), type = "submit"),
      Div(clazz = "append")
    ))

    val div1 = ref.element.node as TestDomNode
    val prepend1 = div1.children[0]
    val label1 = div1.children[1]
    val input1 = div1.children[2]
    val button1 = div1.children[3]
    val append1 = div1.children[4]

    ref.rebuildTest(Div(
      Div(clazz = "prepend"),
      Button(key = ValueKey("button"), type = "submit"),
      Label(key = ValueKey("label"), `for` = "name", text = "name"),
      Input(key = ValueKey("input"), id = "name", type = "text"),
      Div(clazz = "append")
    ))

    val div2 = ref.element.node as TestDomNode
    val prepend2 = div2.children[0]
    val label2 = div2.children[2]
    val input2 = div2.children[3]
    val button2 = div2.children[1]
    val append2 = div2.children[4]

    assertSame(prepend1, prepend2)
    assertSame(label1, label2)
    assertSame(input1, input2)
    assertSame(button1, button2)
    assertSame(append1, append2)
  }

  @Test
  fun rebuildChildren2() {
    val ref = mountTest(Div(
      Div(clazz = "prepend"),
      Label(key = ValueKey("label"), `for` = "name", text = "name"),
      Input(key = ValueKey("input"), id = "name", type = "text"),
      Button(key = ValueKey("button"), type = "submit"),
      Div(clazz = "append")
    ))

    val div1 = ref.element.node as TestDomNode
    val input1 = div1.children[2]
    val button1 = div1.children[3]
    val append1 = div1.children[4]

    ref.rebuildTest(Div(
      Button(key = ValueKey("button"), type = "submit"),
      Input(key = ValueKey("input"), id = "name", type = "text"),
      Div(clazz = "append")
    ))

    val div2 = ref.element.node as TestDomNode
    val input2 = div2.children[1]
    val button2 = div2.children[0]
    val append2 = div2.children[2]

    assertSame(input1, input2)
    assertSame(button1, button2)
    assertNotSame(append1, append2)
  }

  @Test
  fun rebuildChildren3() {
    val ref = mountTest(Div(
      Div(key = ValueKey("a"), text = "a"),
      Div(key = ValueKey("b"), text = "b"),
      Div(key = ValueKey("c"), text = "c")
    ))

    val div1 = ref.element.node as TestDomNode
    val a1 = div1.children[0]
    val b1 = div1.children[1]
    val c1 = div1.children[2]

    ref.rebuildTest(Div(
      Div(key = ValueKey("c"), text = "c"),
      Div(key = ValueKey("b"), text = "b"),
      Div(key = ValueKey("a"), text = "a")
    ))

    val div2 = ref.element.node as TestDomNode
    val a2 = div2.children[2]
    val b2 = div2.children[1]
    val c2 = div2.children[0]

    assertSame(a1, a2)
    assertSame(b1, b2)
    assertSame(c1, c2)
  }

  @Test
  fun rebuildChildren4() {
    val ref = mountTest(Div(
      Div(key = ValueKey("a"), text = "a")
    ))

    ref.rebuildTest(Div(
      Div(key = ValueKey("c"), text = "c"),
      Div(key = ValueKey("a"), text = "a"),
      Div(key = ValueKey("b"), text = "b")
    ))
  }

  @Test
  fun rebuildChildren5() {
    val ref = mountTest(Div(
      Div(text = "a")
    ))

    ref.rebuildTest(Div(
      Div(text = "c"),
      Div(text = "a"),
      Div(text = "b")
    ))
  }

  @Test
  fun rebuildChildren6() {
    val ref = mountTest(Div(
      Div(clazz = "prepend"),
      Label(key = ValueKey("label"), `for` = "name", text = "name"),
      Input(key = ValueKey("input"), id = "name", type = "text"),
      Button(key = ValueKey("button"), type = "submit")
    ))

    val div1 = ref.element.node as TestDomNode
    val prepend1 = div1.children[0]
    val input1 = div1.children[2]
    val button1 = div1.children[3]

    ref.rebuildTest(Div(
      Div(clazz = "prepend"),
      Button(key = ValueKey("button"), type = "submit"),
      Input(key = ValueKey("input"), id = "name", type = "text")
    ))

    val div2 = ref.element.node as TestDomNode
    val prepend2 = div2.children[0]
    val input2 = div2.children[2]
    val button2 = div2.children[1]

    assertSame(input1, input2)
    assertSame(button1, button2)
    assertSame(prepend1, prepend2)
  }
}