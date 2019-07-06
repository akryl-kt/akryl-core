package io.akryl

import kotlin.test.*

private data class Bar(
  var a: String,
  var b: Any?
)

private data class Foo(
  var a: Int,
  var b: String,
  var c: MutableMap<String, Bar>,
  var d: MutableList<Bar>
)

private data class Baz(
  var a: Bar?
)

private class WithComputed(bar: Bar) : EmptyComputedPropertyContainer {
  var first = 10
  val second by computed { "first = $first, bar = $bar" }
}

private class LateInit {
  lateinit var foo: String
}

private fun init() = observable(Foo(
  a = 1,
  b = "str",
  c = hashMapOf(
    "first" to Bar("a", "aa"),
    "second" to Bar("b", 12.34)
  ),
  d = arrayListOf(Bar("a", 1), Bar("b", 2))
))

class ProxyReactiveTest : EmptyComputedPropertyContainer {
  @Test
  fun simpleObservable() {
    val bar = observable(Bar("1", 1))
    val result by computed { bar.a }
    assertEquals("1", result)
    bar.a = "2"
    assertEquals("2", result)
  }

  @Test
  fun objObservable() {
    val baz = observable(Baz(Bar("1", 1)))
    val result by computed { baz.toString() }
    assertEquals("Baz(a=Bar(a=1, b=1))", result)
    baz.a?.a = "2"
    assertEquals("Baz(a=Bar(a=2, b=1))", result)
  }

  @Test
  fun changePrimitiveOnRoot() {
    val tree = init()
    val str by computed { tree.toString() }
    assertEquals(tree.toString(), str)
    tree.a = 10
    assertEquals("Foo(a=10, b=str, c={first=Bar(a=a, b=aa), second=Bar(a=b, b=12.34)}, d=[Bar(a=a, b=1), Bar(a=b, b=2)])", str)
  }

  @Test
  fun changeObjectOnMap() {
    val tree = init()
    val str by computed { tree.toString() }
    assertEquals(tree.toString(), str)
    tree.c["first"] = Bar("b", "bb")
    assertEquals("Foo(a=1, b=str, c={first=Bar(a=b, b=bb), second=Bar(a=b, b=12.34)}, d=[Bar(a=a, b=1), Bar(a=b, b=2)])", str)
  }

  @Test
  fun changeObjectPropertyOnMap() {
    val tree = init()
    val str by computed { tree.toString() }
    assertEquals(tree.toString(), str)
    tree.c["first"]?.a = "b"
    assertEquals("Foo(a=1, b=str, c={first=Bar(a=b, b=aa), second=Bar(a=b, b=12.34)}, d=[Bar(a=a, b=1), Bar(a=b, b=2)])", str)
  }

  @Test
  fun addObjectOnMap() {
    val tree = init()
    val str by computed { tree.toString() }
    assertEquals(tree.toString(), str)
    tree.c["third"] = Bar("1", 11)
    assertEquals("Foo(a=1, b=str, c={first=Bar(a=a, b=aa), third=Bar(a=1, b=11), second=Bar(a=b, b=12.34)}, d=[Bar(a=a, b=1), Bar(a=b, b=2)])", str)
  }

  @Test
  fun changePropInAddedObjectOnMap() {
    val tree = init()
    val str by computed { tree.toString() }
    assertEquals(tree.toString(), str)
    tree.c["third"] = Bar("1", 11)
    tree.c["third"]?.a = "2"
    assertEquals("Foo(a=1, b=str, c={first=Bar(a=a, b=aa), third=Bar(a=2, b=11), second=Bar(a=b, b=12.34)}, d=[Bar(a=a, b=1), Bar(a=b, b=2)])", str)
  }

  @Test
  fun removeObjectOnMap() {
    val tree = init()
    val str by computed { tree.toString() }
    assertEquals(tree.toString(), str)
    tree.c.remove("second")
    assertEquals("Foo(a=1, b=str, c={first=Bar(a=a, b=aa)}, d=[Bar(a=a, b=1), Bar(a=b, b=2)])", str)
  }

  @Test
  fun changeObjectOnList() {
    val tree = init()
    val str by computed { tree.toString() }
    assertEquals(tree.toString(), str)
    tree.d[0] = Bar("aa", 11)
    assertEquals("Foo(a=1, b=str, c={first=Bar(a=a, b=aa), second=Bar(a=b, b=12.34)}, d=[Bar(a=aa, b=11), Bar(a=b, b=2)])", str)
  }

  @Test
  fun changeObjectPropOnList() {
    val tree = init()
    val str by computed { tree.toString() }
    assertEquals(tree.toString(), str)
    tree.d[0].a = "aa"
    assertEquals("Foo(a=1, b=str, c={first=Bar(a=a, b=aa), second=Bar(a=b, b=12.34)}, d=[Bar(a=aa, b=1), Bar(a=b, b=2)])", str)
  }

