package io.akryl.rx

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MapTest : EmptyReactiveContainer {
  @Test
  fun getSetExists() {
    val map = observable(hashMapOf("foo" to "bar"))
    val result by computed { map["foo"] }
    assertEquals("bar", result)

    map["foo"] = "baz"
    assertEquals("baz", result)
  }

  @Test
  fun getSetAbsent() {
    val map = observable(hashMapOf<String, String>())
    val result by computed { map["foo"] }
    assertEquals(null, result)

    map["foo"] = "bar"
    assertEquals("bar", result)
  }

  @Test
  fun remove() {
    val map = observable(hashMapOf("foo" to "bar"))
    val result by computed { map["foo"] }
    assertEquals("bar", result)

    map.remove("foo")
    assertEquals(null, result)
  }

  @Test
  fun clear() {
    val map = observable(hashMapOf("foo" to "bar", "baz" to "qux"))
    val result by computed { map.toString() }
    assertEquals("{baz=qux, foo=bar}", result)

    map.clear()
    assertEquals("{}", result)
  }

  @Test
  fun mapToString() {
    val first = hashMapOf("foo" to "bar", "baz" to "qux")
    val second = observable(HashMap(first))
    assertEquals(first.toString(), second.toString())
  }

  @Test
  fun mapEquals() {
    val first = hashMapOf("foo" to "bar", "baz" to "qux")
    val second = observable(HashMap(first))
    assertEquals(second, first)
  }

  @Test
  fun mapHashCode() {
    val first = hashMapOf("foo" to "bar", "baz" to "qux")
    val second = observable(HashMap(first))
    assertEquals(first.hashCode(), second.hashCode())
  }

  @Test
  fun iterator() {
    val map = observable(hashMapOf("1" to "foo", "2" to "bar", "3" to "baz"))

    val result by computed {
      val s = ArrayList<String>()
      val iterator = map.iterator()
      while (iterator.hasNext()) {
        s.add(iterator.next().value)
      }
      s.joinToString()
    }
    assertEquals("foo, bar, baz", result)

    map["4"] = "qux"
    assertEquals("foo, bar, baz, qux", result)

    map["4"] = "new"
    assertEquals("foo, bar, baz, new", result)

    map.remove("1")
    assertEquals("bar, baz, new", result)

    map.clear()
    assertEquals("", result)

    map.putAll(mapOf("a" to "b", "c" to "d", "e" to "f"))
    assertEquals("b, d, f", result)

    val iterator2 = map.iterator()
    while (iterator2.hasNext()) {
      val entry = iterator2.next()
      if (entry.key == "a") {
        entry.setValue("bb")
      }
    }
    assertEquals("bb, d, f", result)

    val iterator3 = map.iterator()
    while (iterator3.hasNext()) {
      val entry = iterator3.next()
      if (entry.key == "e") {
        iterator3.remove()
      }
    }
    assertEquals("bb, d", result)
  }

  @Test
  fun keys() {
    val map = observable(hashMapOf("1" to "foo", "2" to "bar", "3" to "baz"))
    val result by computed { ArrayList(map.keys) }
    assertEquals(listOf("1", "2", "3"), result)

    map["4"] = "qux"
    assertEquals(listOf("1", "2", "3", "4"), result)

    map.remove("1")
    assertEquals(listOf("2", "3", "4"), result)
  }

  @Test
  fun values() {
    val map = observable(hashMapOf("1" to "foo", "2" to "bar", "3" to "baz"))
    val result by computed { ArrayList(map.values) }
    assertEquals(listOf("foo", "bar", "baz"), result)

    map["4"] = "qux"
    assertEquals(listOf("foo", "bar", "baz", "qux"), result)

    map.remove("1")
    assertEquals(listOf("bar", "baz", "qux"), result)
  }

  @Test
  fun containsKey() {
    val map = observable(hashMapOf("1" to "foo", "2" to "bar", "3" to "baz"))
    val result by computed { "1" in map }
    assertTrue(result)

    map.remove("1")
    assertFalse(result)

    map["1"] = "qux"
    assertTrue(result)
  }

  @Test
  fun containsValue() {
    val map = observable(hashMapOf("1" to "foo", "2" to "bar", "3" to "baz"))
    val result by computed { map.containsValue("foo") }
    assertTrue(result)

    map.remove("1")
    assertFalse(result)

    map["1"] = "foo"
    assertTrue(result)

    map["1"] = "qux"
    assertFalse(result)
  }

  @Test
  fun deepInitial() {
    class TestClass {
      var a = 10
    }

    val map = observable(hashMapOf("1" to TestClass()))
    val result by computed { map["1"]?.a }
    assertEquals(10, result)

    map["1"]?.a = 20
    assertEquals(20, result)
  }

  @Test
  fun deepSet() {
    class TestClass {
      var a = 10
    }

    val map = observable(hashMapOf<String, TestClass>())
    val result by computed { map["1"]?.a }
    assertEquals(null, result)

    map["1"] = TestClass()
    assertEquals(10, result)

    map["1"]?.a = 20
    assertEquals(20, result)
  }

  @Test
  fun deepIteratorSet() {
    class TestClass {
      var a = 10
    }

    val map = observable(hashMapOf("1" to TestClass()))
    val result by computed { map["1"]?.a }
    assertEquals(10, result)

    val iterator = map.iterator()
    iterator.next().setValue(TestClass())
    assertEquals(10, result)

    map["1"]?.a = 20
    assertEquals(20, result)
  }

  @Test
  fun deepRemove() {
    class TestClass {
      var a = 10
    }

    val map = observable(hashMapOf("1" to TestClass()))
    val result by computed { map["1"]?.a }
    assertEquals(10, result)

    map.remove("1")
    assertEquals(null, result)
  }

  @Test
  fun recursive() {
    class TestClass {
      var map: MutableMap<String, TestClass>? = null
    }

    val map = hashMapOf("1" to TestClass())
    map["1"]?.map = map
    observable(map)
  }

  // todo .withDefault()
}