package react_test_renderer

import react.ReactElement
import kotlin.js.Json

@JsModule("react-test-renderer")
@JsNonModule
external class ReactTestRenderer {
    companion object Factory {
        fun create(element: ReactElement<*>): ReactTestRenderer
        fun act(block: () -> dynamic)
    }

    fun update(element: ReactElement<*>)
    fun toJSON(): Json
}

@Suppress("UNCHECKED_CAST")
fun <T> ReactTestRenderer.Factory.akt(block: () -> T): T {
    var result: T? = null

    act {
        result = block()
        undefined
    }

    return result as T
}

fun ReactTestRenderer.Factory.aktCreate(block: () -> ReactElement<*>): ReactTestRenderer {
    return akt {
        create(block())
    }
}

fun ReactTestRenderer.aktUpdate(block: () -> ReactElement<*>) {
    ReactTestRenderer.akt {
        update(block())
    }
}
