package io.akryl

import react.React
import react_test_renderer.ReactTestRenderer
import react_test_renderer.akt
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

class HooksTest {
    @Test
    fun testState() {
        val emitter = EventEmitter()

        val root = ReactTestRenderer.akt {
            ReactTestRenderer.create(
                counterComponent(emitter)
            )
        }

        for (i in 0..2) {
            assertJsonEquals(
                json(
                    "type" to "div",
                    "props" to json(),
                    "children" to arrayOf(i.toString())
                ),
                root.toJSON()
            )

            ReactTestRenderer.akt {
                emitter.emit()
            }
        }
    }

    @Test
    fun testSimpleEffect() {
        val sideEffect = Value("")

        val root = ReactTestRenderer.akt {
            ReactTestRenderer.create(
                simpleSideEffectComponent(sideEffect, "foo")
            )
        }

        assertEquals("foo", sideEffect.value)

        ReactTestRenderer.akt {
            root.update(
                simpleSideEffectComponent(sideEffect, "bar")
            )
        }

        assertEquals("bar", sideEffect.value)
    }

    @Test
    fun testDependenciesEffect() {
        val sideEffect = Value(0)

        val root = ReactTestRenderer.akt {
            ReactTestRenderer.create(
                dependenciesSideEffectComponent(sideEffect, "foo")
            )
        }
        assertEquals(1, sideEffect.value)

        ReactTestRenderer.akt {
            root.update(
                dependenciesSideEffectComponent(sideEffect, "foo")
            )
        }
        assertEquals(1, sideEffect.value)

        ReactTestRenderer.akt {
            root.update(
                dependenciesSideEffectComponent(sideEffect, "bar")
            )
        }
        assertEquals(2, sideEffect.value)
    }
}