/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package samples.collections

import samples.*
import kotlin.test.*

@RunWith(Enclosed::class)
class Iterables {

    class Building {

        @Sample
        fun iterable() {
            val iterable = Iterable {
                iterator {
                    yield(42)
                    yieldAll(1..5 step 2)
                }
            }
            val result = iterable.mapIndexed { index, value -> "$index: $value" }
            assertPrints(result, "[0: 42, 1: 1, 2: 3, 3: 5]")

            // can be iterated many times
            repeat(2) {
                val sum = iterable.sum()
                assertPrints(sum, "51")
            }
        }

    }

    class Operations {

        @Sample
        fun flattenIterable() {
            val deepList = listOf(listOf(1), listOf(2, 3), listOf(4, 5, 6))
            assertPrints(deepList.flatten(), "[1, 2, 3, 4, 5, 6]")
        }

        @Sample
        fun unzipIterable() {
            val list = listOf(1 to 'a', 2 to 'b', 3 to 'c')
            assertPrints(list.unzip(), "([1, 2, 3], [a, b, c])")
        }

        @Sample
        fun zipIterable() {
            val listA = listOf("a", "b", "c")
            val listB = listOf(1, 2, 3, 4)
            assertPrints(listA zip listB, "[(a, 1), (b, 2), (c, 3)]")
        }

        @Sample
        fun zipIterableWithTransform() {
            val listA = listOf("a", "b", "c")
            val listB = listOf(1, 2, 3, 4)
            val result = listA.zip(listB) { a, b -> "$a$b" }
            assertPrints(result, "[a1, b2, c3]")
        }

        @Sample
        fun partition() {
            data class Person(val name: String, val age: Int) {
                override fun toString(): String {
                    return "$name - $age"
                }
            }

            val list = listOf(Person("Tom", 18), Person("Andy", 32), Person("Sarah", 22))
            val result = list.partition { it.age < 30 }
            assertPrints(result, "([Tom - 18, Sarah - 22], [Andy - 32])")
        }
    }

    class Sorting {
        @Sample
        fun isSorted() {
            val sortedList = listOf(1, 2, 3, 4, 5)
            assertPrints(sortedList.isSorted(), "true")

            val unsortedList = listOf(1, 3, 2, 4, 5)
            assertPrints(unsortedList.isSorted(), "false")

            val emptyList = emptyList<Int>()
            assertPrints(emptyList.isSorted(), "true")
        }

        @Sample
        fun isSortedBy() {
            data class Person(val name: String, val age: Int)

            val people = listOf(Person("Carol", 20), Person("Bob", 25), Person("Alice", 30))
            assertPrints(people.isSortedBy { it.age }, "true")
            assertPrints(people.isSortedBy { it.name }, "false") // "Carol" > "Bob" > "Alice"
        }

        @Sample
        fun isSortedByDescending() {
            data class Person(val name: String, val age: Int)

            val people = listOf(Person("Alice", 30), Person("Bob", 25), Person("Carol", 20))
            assertPrints(people.isSortedByDescending { it.age }, "true")
            assertPrints(people.isSortedByDescending { it.name }, "false") // "Alice" < "Bob" < "Carol"
        }

        @Sample
        fun isSortedDescending() {
            val sortedDescending = listOf(5, 4, 3, 2, 1)
            assertPrints(sortedDescending.isSortedDescending(), "true")

            val unsorted = listOf(5, 3, 4, 2, 1)
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedWith() {
            val byLength = compareBy<String> { it.length }

            val sorted = listOf("a", "bb", "ccc")
            assertPrints(sorted.isSortedWith(byLength), "true")

            val unsorted = listOf("bb", "a", "ccc")
            assertPrints(unsorted.isSortedWith(byLength), "false")
        }
    }
}
