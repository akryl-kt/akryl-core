package io.akryl.rx

import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleClassTest : EmptyReactiveContainer {
  @Test
  fun primitives() {
    assertEquals(null, observable(null))
    assertEquals(10, observable(10))
    assertEquals(true, observable(true))
    assertEquals(1.2, observable(1.2))
    assertEquals("foo", observable("foo"))
  }

  @Test
  fun constructorField() {
    class TestClass(var a: Int, var b: Int)

    val obj = observable(TestClass(1, 2))
    val a by computed { obj.a }
    val b by computed { obj.b }
    val sum by computed { obj.a + obj.b }

    assertEquals(1, a)
    assertEquals(2, b)
    assertEquals(3, sum)

    obj.a = 10
    assertEquals(10, a)
    assertEquals(2, b)
    assertEquals(12, sum)

    obj.b = 20
    assertEquals(10, a)
    assertEquals(20, b)
    assertEquals(30, sum)
  }

  @Test
  fun properties() {
    class TestClass {
      var a: Int = 1
      var b: Int = 2
    }

    val obj = observable(TestClass())
    val a by computed { obj.a }
    val b by computed { obj.b }
    val sum by computed { obj.a + obj.b }

    assertEquals(1, a)
    assertEquals(2, b)
    assertEquals(3, sum)

    obj.a = 10
    assertEquals(10, a)
    assertEquals(2, b)
    assertEquals(12, sum)

    obj.b = 20
    assertEquals(10, a)
    assertEquals(20, b)
    assertEquals(30, sum)
  }

  @Test
  fun accessors() {
    class TestClass {
      private var inner = 1
      val outer get() = inner + 1

      fun increment() {
        inner += 1
      }
    }

    val obj = observable(TestClass())
    val result by computed { obj.outer }
    assertEquals(2, result)

    obj.increment()
    assertEquals(3, result)
  }

  @Test
  fun deep() {
    class Second {
      var value = 10
    }

    class First {
      var second = Second()
    }

    val first = observable(First())
    val value by computed { first.second.value }
    assertEquals(10, value)

    first.second.value = 20
    assertEquals(20, value)
  }

  @Test
  fun recursive() {
    class TestClass(var value: Int) {
      var inner: TestClass? = null
    }

    val obj = TestClass(10)
    val a = TestClass(20)
    val b = TestClass(30)
    obj.inner = a
    a.inner = b
    b.inner = obj

    observable(obj)
    assertEquals(obj, obj.inner?.inner?.inner)

    val objValue by computed { obj.inner?.inner?.inner?.value }
    val aValue by computed { obj.inner?.value }
    val bValue by computed { obj.inner?.inner?.value }

    assertEquals(10, objValue)
    assertEquals(20, aValue)
    assertEquals(30, bValue)

    obj.value = 100
    assertEquals(100, objValue)

    a.value = 200
    assertEquals(200, aValue)
  }

  @Test
  fun withComputed() {
    class TestClass : ReactiveContainer {
      var a = 10
      val b by computed { a + 10 }

      override fun registerReactiveHandle(handle: ReactiveHandle) {}
    }

    val obj = observable(TestClass())
    assertEquals(20, obj.b)

    obj.a = 20
    assertEquals(30, obj.b)
  }

  // todo $magicProps
}