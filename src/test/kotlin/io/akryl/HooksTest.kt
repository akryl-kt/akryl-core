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

private fun counterInitializerComponent(emitter: EventEmitter<Unit>) = component {
    val (state, setState) = useState { 0 }
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

private fun disposeSideEffectComponent(sideEffect: Value<Int>, disposeResult: Value<Int>, value: String) = component {
    useEffect(listOf(value)) {
        sideEffect.value += 1
        dispose { disposeResult.value += 1 }
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

private fun debugValueComponent() = component {
    useDebugValue("test")
    null
}

private fun memoComponent(renderCount: Value<Int>, memoCount: Value<Int>, dependency: String) = component {
    renderCount.value += 1
    val result = useMemo(listOf(dependency)) {
        memoCount.value += 1
        dependency
    }
    React.createElement("div", null, result)
}

private fun ComponentScope.useCounter(emitter: EventEmitter<Unit>): Int {
    val (count, setCount) = useState(0)
    val cb = useCallback(listOf(count)) {
        setCount(count + 1)
    }
    emitter { cb() }
    return count
}

private fun customHookComponent(emitter: EventEmitter<Unit>) = component {
    val count = useCounter(emitter)
    React.createElement("div", null, count.toString())
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
    fun testStateWithInitializer() {
        val emitter = EventEmitter<Unit>()

        val root = ReactTestRenderer.aktCreate {
            counterInitializerComponent(emitter)
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
    fun testDisposeEffect() {
        val sideEffect = Value(0)
        val disposeResult = Value(0)

        val root = ReactTestRenderer.aktCreate {
            disposeSideEffectComponent(sideEffect, disposeResult, "foo")
        }
        assertEquals(1, sideEffect.value)
        assertEquals(0, disposeResult.value)

        root.aktUpdate {
            disposeSideEffectComponent(sideEffect, disposeResult, "bar")
        }
        assertEquals(2, sideEffect.value)
        assertEquals(1, disposeResult.value)

        root.aktUpdate {
            React.createElement("div")
        }
        assertEquals(2, sideEffect.value)
        assertEquals(2, disposeResult.value)
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

    @Test
    fun testDebugValueRuns() {
        ReactTestRenderer.aktCreate {
            debugValueComponent()
        }
    }

    @Test
    fun testMemo() {
        val renderCount = Value(0)
        val memoCount = Value(0)

        val root = ReactTestRenderer.aktCreate {
            memoComponent(renderCount, memoCount, "a")
        }
        assertEquals(1, renderCount.value)
        assertEquals(1, memoCount.value)
        assertContent("a", root)

        root.aktUpdate {
            memoComponent(renderCount, memoCount, "a")
        }
        assertEquals(2, renderCount.value)
        assertEquals(1, memoCount.value)
        assertContent("a", root)

        root.aktUpdate {
            memoComponent(renderCount, memoCount, "b")
        }
        assertEquals(3, renderCount.value)
        assertEquals(2, memoCount.value)
        assertContent("b", root)
    }

    @Test
    fun testCustomHook() {
        val emitter = EventEmitter<Unit>()

        val root = ReactTestRenderer.aktCreate {
            customHookComponent(emitter)
        }
        assertContent("0", root)

        ReactTestRenderer.akt {
            emitter.emit(Unit)
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
