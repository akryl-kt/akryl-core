package io.akryl

import react.React
import react_test_renderer.ReactTestRenderer
import react_test_renderer.akt
import react_test_renderer.aktCreate
import react_test_renderer.aktUpdate
import utils.assertJsonEquals
import kotlin.js.json
import kotlin.test.Test
import kotlin.test.assertEquals

private class EventEmitter {
    private lateinit var event: () -> Unit

    operator fun invoke(event: () -> Unit) {
        this.event = event
    }

    fun emit() = event()
}

private class Value<T>(var value: T)

private fun counterComponent(emitter: EventEmitter) = component {
    val (state, setState) = useState(0)
    emitter { setState(state + 1) }
    React.createElement("div", null, state.toString())
}

private fun simpleSideEffectComponent(sideEffect: Value<String>, value: String) = component {
    useEffect {
        sideEffect.value = value
    }
    null
}

private fun dependenciesSideEffectComponent(sideEffect: Value<Int>, value: String) = component {
    useEffect(arrayOf(value)) {
        sideEffect.value += 1
    }
    null
}

data class TestContext(
    val value: String
)

private val testContext = React.createContext<TestContext?>(null)

private fun contextComponent() = component {
    val data = useContext(testContext)
    React.createElement("div", null, data?.value.toString())
}

private fun callbackComponent(dependency: String, callback: () -> Int) = component {
    val cb = useCallback(listOf(dependency), callback)
    React.createElement("div", null, cb().toString())
}

class HooksTest {
    @Test
    fun testState() {
        val emitter = EventEmitter()

        val root = ReactTestRenderer.aktCreate {
            counterComponent(emitter)
        }

        for (i in 0..2) {
            assertContent(i.toString(), root)

            ReactTestRenderer.akt {
                emitter.emit()
            }
        }
    }

    @Test
    fun testSimpleEffect() {
        val sideEffect = Value("")

        val root = ReactTestRenderer.aktCreate {
            simpleSideEffectComponent(sideEffect, "foo")
        }
        assertEquals("foo", sideEffect.value)

        root.aktUpdate {
            simpleSideEffectComponent(sideEffect, "bar")
        }
        assertEquals("bar", sideEffect.value)
    }

    @Test
    fun testDependenciesEffect() {
        val sideEffect = Value(0)

        val root = ReactTestRenderer.aktCreate {
            dependenciesSideEffectComponent(sideEffect, "foo")
        }
        assertEquals(1, sideEffect.value)

        root.aktUpdate {
            dependenciesSideEffectComponent(sideEffect, "foo")
        }
        assertEquals(1, sideEffect.value)

        root.aktUpdate {
            dependenciesSideEffectComponent(sideEffect, "bar")
        }
        assertEquals(2, sideEffect.value)
    }

    @Test
    fun testContextAbsent() {
        val root = ReactTestRenderer.aktCreate {
            contextComponent()
        }
        assertContent("null", root)
    }

    @Test
    fun testContextPresented() {
        val root = ReactTestRenderer.aktCreate {
            testContext.provider(
                value = TestContext(value = "foobar"),
                children = listOf(
                    contextComponent()
                )
            )
        }
        assertContent("foobar", root)
    }

    @Test
    fun testDependenciesCallback() {
        val root = ReactTestRenderer.aktCreate {
            callbackComponent("first") { 10 }
        }
        assertContent("10", root)

        root.aktUpdate {
            callbackComponent("first") { 20 }
        }
        assertContent("10", root)

        root.aktUpdate {
            callbackComponent("second") { 20 }
        }
        assertContent("20", root)
    }
}

private fun assertContent(expected: String, actual: ReactTestRenderer) {
    assertJsonEquals(
        json(
            "type" to "div",
            "props" to json(),
            "children" to arrayOf(expected)
        ),
        actual.toJSON()
    )
}