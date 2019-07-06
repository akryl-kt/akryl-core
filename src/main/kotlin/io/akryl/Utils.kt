@file:Suppress("FunctionName")

package io.akryl

fun <T, R> For(items: Collection<T>, mapper: (T) -> R): Array<R> = items.map(mapper).toTypedArray()
fun <T, R> For(items: Array<T>, mapper: (T) -> R): Array<R> = items.map(mapper).toTypedArray()
fun <T, R> ForOf(vararg items: T, mapper: (T) -> R): Array<R> = items.map(mapper).toTypedArray()

fun <R> If(condition: Boolean, onTrue: () -> R): Array<R> = if (condition) arrayOf(onTrue()) else emptyArray()
fun <T, R> IfNotNull(condition: T?, onTrue: (T) -> R): Array<R> = if (condition != null) arrayOf(onTrue(condition)) else emptyArray()

infix fun <R> Array<out R>.Else(onElse: () -> R): Array<out R> = if (this.isNotEmpty()) this else arrayOf(onElse())
