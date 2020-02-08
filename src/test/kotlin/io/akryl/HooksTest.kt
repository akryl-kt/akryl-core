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

private class EventEmitter<T> {
    private lateinit var event: (T) -> Unit

    operator fun invoke(event: (T) -> Unit) {
        this.event = event
    }

    fun emit(value: T) = event(value)
}

private class Value<T>(var value: T)

private fun counterComponent(emitter: EventEmitter<Unit>) = component {
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
    useEffect(listOf(value)) {
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

private fun dependenciesCallbackComponent(dependency: String, callback: () -> Int) = component {
    val cb = useCallback(listOf(dependency), callback)
    React.createElement("div", null, cb().toString())
}

private fun simpleCallbackComponent(callback: () -> Int) = component {
    val cb = useCallback { callback() }
    React.createElement("div", null, cb().toString())
}

private fun permanentCallbackComponent(callback: () -> Int) = component {
    val cb = useCallback(emptyList()) { callback() }
    React.createElement("div", null, cb().toString())
}

private fun refComponent(eventEmitter: EventEmitter<Int>) = component {
    val ref = useRef(0)
    eventEmitter { ref.current = it }
    React.createElement("div", null, ref.current.toString())
}

class HooksTest {
    @Test
    fun testState() {
        val emitter = EventEmitter<Unit>()

        val root = ReactTestRenderer.aktCreate {
            counterComponent(emitter)
        }

        for (i in 0..2) {
            assertContent(i.toString(), root)

            ReactTestRenderer.akt {
                emitter.emit(Unit)
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
    fun testSimpleCallback() {
        val root = ReactTestRenderer.aktCreate {
            simpleCallbackComponent { 10 }
        }
        assertContent("10", root)

        root.aktUpdate {
            simpleCallbackComponent { 20 }
        }
        assertContent("20", root)
    }

    @Test
    fun testPermanentCallback() {
        val root = ReactTestRenderer.aktCreate {
            permanentCallbackComponent { 10 }
        }
        assertContent("10", root)

        root.aktUpdate {
            permanentCallbackComponent { 20 }
        }
        assertContent("10", root)
    }

    @Test
    fun testDependenciesCallback() {
        val root = ReactTestRenderer.aktCreate {
            dependenciesCallbackComponent("first") { 10 }
        }
        assertContent("10", root)

        root.aktUpdate {
            dependenciesCallbackComponent("first") { 20 }
        }
        assertContent("10", root)

        root.aktUpdate {
            dependenciesCallbackComponent("second") { 20 }
        }
        assertContent("20", root)
    }

    @Test
    fun testRef() {
        val emitter = EventEmitter<Int>()

        val root = ReactTestRenderer.aktCreate {
            refComponent(emitter)
        }
        assertContent("0", root)

        ReactTestRenderer.akt {
            emitter.emit(1)
        }
        assertContent("0", root)

        root.aktUpdate {
            refComponent(emitter)
        }
        assertContent("1", root)
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