  @Test
  fun addObjectOnList() {
    val tree = init()
    val str by computed { tree.toString() }
    assertEquals(tree.toString(), str)
    tree.d.add(Bar("aa", 11))
    assertEquals("Foo(a=1, b=str, c={first=Bar(a=a, b=aa), second=Bar(a=b, b=12.34)}, d=[Bar(a=a, b=1), Bar(a=b, b=2), Bar(a=aa, b=11)])", str)
  }

  @Test
  fun changePropInAddedObjectOnList() {
    val tree = init()
    val str by computed { tree.toString() }
    assertEquals(tree.toString(), str)
    tree.d.add(Bar("aa", 11))
    tree.d[2].a = "aaa"
    assertEquals("Foo(a=1, b=str, c={first=Bar(a=a, b=aa), second=Bar(a=b, b=12.34)}, d=[Bar(a=a, b=1), Bar(a=b, b=2), Bar(a=aaa, b=11)])", str)
  }

  @Test
  fun removeObjectOnList() {
    val tree = init()
    val str by computed { tree.toString() }
    assertEquals(tree.toString(), str)
    tree.d.removeAt(0)
    assertEquals("Foo(a=1, b=str, c={first=Bar(a=a, b=aa), second=Bar(a=b, b=12.34)}, d=[Bar(a=b, b=2)])", str)
  }

  @Test
  fun assignObservableTwice() {
    val first = observable(Baz(a = null))
    val second = observable(Baz(a = null))
    val bar = Bar("a", "a")
    val result by computed { second.a?.a }
    assertNull(result)

    first.a = bar
    second.a = bar
    second.a?.a = "b"

    assertEquals("b", result)

    first.a?.a = "c"

    assertEquals("c", result)
  }

  @Test
  fun unsubscribeOnChange() {
    val first = ProxyObservable()
    val second = ProxyObservable()
    var callCount = 0
    var subscriptionsCount = 0

    for (i in 1..10) {
      ChangeDetector.evaluate({
        ChangeDetector.observed(first)
        ChangeDetector.observed(second)
      }, {
        callCount += 1
        subscriptionsCount = second.count
      })

      first.fire()
    }

    assertEquals(10, callCount)
    assertEquals(0, subscriptionsCount)
  }

  @Test
  fun disposeHandle() {
    val first = ProxyObservable()
    var called = false

    val handle = ChangeDetector.evaluate({
      ChangeDetector.observed(first)
    }, {
      called = true
    }).second

    handle.dispose()
    first.fire()

    assertFalse(called)
  }

  @Test
  fun withComputed() {
    val a = observable(Bar("a", 11))
    val b = observable(WithComputed(a))
    assertEquals("first = 10, bar = Bar(a=a, b=11)", b.second)

    b.first = 20
    assertEquals("first = 20, bar = Bar(a=a, b=11)", b.second)

    a.a = "b"
    assertEquals("first = 20, bar = Bar(a=b, b=11)", b.second)
  }

  @Test
  fun lateInit() {
    val obj = observable(LateInit())
    obj.foo = "foo"
  }

  @Test
  fun computedNotTriggeredIfNotChanged() {
    val obj = observable(Bar("1", 1))
    var called = false
    val result = observable(WithComputed(obj))

    ChangeDetector.evaluate({
      assertEquals("first = 10, bar = Bar(a=1, b=1)", result.second)
    }, {
      called = true
    })

    obj.b = 1
    assertFalse(called)
  }

  @Test
  fun detectorNotTriggeredIfNotChanged() {
    val obj = observable(Bar("1", 1))
    var called = false

    ChangeDetector.evaluate({
      assertEquals(1, obj.b)
    }, {
      called = true
    })

    obj.b = 1
    assertFalse(called)
  }

  @Test
  fun computedPropertyContainer() {
    var prop: ComputedProperty<*, *>? = null

    class Container : ComputedPropertyContainer {
      override val isInitialized = true

      var first = 10
      val second by computed { first + 10 }

      override fun registerComputedProperty(computedProperty: ComputedProperty<*, *>) {
        assertNull(prop)
        prop = computedProperty
      }
    }

    val container = observable(Container())
    assertNotNull(prop)
    assertEquals(20, container.second)

    container.first = 20
    assertEquals(30, container.second)

    prop?.dispose()
    container.first = 30
    assertEquals(30, container.second)
  }

  @Test
  fun recomputeUntilContainerInitialized() {
    var initialized = false
    var callCount = 0

    class Container : ComputedPropertyContainer {
      override val isInitialized get() = initialized

      var first = 10
      val second by computed {
        callCount += 1
        first + 10
      }

      override fun registerComputedProperty(computedProperty: ComputedProperty<*, *>) {}
    }

    val container = observable(Container())
    for (i in 1..3) {
      assertEquals(20, container.second)
    }
    assertEquals(3, callCount)

    initialized = true
    for (i in 1..10) {
      assertEquals(20, container.second)
    }
    assertEquals(4, callCount)
  }
}