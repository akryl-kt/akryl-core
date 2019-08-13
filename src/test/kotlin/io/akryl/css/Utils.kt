package io.akryl.css

fun cssString(selector: String, block: RuleSet): String {
  return buildString {
    stringify(selector, CssBuilder().apply(block))
  }.trimIndent()
}