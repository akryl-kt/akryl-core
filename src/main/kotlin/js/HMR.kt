package js

external val module: Module

external interface Module {
  val hot: Hot?
}

external interface Hot {
  val data: dynamic

  fun accept()
  fun accept(dependency: String, callback: () -> Unit)
  fun accept(dependencies: Array<String>, callback: (updated: Array<String>) -> Unit)

  fun dispose(callback: (data: dynamic) -> Unit)
}

external fun require(name: String): dynamic

object HMR {
  private val dispose = ArrayList<(dynamic) -> Unit>()

  fun <T> singleton(name: String, builder: () -> T) = lazy {
    val saved = module.hot?.data

    val singleton = if (saved != null && saved != undefined) {
      saved[name]
    } else {
      builder()
    }

    dispose.add { data ->
      data[name] = singleton
    }

    singleton.unsafeCast<T>()
  }

  fun dispose(block: (dynamic) -> Unit) {
    dispose.add(block)
  }

  fun accept() {
    val hot = module.hot

    if (hot != null) {
      hot.accept()

      hot.dispose { data ->
        dispose.forEach {
          it(data)
        }
        dispose.clear()
      }
    }
  }
}