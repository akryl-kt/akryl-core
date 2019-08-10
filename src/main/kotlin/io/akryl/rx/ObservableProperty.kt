package io.akryl.rx

class ObservableProperty : Observable {
  private val subscriptions = HashSet<Observer>()

  val subscriptionsCount get() = subscriptions.size

  override fun subscribe(observer: Observer) {
    subscriptions.add(observer)
  }

  override fun unsubscribe(observer: Observer) {
    subscriptions.remove(observer)
  }

  fun fire() {
    val subs = ArrayList(subscriptions)
    subscriptions.clear()
    subs.forEach { it.changed() }
  }

  fun observed() {
    ChangeDetector.observed(this)
  }
}