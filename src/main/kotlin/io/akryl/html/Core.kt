@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package io.akryl.html

import io.akryl.Component
import io.akryl.RefProperty
import io.akryl.react.ReactNode
import io.akryl.react.createElement
import io.akryl.react.createTextElement
import org.w3c.dom.events.Event
import kotlin.js.json

fun html(
  tag: String,
  attributes: Map<String, Any?> = emptyMap(),
  style: Map<String, String?> = emptyMap(),
  listeners: Map<String, (event: Event) -> Unit> = emptyMap(),
  children: List<ReactNode> = emptyList(),
  innerHtml: String? = null,
  key: Any? = null,
  ref: RefProperty<*>? = null
): ReactNode {
  val styleProps = if (style.isNotEmpty()) js("{}") else undefined

  for ((k, v) in style) {
    if (v != null) styleProps[k] = v
  }

  val props = js("{}")
  props["style"] = styleProps

  for ((k, v) in attributes) {
    if (v != null) props[k] = v
  }

  for ((k, v) in listeners) {
    props[k] = v
  }

  if (innerHtml != null) {
    props["dangerouslySetInnerHTML"] = json("__html" to innerHtml)
  }

  if (key != null) {
    props["key"] = key
  }

  if (ref != null) {
    props["ref"] = ref.inner
  }

  return createElement(tag, props, *Array(children.size) { Component.build(children[it]) })
}

fun Text(value: String) = createTextElement(value)
