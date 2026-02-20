// KT-66100: AssertionError: Expected an exception of class IndexOutOfBoundsException to be thrown, but was completed successfully.
// IGNORE_BACKEND: JS_IR, JS_IR_ES6
// IGNORE_KLIB_RUNTIME_ERRORS_WITH_CUSTOM_SECOND_STAGE: WASM-JS:2.3
// ^^^ Uncaught Runtime error
// WITH_STDLIB
import kotlin.test.*

class Foo(size: Int) {
    val array = IntArray(size)
}

class Bar {
    val smallFoo = Foo(1)
    val largeFoo = Foo(10)

    val smallArray = smallFoo.array
    val largeArray = largeFoo.array
}

fun box(): String {
    val bar = Bar()

    assertFailsWith<IndexOutOfBoundsException> {
        for (index in 0 until bar.largeArray.size) {
            bar.smallArray[index] = 6
        }
    }
    return "OK"
}
