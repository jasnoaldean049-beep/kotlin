// KT-66100: AssertionError: Expected an exception of class IndexOutOfBoundsException to be thrown, but was completed successfully.
// IGNORE_BACKEND: JS_IR, JS_IR_ES6
// IGNORE_KLIB_RUNTIME_ERRORS_WITH_CUSTOM_SECOND_STAGE: WASM-JS:2.3
// ^^^ Uncaught Runtime error
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    val array = Array(10) { 0 }

    assertFailsWith<IndexOutOfBoundsException> {
        array.forEachIndexed { index, _ ->
            array[index + 1] = 1
        }
    }
    return "OK"
}
