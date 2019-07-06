package io.akryl

import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import kotlin.experimental.and

const val LEFT_BUTTON: Short = 1

object Keys {
  const val ENTER = 13
}

typealias VoidCallback = () -> Unit
typealias EventCallback<E> = (event: E) -> Unit

val VoidCallback.left get() = { event: MouseEvent ->
  if ((event.buttons and LEFT_BUTTON) == LEFT_BUTTON) {
    this()
  }
}

val EventCallback<MouseEvent>.left get() = { event: MouseEvent ->
  if ((event.buttons and LEFT_BUTTON) == LEFT_BUTTON) {
    this(event)
  }
}

val VoidCallback.stop get() = { event: Event ->
  this()
  event.stopPropagation()
}

val <E : Event> EventCallback<E>.stop get() = { event: E ->
  this(event)
  event.stopPropagation()
}

val VoidCallback.prevent get() = { event: Event ->
  this()
  event.preventDefault()
}

val EventCallback<KeyboardEvent>.enter get() = { event: KeyboardEvent ->
  if (event.keyCode == Keys.ENTER) {
    this(event)
  }
}
