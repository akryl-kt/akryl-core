package io.akryl

import react.React
import utils.assertJsonEquals
import kotlin.js.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private fun emptyComponent() = component {
    React.createElement("div")
}

private fun emptyMemo() = memo {
    React.createElement("div")
}

private fun componentWithProps(a: Int, b: String) = component {
    React.createElement("div", json("a" to a, "b" to b))
}

private fun memoWithProps(a: Int, b: String) = memo {
    React.createElement("div", json("a" to a, "b" to b))
}

class ComponentTest {
    @Test
    fun testEmptyComponent() {
        val node = emptyComponent()
        assertTrue(React.isValidElement(node))
        assertEquals("emptyComponent", node.type.name)
    }

    @Test
    fun testEmptyMemo() {
        val node = emptyMemo()
        assertTrue(React.isValidElement(node))
        assertNotNull(node.type.`$$typeof`)
        assertEquals("emptyMemo", node.type.type.name)
    }

    @Test
    fun testComponentWithProps() {
        val node = componentWithProps(10, "str")
        assertTrue(React.isValidElement(node))
        assertEquals("componentWithProps", node.type.name)
        assertJsonEquals(json("a" to 10, "b" to "str"), node.props)
    }

    @Test
    fun testMemoWithProps() {
        val node = memoWithProps(10, "str")
        assertTrue(React.isValidElement(node))
        assertEquals("memoWithProps", node.type.type.name)
        assertJsonEquals(json("a" to 10, "b" to "str"), node.props)
    }

    @Test
    fun testKey() {
        val node = React.createElement("div", null, listOf("child")).withKey("foobar")
        assertTrue(React.isValidElement(node))
        assertJsonEquals(json("children" to arrayOf("child")), node.props)
        assertEquals("foobar", node.key)
    }
}