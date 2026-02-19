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
import kotlin.math.abs
import kotlin.test.*


@RunWith(Enclosed::class)
class Arrays {

    class Usage {

        @Sample
        fun arrayOrEmpty() {
            val nullArray: Array<Any>? = null
            assertPrints(nullArray.orEmpty().contentToString(), "[]")

            val array: Array<Char>? = arrayOf('a', 'b', 'c')
            assertPrints(array.orEmpty().contentToString(), "[a, b, c]")
        }

        @Sample
        fun arrayIsNullOrEmpty() {
            val nullArray: Array<Any>? = null
            assertTrue(nullArray.isNullOrEmpty())

            val emptyArray: Array<Any>? = emptyArray<Any>()
            assertTrue(emptyArray.isNullOrEmpty())

            val array: Array<Char>? = arrayOf('a', 'b', 'c')
            assertFalse(array.isNullOrEmpty())
        }

        @Sample
        fun arrayIfEmpty() {
            val emptyArray: Array<Any> = emptyArray()

            val emptyOrNull: Array<Any>? = emptyArray.ifEmpty { null }
            assertPrints(emptyOrNull, "null")

            val emptyOrDefault: Array<Any> = emptyArray.ifEmpty { arrayOf("default") }
            assertPrints(emptyOrDefault.contentToString(), "[default]")

            val nonEmptyArray = arrayOf(1)
            val sameArray = nonEmptyArray.ifEmpty { arrayOf(2) }
            assertTrue(nonEmptyArray === sameArray)
        }

        @Sample
        fun getOrElse() {
            val emptyArray: Array<Any> = emptyArray()
            assertPrints(emptyArray.getOrElse(0) { "default" }, "default")

            val array = arrayOf(1)
            assertPrints(array.getOrElse(0) { 0 }, "1")
            assertPrints(array.getOrElse(-1) { 0 }, "0")
            assertPrints(array.getOrElse(0) { "default" }, "1")
            assertPrints(array.getOrElse(-1) { "default" }, "default")

            // arrays of primitive types
            val intArray = intArrayOf(1, 2, 3)
            assertPrints(intArray.getOrElse(0) { 0 }, "1")
            assertPrints(intArray.getOrElse(-1) { 0 }, "0")

            val booleanArray = booleanArrayOf(true, false)
            assertPrints(booleanArray.getOrElse(0) { false }, "true")
            assertPrints(booleanArray.getOrElse(-1) { false }, "false")

            val charArray = charArrayOf('a', 'b', 'c')
            assertPrints(charArray.getOrElse(0) { 'z' }, "a")
            assertPrints(charArray.getOrElse(-1) { 'z' }, "z")

            // arrays of unsigned types
            val uIntArray = uintArrayOf(1u, 2u, 3u)
            assertPrints(uIntArray.getOrElse(0) { 10u }, "1")
            assertPrints(uIntArray.getOrElse(-1) { 10u }, "10")
        }
    }

    class Transformations {

        @Sample
        fun associateArrayOfPrimitives() {
            val charCodes = intArrayOf(72, 69, 76, 76, 79)

            val byCharCode = charCodes.associate { it to Char(it) }

            // 76=L only occurs once because only the last pair with the same key gets added
            assertPrints(byCharCode, "{72=H, 69=E, 76=L, 79=O}")
        }


        @Sample
        fun associateArrayOfPrimitivesBy() {
            val charCodes = intArrayOf(72, 69, 76, 76, 79)

            val byChar = charCodes.associateBy { Char(it) }

            // L=76 only occurs once because only the last pair with the same key gets added
            assertPrints(byChar, "{H=72, E=69, L=76, O=79}")
        }

        @Sample
        fun associateArrayOfPrimitivesByWithValueTransform() {
            val charCodes = intArrayOf(65, 65, 66, 67, 68, 69)

            val byUpperCase = charCodes.associateBy({ Char(it) }, { Char(it + 32) })

            // A=a only occurs once because only the last pair with the same key gets added
            assertPrints(byUpperCase, "{A=a, B=b, C=c, D=d, E=e}")
        }

        @Sample
        fun associateArrayOfPrimitivesByTo() {
            val charCodes = intArrayOf(72, 69, 76, 76, 79)
            val byChar = mutableMapOf<Char, Int>()

            assertTrue(byChar.isEmpty())
            charCodes.associateByTo(byChar) { Char(it) }

            assertTrue(byChar.isNotEmpty())
            // L=76 only occurs once because only the last pair with the same key gets added
            assertPrints(byChar, "{H=72, E=69, L=76, O=79}")
        }

        @Sample
        fun associateArrayOfPrimitivesByToWithValueTransform() {
            val charCodes = intArrayOf(65, 65, 66, 67, 68, 69)

            val byUpperCase = mutableMapOf<Char, Char>()
            charCodes.associateByTo(byUpperCase, { Char(it) }, { Char(it + 32) })

            // A=a only occurs once because only the last pair with the same key gets added
            assertPrints(byUpperCase, "{A=a, B=b, C=c, D=d, E=e}")
        }

        @Sample
        fun associateArrayOfPrimitivesTo() {
            val charCodes = intArrayOf(72, 69, 76, 76, 79)

            val byChar = mutableMapOf<Int, Char>()
            charCodes.associateTo(byChar) { it to Char(it) }

            // 76=L only occurs once because only the last pair with the same key gets added
            assertPrints(byChar, "{72=H, 69=E, 76=L, 79=O}")
        }

        @Sample
        fun flattenArray() {
            val deepArray = arrayOf(
                arrayOf(1),
                arrayOf(2, 3),
                arrayOf(4, 5, 6)
            )

            assertPrints(deepArray.flatten(), "[1, 2, 3, 4, 5, 6]")
        }

        @Sample
        fun unzipArray() {
            val array = arrayOf(1 to 'a', 2 to 'b', 3 to 'c')
            assertPrints(array.unzip(), "([1, 2, 3], [a, b, c])")
        }

        @Sample
        fun partitionArrayOfPrimitives() {
            val array = intArrayOf(1, 2, 3, 4, 5)
            val (even, odd) = array.partition { it % 2 == 0 }
            assertPrints(even, "[2, 4]")
            assertPrints(odd, "[1, 3, 5]")
        }
    }

    class ContentOperations {

        @Sample
        fun contentToString() {
            val array = arrayOf("apples", "oranges", "lime")

            assertPrints(array.contentToString(), "[apples, oranges, lime]")
        }

        @Sample
        fun contentDeepToString() {
            val matrix = arrayOf(
                intArrayOf(3, 7, 9),
                intArrayOf(0, 1, 0),
                intArrayOf(2, 4, 8)
            )

            assertPrints(matrix.contentDeepToString(), "[[3, 7, 9], [0, 1, 0], [2, 4, 8]]")
        }

        @Sample
        fun arrayContentEquals() {
            val array = arrayOf("apples", "oranges", "lime")

            // the same size and equal elements
            assertPrints(array.contentEquals(arrayOf("apples", "oranges", "lime")), "true")

            // different size
            assertPrints(array.contentEquals(arrayOf("apples", "oranges")), "false")

            // the elements at index 1 are not equal
            assertPrints(array.contentEquals(arrayOf("apples", "lime", "oranges")), "false")
        }

        @Sample
        fun charArrayContentEquals() {
            val array = charArrayOf('a', 'b', 'c')

            // the same size and equal elements
            assertPrints(array.contentEquals(charArrayOf('a', 'b', 'c')), "true")

            // different size
            assertPrints(array.contentEquals(charArrayOf('a', 'b')), "false")

            // the elements at index 1 are not equal
            assertPrints(array.contentEquals(charArrayOf('a', 'c', 'b')), "false")
        }

        @Sample
        fun booleanArrayContentEquals() {
            val array = booleanArrayOf(true, false, true)

            // the same size and equal elements
            assertPrints(array.contentEquals(booleanArrayOf(true, false, true)), "true")

            // different size
            assertPrints(array.contentEquals(booleanArrayOf(true, false)), "false")

            // the elements at index 1 are not equal
            assertPrints(array.contentEquals(booleanArrayOf(true, true, false)), "false")
        }

        @Sample
        fun intArrayContentEquals() {
            val array = intArrayOf(1, 2, 3)

            // the same size and equal elements
            assertPrints(array.contentEquals(intArrayOf(1, 2, 3)), "true")

            // different size
            assertPrints(array.contentEquals(intArrayOf(1, 2)), "false")

            // the elements at index 1 are not equal
            assertPrints(array.contentEquals(intArrayOf(1, 3, 2)), "false")
        }

        @Sample
        fun doubleArrayContentEquals() {
            val array = doubleArrayOf(1.0, Double.NaN, 0.0)

            // the same size and equal elements, NaN is equal to NaN
            assertPrints(array.contentEquals(doubleArrayOf(1.0, Double.NaN, 0.0)), "true")

            // different size
            assertPrints(array.contentEquals(doubleArrayOf(1.0, Double.NaN)), "false")

            // the elements at index 2 are not equal, 0.0 is not equal to -0.0
            assertPrints(array.contentEquals(doubleArrayOf(1.0, Double.NaN, -0.0)), "false")

            // the elements at index 1 are not equal
            assertPrints(array.contentEquals(doubleArrayOf(1.0, 0.0, Double.NaN)), "false")
        }

        @Sample
        fun contentDeepEquals() {
            val identityMatrix = arrayOf(
                intArrayOf(1, 0),
                intArrayOf(0, 1)
            )
            val reflectionMatrix = arrayOf(
                intArrayOf(1, 0),
                intArrayOf(0, -1)
            )

            // the elements at index [1][1] are not equal
            assertPrints(identityMatrix.contentDeepEquals(reflectionMatrix), "false")

            reflectionMatrix[1][1] = 1
            assertPrints(identityMatrix.contentDeepEquals(reflectionMatrix), "true")
        }
    }

    class CopyOfOperations {

        @Sample
        fun copyOf() {
            val array = arrayOf("apples", "oranges", "limes")
            val arrayCopy = array.copyOf()
            assertPrints(arrayCopy.contentToString(), "[apples, oranges, limes]")
        }

        @Sample
        fun resizingCopyOf() {
            val array = arrayOf("apples", "oranges", "limes")
            val arrayCopyPadded = array.copyOf(5)
            assertPrints(arrayCopyPadded.contentToString(), "[apples, oranges, limes, null, null]")
            val arrayCopyTruncated = array.copyOf(2)
            assertPrints(arrayCopyTruncated.contentToString(), "[apples, oranges]")
        }

        @Sample
        fun resizedPrimitiveCopyOf() {
            val array = intArrayOf(1, 2, 3)
            val arrayCopyPadded = array.copyOf(5)
            assertPrints(arrayCopyPadded.contentToString(), "[1, 2, 3, 0, 0]")
            val arrayCopyTruncated = array.copyOf(2)
            assertPrints(arrayCopyTruncated.contentToString(), "[1, 2]")
        }

        @Sample
        fun copyOfBooleanArrayWithInitializer() {
            val array = booleanArrayOf(true, false, true)
            val truncatedCopy = array.copyOf(2)
            assertPrints(truncatedCopy.contentToString(), "[true, false]")
            val paddedCopy = array.copyOf(5) { it % 2 == 0 }
            assertPrints(paddedCopy.contentToString(), "[true, false, true, false, true]")
        }

        @Sample
        fun copyOfCharArrayWithInitializer() {
            val array = charArrayOf('a', 'b', 'c')
            val truncatedCopy = array.copyOf(2)
            assertPrints(truncatedCopy.contentToString(), "[a, b]")
            val paddedCopy = array.copyOf(5) { '?' }
            assertPrints(paddedCopy.contentToString(), "[a, b, c, ?, ?]")
        }

        @Sample
        fun copyOfByteArrayWithInitializer() {
            val array = byteArrayOf(1, 2, 3)
            val truncatedCopy = array.copyOf(2)
            assertPrints(truncatedCopy.contentToString(), "[1, 2]")
            val paddedCopy = array.copyOf(5) { -1 }
            assertPrints(paddedCopy.contentToString(), "[1, 2, 3, -1, -1]")
            val paddedCopyWithIndex = array.copyOf(6) { it.toByte() }
            assertPrints(paddedCopyWithIndex.contentToString(), "[1, 2, 3, 3, 4, 5]")
        }

        @Sample
        fun copyOfShortArrayWithInitializer() {
            val array = shortArrayOf(1, 2, 3)
            val truncatedCopy = array.copyOf(2)
            assertPrints(truncatedCopy.contentToString(), "[1, 2]")
            val paddedCopy = array.copyOf(5) { -1 }
            assertPrints(paddedCopy.contentToString(), "[1, 2, 3, -1, -1]")
            val paddedCopyWithIndex = array.copyOf(6) { it.toShort() }
            assertPrints(paddedCopyWithIndex.contentToString(), "[1, 2, 3, 3, 4, 5]")
        }

        @Sample
        fun copyOfIntArrayWithInitializer() {
            val array = intArrayOf(1, 2, 3)
            val truncatedCopy = array.copyOf(2)
            assertPrints(truncatedCopy.contentToString(), "[1, 2]")
            val paddedCopy = array.copyOf(5) { -1 }
            assertPrints(paddedCopy.contentToString(), "[1, 2, 3, -1, -1]")
            val paddedCopyWithIndex = array.copyOf(6) { it }
            assertPrints(paddedCopyWithIndex.contentToString(), "[1, 2, 3, 3, 4, 5]")
        }

        @Sample
        fun copyOfLongArrayWithInitializer() {
            val array = longArrayOf(1, 2, 3)
            val truncatedCopy = array.copyOf(2)
            assertPrints(truncatedCopy.contentToString(), "[1, 2]")
            val paddedCopy = array.copyOf(5) { -1 }
            assertPrints(paddedCopy.contentToString(), "[1, 2, 3, -1, -1]")
            val paddedCopyWithIndex = array.copyOf(6) { it.toLong() }
            assertPrints(paddedCopyWithIndex.contentToString(), "[1, 2, 3, 3, 4, 5]")
        }

        @Sample
        fun copyOfFloatArrayWithInitializer() {
            val array = floatArrayOf(1.0f, 2.0f, 3.0f)
            val truncatedCopy = array.copyOf(2)
            assertPrints(truncatedCopy.contentToString(), "[1.0, 2.0]")
            val paddedCopy = array.copyOf(5) { -1.0f }
            assertPrints(paddedCopy.contentToString(), "[1.0, 2.0, 3.0, -1.0, -1.0]")
        }

        @Sample
        fun copyOfDoubleArrayWithInitializer() {
            val array = doubleArrayOf(1.0, 2.0, 3.0)
            val truncatedCopy = array.copyOf(2)
            assertPrints(truncatedCopy.contentToString(), "[1.0, 2.0]")
            val paddedCopy = array.copyOf(5) { -1.0 }
            assertPrints(paddedCopy.contentToString(), "[1.0, 2.0, 3.0, -1.0, -1.0]")
        }

        @Sample
        fun copyOfArrayWithInitializer() {
            val array = arrayOf("foo", "bar", "baz")
            val truncatedCopy = array.copyOf(2)
            assertPrints(truncatedCopy.contentToString(), "[foo, bar]")
            val paddedCopy = array.copyOf(5) { "qux" }
            assertPrints(paddedCopy.contentToString(), "[foo, bar, baz, qux, qux]")
        }

        @Sample
        fun copyOfUByteArrayWithInitializer() {
            val array = ubyteArrayOf(1u, 2u, 3u)
            val truncatedCopy = array.copyOf(2)
            assertPrints(truncatedCopy.contentToString(), "[1, 2]")
            val paddedCopy = array.copyOf(5) { 0xffu }
            assertPrints(paddedCopy.contentToString(), "[1, 2, 3, 255, 255]")
            val paddedCopyWithIndex = array.copyOf(6) { it.toUByte() }
            assertPrints(paddedCopyWithIndex.contentToString(), "[1, 2, 3, 3, 4, 5]")
        }

        @Sample
        fun copyOfUShortArrayWithInitializer() {
            val array = ushortArrayOf(1u, 2u, 3u)
            val truncatedCopy = array.copyOf(2)
            assertPrints(truncatedCopy.contentToString(), "[1, 2]")
            val paddedCopy = array.copyOf(5) { 0xffu }
            assertPrints(paddedCopy.contentToString(), "[1, 2, 3, 255, 255]")
            val paddedCopyWithIndex = array.copyOf(6) { it.toUShort() }
            assertPrints(paddedCopyWithIndex.contentToString(), "[1, 2, 3, 3, 4, 5]")
        }

        @Sample
        fun copyOfUIntArrayWithInitializer() {
            val array = uintArrayOf(1u, 2u, 3u)
            val truncatedCopy = array.copyOf(2)
            assertPrints(truncatedCopy.contentToString(), "[1, 2]")
            val paddedCopy = array.copyOf(5) { 0xffu }
            assertPrints(paddedCopy.contentToString(), "[1, 2, 3, 255, 255]")
            val paddedCopyWithIndex = array.copyOf(6) { it.toUInt() }
            assertPrints(paddedCopyWithIndex.contentToString(), "[1, 2, 3, 3, 4, 5]")
        }

        @Sample
        fun copyOfULongArrayWithInitializer() {
            val array = ulongArrayOf(1u, 2u, 3u)
            val truncatedCopy = array.copyOf(2)
            assertPrints(truncatedCopy.contentToString(), "[1, 2]")
            val paddedCopy = array.copyOf(5) { 0xffu }
            assertPrints(paddedCopy.contentToString(), "[1, 2, 3, 255, 255]")
            val paddedCopyWithIndex = array.copyOf(6) { it.toULong() }
            assertPrints(paddedCopyWithIndex.contentToString(), "[1, 2, 3, 3, 4, 5]")
        }
    }

    class Sorting {

        @Sample
        fun sortArray() {
            val intArray = intArrayOf(4, 3, 2, 1)

            // before sorting
            assertPrints(intArray.joinToString(), "4, 3, 2, 1")

            intArray.sort()

            // after sorting
            assertPrints(intArray.joinToString(), "1, 2, 3, 4")
        }

        @Sample
        fun sortArrayOfComparable() {
            class Person(val firstName: String, val lastName: String) : Comparable<Person> {
                override fun compareTo(other: Person): Int = this.lastName.compareTo(other.lastName)
                override fun toString(): String = "$firstName $lastName"
            }

            val people = arrayOf(
                Person("Ragnar", "Lodbrok"),
                Person("Bjorn", "Ironside"),
                Person("Sweyn", "Forkbeard")
            )

            // before sorting
            assertPrints(people.joinToString(), "Ragnar Lodbrok, Bjorn Ironside, Sweyn Forkbeard")

            people.sort()

            // after sorting
            assertPrints(people.joinToString(), "Sweyn Forkbeard, Bjorn Ironside, Ragnar Lodbrok")

        }

        @Sample
        fun sortRangeOfArray() {
            val intArray = intArrayOf(4, 3, 2, 1)

            // before sorting
            assertPrints(intArray.joinToString(), "4, 3, 2, 1")

            intArray.sort(0, 3)

            // after sorting
            assertPrints(intArray.joinToString(), "2, 3, 4, 1")
        }

        @Sample
        fun sortRangeOfArrayOfComparable() {
            class Person(val firstName: String, val lastName: String) : Comparable<Person> {
                override fun compareTo(other: Person): Int = this.lastName.compareTo(other.lastName)
                override fun toString(): String = "$firstName $lastName"
            }

            val people = arrayOf(
                Person("Ragnar", "Lodbrok"),
                Person("Bjorn", "Ironside"),
                Person("Sweyn", "Forkbeard")
            )

            // before sorting
            assertPrints(people.joinToString(), "Ragnar Lodbrok, Bjorn Ironside, Sweyn Forkbeard")

            people.sort(0, 2)

            // after sorting
            assertPrints(people.joinToString(), "Bjorn Ironside, Ragnar Lodbrok, Sweyn Forkbeard")
        }

        @Sample
        fun isSortedArrayOfComparable() {
            val sorted = arrayOf("apple", "banana", "cherry")
            assertPrints(sorted.isSorted(), "true")

            val unsorted = arrayOf("banana", "apple", "cherry")
            assertPrints(unsorted.isSorted(), "false")
        }

        @Sample
        fun isSortedByteArray() {
            val sorted = byteArrayOf(1, 2, 3, 4, 5)
            assertPrints(sorted.isSorted(), "true")

            val unsorted = byteArrayOf(1, 3, 2, 4, 5)
            assertPrints(unsorted.isSorted(), "false")
        }

        @Sample
        fun isSortedShortArray() {
            val sorted = shortArrayOf(1, 2, 3, 4, 5)
            assertPrints(sorted.isSorted(), "true")

            val unsorted = shortArrayOf(1, 3, 2, 4, 5)
            assertPrints(unsorted.isSorted(), "false")
        }

        @Sample
        fun isSortedIntArray() {
            val sorted = intArrayOf(1, 2, 3, 4, 5)
            assertPrints(sorted.isSorted(), "true")

            val unsorted = intArrayOf(1, 3, 2, 4, 5)
            assertPrints(unsorted.isSorted(), "false")
        }

        @Sample
        fun isSortedLongArray() {
            val sorted = longArrayOf(1L, 2L, 3L, 4L, 5L)
            assertPrints(sorted.isSorted(), "true")

            val unsorted = longArrayOf(1L, 3L, 2L, 4L, 5L)
            assertPrints(unsorted.isSorted(), "false")
        }

        @Sample
        fun isSortedFloatArray() {
            val sorted = floatArrayOf(1.0f, 2.5f, 3.14f)
            assertPrints(sorted.isSorted(), "true")

            val unsorted = floatArrayOf(2.5f, 1.0f, 3.14f)
            assertPrints(unsorted.isSorted(), "false")
        }

        @Sample
        fun isSortedDoubleArray() {
            val sorted = doubleArrayOf(1.0, 2.5, 3.14)
            assertPrints(sorted.isSorted(), "true")

            val unsorted = doubleArrayOf(2.5, 1.0, 3.14)
            assertPrints(unsorted.isSorted(), "false")
        }

        @Sample
        fun isSortedBooleanArray() {
            val sorted = booleanArrayOf(false, false, true)
            assertPrints(sorted.isSorted(), "true")

            val unsorted = booleanArrayOf(true, false, true)
            assertPrints(unsorted.isSorted(), "false")
        }

        @Sample
        fun isSortedCharArray() {
            val sorted = charArrayOf('a', 'b', 'c')
            assertPrints(sorted.isSorted(), "true")

            val unsorted = charArrayOf('b', 'a', 'c')
            assertPrints(unsorted.isSorted(), "false")
        }

        @Sample
        fun isSortedUIntArray() {
            val sorted = uintArrayOf(1u, 2u, 3u, 4u, 5u)
            assertPrints(sorted.isSorted(), "true")

            val unsorted = uintArrayOf(1u, 3u, 2u, 4u, 5u)
            assertPrints(unsorted.isSorted(), "false")
        }

        @Sample
        fun isSortedULongArray() {
            val sorted = ulongArrayOf(1u, 2u, 3u, 4u, 5u)
            assertPrints(sorted.isSorted(), "true")

            val unsorted = ulongArrayOf(1u, 3u, 2u, 4u, 5u)
            assertPrints(unsorted.isSorted(), "false")
        }

        @Sample
        fun isSortedUByteArray() {
            val sorted = ubyteArrayOf(1u, 2u, 3u, 4u, 5u)
            assertPrints(sorted.isSorted(), "true")

            val unsorted = ubyteArrayOf(1u, 3u, 2u, 4u, 5u)
            assertPrints(unsorted.isSorted(), "false")
        }

        @Sample
        fun isSortedUShortArray() {
            val sorted = ushortArrayOf(1u, 2u, 3u, 4u, 5u)
            assertPrints(sorted.isSorted(), "true")

            val unsorted = ushortArrayOf(1u, 3u, 2u, 4u, 5u)
            assertPrints(unsorted.isSorted(), "false")
        }

        @Sample
        fun isSortedByArrayOfComparable() {
            val strings = arrayOf("c", "bb", "aaa")
            assertPrints(strings.isSortedBy { it.length }, "true")
            assertPrints(strings.isSortedBy { it }, "false")
        }

        @Sample
        fun isSortedByByteArray() {
            val byteArray = byteArrayOf(1, -2, 3, -4, 5)
            assertPrints(byteArray.isSortedBy { it * it }, "true")  // 1, 4, 9, 16, 25
            assertPrints(byteArray.isSortedBy { abs(it.toInt()) }, "true")  // 1, 2, 3, 4, 5
            assertPrints(byteArray.isSortedBy { it }, "false")
        }

        @Sample
        fun isSortedByShortArray() {
            val shortArray = shortArrayOf(1, -2, 3, -4, 5)
            assertPrints(shortArray.isSortedBy { it * it }, "true")  // 1, 4, 9, 16, 25
            assertPrints(shortArray.isSortedBy { abs(it.toInt()) }, "true")  // 1, 2, 3, 4, 5
            assertPrints(shortArray.isSortedBy { it }, "false")
        }

        @Sample
        fun isSortedByIntArray() {
            val intArray = intArrayOf(1, -2, 3, -4, 5)
            assertPrints(intArray.isSortedBy { it * it }, "true")  // 1, 4, 9, 16, 25
            assertPrints(intArray.isSortedBy { abs(it) }, "true")  // 1, 2, 3, 4, 5
            assertPrints(intArray.isSortedBy { it }, "false")
        }

        @Sample
        fun isSortedByLongArray() {
            val longArray = longArrayOf(1L, -2L, 3L, -4L, 5L)
            assertPrints(longArray.isSortedBy { it * it }, "true")  // 1, 4, 9, 16, 25
            assertPrints(longArray.isSortedBy { abs(it) }, "true")  // 1, 2, 3, 4, 5
            assertPrints(longArray.isSortedBy { it }, "false")
        }

        @Sample
        fun isSortedByFloatArray() {
            val floatArray = floatArrayOf(-0.5f, 1.0f, -1.5f, 2.0f)
            assertPrints(floatArray.isSortedBy { it * it }, "true")  // 0.25, 1.0, 2.25, 4.0
            assertPrints(floatArray.isSortedBy { abs(it) }, "true")  // 0.5, 1.0, 1.5, 2.0
            assertPrints(floatArray.isSortedBy { it }, "false")
        }

        @Sample
        fun isSortedByDoubleArray() {
            val doubleArray = doubleArrayOf(-0.5, 1.0, -1.5, 2.0)
            assertPrints(doubleArray.isSortedBy { it * it }, "true")  // 0.25, 1.0, 2.25, 4.0
            assertPrints(doubleArray.isSortedBy { abs(it) }, "true")  // 0.5, 1.0, 1.5, 2.0
            assertPrints(doubleArray.isSortedBy { it }, "false")
        }

        @Sample
        fun isSortedByBooleanArray() {
            val booleanArray = booleanArrayOf(false, false, true)
            assertPrints(booleanArray.isSortedBy { it.compareTo(false) }, "true")  // 0, 0, 1
            assertPrints(booleanArray.isSortedBy { it }, "true")
            assertPrints(booleanArray.isSortedBy { !it }, "false")  // true, true, false
        }

        @Sample
        fun isSortedByCharArray() {
            val charArray = charArrayOf('A', 'b', 'C')
            assertPrints(charArray.isSortedBy { it.uppercaseChar() }, "true")  // A, B, C
            assertPrints(charArray.isSortedBy { it.lowercaseChar() }, "true")  // a, b, c
            assertPrints(charArray.isSortedBy { it }, "false")  // 'A'(65), 'b'(98), 'C'(67)
        }

        @Sample
        fun isSortedByUIntArray() {
            val uintArray = uintArrayOf(3u, 1u, 4u, 2u)
            assertPrints(uintArray.isSortedBy { it % 3u }, "true")  // 0, 1, 1, 2
            assertPrints(uintArray.isSortedBy { it }, "false")
        }

        @Sample
        fun isSortedByULongArray() {
            val ulongArray = ulongArrayOf(3u, 1u, 4u, 2u)
            assertPrints(ulongArray.isSortedBy { it % 3uL }, "true")  // 0, 1, 1, 2
            assertPrints(ulongArray.isSortedBy { it }, "false")
        }

        @Sample
        fun isSortedByUByteArray() {
            val ubyteArray = ubyteArrayOf(3u, 1u, 4u, 2u)
            assertPrints(ubyteArray.isSortedBy { it.toUInt() % 3u }, "true")  // 0, 1, 1, 2
            assertPrints(ubyteArray.isSortedBy { it }, "false")
        }

        @Sample
        fun isSortedByUShortArray() {
            val ushortArray = ushortArrayOf(3u, 1u, 4u, 2u)
            assertPrints(ushortArray.isSortedBy { it.toUInt() % 3u }, "true")  // 0, 1, 1, 2
            assertPrints(ushortArray.isSortedBy { it }, "false")
        }

        @Sample
        fun isSortedByDescendingArrayOfComparable() {
            val strings = arrayOf("aaa", "bb", "c")
            assertPrints(strings.isSortedByDescending { it.length }, "true")
            assertPrints(strings.isSortedByDescending { it }, "false")
        }

        @Sample
        fun isSortedByDescendingByteArray() {
            val byteArray = byteArrayOf(5, -4, 3, -2, 1)
            assertPrints(byteArray.isSortedByDescending { it * it }, "true")  // 25, 16, 9, 4, 1
            assertPrints(byteArray.isSortedByDescending { abs(it.toInt()) }, "true")  // 5, 4, 3, 2, 1
            assertPrints(byteArray.isSortedByDescending { it }, "false")
        }

        @Sample
        fun isSortedByDescendingShortArray() {
            val shortArray = shortArrayOf(5, -4, 3, -2, 1)
            assertPrints(shortArray.isSortedByDescending { it * it }, "true")  // 25, 16, 9, 4, 1
            assertPrints(shortArray.isSortedByDescending { abs(it.toInt()) }, "true")  // 5, 4, 3, 2, 1
            assertPrints(shortArray.isSortedByDescending { it }, "false")
        }

        @Sample
        fun isSortedByDescendingIntArray() {
            val intArray = intArrayOf(5, -4, 3, -2, 1)
            assertPrints(intArray.isSortedByDescending { it * it }, "true")  // 25, 16, 9, 4, 1
            assertPrints(intArray.isSortedByDescending { abs(it) }, "true")  // 5, 4, 3, 2, 1
            assertPrints(intArray.isSortedByDescending { it }, "false")
        }

        @Sample
        fun isSortedByDescendingLongArray() {
            val longArray = longArrayOf(5L, -4L, 3L, -2L, 1L)
            assertPrints(longArray.isSortedByDescending { it * it }, "true")  // 25, 16, 9, 4, 1
            assertPrints(longArray.isSortedByDescending { abs(it) }, "true")  // 5, 4, 3, 2, 1
            assertPrints(longArray.isSortedByDescending { it }, "false")
        }

        @Sample
        fun isSortedByDescendingFloatArray() {
            val floatArray = floatArrayOf(2.0f, -1.5f, 1.0f, -0.5f)
            assertPrints(floatArray.isSortedByDescending { it * it }, "true")  // 4.0, 2.25, 1.0, 0.25
            assertPrints(floatArray.isSortedByDescending { abs(it) }, "true")  // 2.0, 1.5, 1.0, 0.5
            assertPrints(floatArray.isSortedByDescending { it }, "false")
        }

        @Sample
        fun isSortedByDescendingDoubleArray() {
            val doubleArray = doubleArrayOf(2.0, -1.5, 1.0, -0.5)
            assertPrints(doubleArray.isSortedByDescending { it * it }, "true")  // 4.0, 2.25, 1.0, 0.25
            assertPrints(doubleArray.isSortedByDescending { abs(it) }, "true")  // 2.0, 1.5, 1.0, 0.5
            assertPrints(doubleArray.isSortedByDescending { it }, "false")
        }

        @Sample
        fun isSortedByDescendingBooleanArray() {
            val booleanArray = booleanArrayOf(true, true, false)
            assertPrints(booleanArray.isSortedByDescending { it.compareTo(false) }, "true")  // 1, 1, 0
            assertPrints(booleanArray.isSortedByDescending { it }, "true")
            assertPrints(booleanArray.isSortedByDescending { !it }, "false")  // false, false, true
        }

        @Sample
        fun isSortedByDescendingCharArray() {
            val charArray = charArrayOf('C', 'b', 'A')
            assertPrints(charArray.isSortedByDescending { it.uppercaseChar() }, "true")  // C, B, A
            assertPrints(charArray.isSortedByDescending { it.lowercaseChar() }, "true")  // c, b, a
            assertPrints(charArray.isSortedByDescending { it }, "false")  // 'C'(67), 'b'(98), 'A'(65)
        }

        @Sample
        fun isSortedByDescendingUIntArray() {
            val uintArray = uintArrayOf(2u, 4u, 1u, 3u)
            assertPrints(uintArray.isSortedByDescending { it % 3u }, "true")  // 2, 1, 1, 0
            assertPrints(uintArray.isSortedByDescending { it }, "false")
        }

        @Sample
        fun isSortedByDescendingULongArray() {
            val ulongArray = ulongArrayOf(2u, 4u, 1u, 3u)
            assertPrints(ulongArray.isSortedByDescending { it % 3uL }, "true")  // 2, 1, 1, 0
            assertPrints(ulongArray.isSortedByDescending { it }, "false")
        }

        @Sample
        fun isSortedByDescendingUByteArray() {
            val ubyteArray = ubyteArrayOf(2u, 4u, 1u, 3u)
            assertPrints(ubyteArray.isSortedByDescending { it.toUInt() % 3u }, "true")  // 2, 1, 1, 0
            assertPrints(ubyteArray.isSortedByDescending { it }, "false")
        }

        @Sample
        fun isSortedByDescendingUShortArray() {
            val ushortArray = ushortArrayOf(2u, 4u, 1u, 3u)
            assertPrints(ushortArray.isSortedByDescending { it.toUInt() % 3u }, "true")  // 2, 1, 1, 0
            assertPrints(ushortArray.isSortedByDescending { it }, "false")
        }

        @Sample
        fun isSortedDescendingArrayOfComparable() {
            val sorted = arrayOf("cherry", "banana", "apple")
            assertPrints(sorted.isSortedDescending(), "true")

            val unsorted = arrayOf("banana", "cherry", "apple")
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedDescendingByteArray() {
            val sorted = byteArrayOf(5, 4, 3, 2, 1)
            assertPrints(sorted.isSortedDescending(), "true")

            val unsorted = byteArrayOf(5, 3, 4, 2, 1)
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedDescendingShortArray() {
            val sorted = shortArrayOf(5, 4, 3, 2, 1)
            assertPrints(sorted.isSortedDescending(), "true")

            val unsorted = shortArrayOf(5, 3, 4, 2, 1)
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedDescendingIntArray() {
            val sorted = intArrayOf(5, 4, 3, 2, 1)
            assertPrints(sorted.isSortedDescending(), "true")

            val unsorted = intArrayOf(5, 3, 4, 2, 1)
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedDescendingLongArray() {
            val sorted = longArrayOf(5L, 4L, 3L, 2L, 1L)
            assertPrints(sorted.isSortedDescending(), "true")

            val unsorted = longArrayOf(5L, 3L, 4L, 2L, 1L)
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedDescendingFloatArray() {
            val sorted = floatArrayOf(3.14f, 2.5f, 1.0f)
            assertPrints(sorted.isSortedDescending(), "true")

            val unsorted = floatArrayOf(2.5f, 3.14f, 1.0f)
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedDescendingDoubleArray() {
            val sorted = doubleArrayOf(3.14, 2.5, 1.0)
            assertPrints(sorted.isSortedDescending(), "true")

            val unsorted = doubleArrayOf(2.5, 3.14, 1.0)
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedDescendingBooleanArray() {
            val sorted = booleanArrayOf(true, false, false)
            assertPrints(sorted.isSortedDescending(), "true")

            val unsorted = booleanArrayOf(false, true, false)
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedDescendingCharArray() {
            val sorted = charArrayOf('c', 'b', 'a')
            assertPrints(sorted.isSortedDescending(), "true")

            val unsorted = charArrayOf('b', 'c', 'a')
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedDescendingUIntArray() {
            val sorted = uintArrayOf(5u, 4u, 3u, 2u, 1u)
            assertPrints(sorted.isSortedDescending(), "true")

            val unsorted = uintArrayOf(5u, 3u, 4u, 2u, 1u)
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedDescendingULongArray() {
            val sorted = ulongArrayOf(5u, 4u, 3u, 2u, 1u)
            assertPrints(sorted.isSortedDescending(), "true")

            val unsorted = ulongArrayOf(5u, 3u, 4u, 2u, 1u)
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedDescendingUByteArray() {
            val sorted = ubyteArrayOf(5u, 4u, 3u, 2u, 1u)
            assertPrints(sorted.isSortedDescending(), "true")

            val unsorted = ubyteArrayOf(5u, 3u, 4u, 2u, 1u)
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedDescendingUShortArray() {
            val sorted = ushortArrayOf(5u, 4u, 3u, 2u, 1u)
            assertPrints(sorted.isSortedDescending(), "true")

            val unsorted = ushortArrayOf(5u, 3u, 4u, 2u, 1u)
            assertPrints(unsorted.isSortedDescending(), "false")
        }

        @Sample
        fun isSortedWithArrayOfComparable() {
            val byLength = compareBy<String> { it.length }

            val sorted = arrayOf("a", "bb", "ccc")
            assertPrints(sorted.isSortedWith(byLength), "true")

            val unsorted = arrayOf("bb", "a", "ccc")
            assertPrints(unsorted.isSortedWith(byLength), "false")
        }

        @Sample
        fun isSortedWithByteArray() {
            val byteArray = byteArrayOf(1, -2, 3, -4, 5)
            assertPrints(byteArray.isSortedWith(compareBy { it * it }), "true")  // 1, 4, 9, 16, 25
            assertPrints(byteArray.isSortedWith(compareBy { abs(it.toInt()) }), "true")  // 1, 2, 3, 4, 5
            assertPrints(byteArray.isSortedWith(compareBy { it }), "false")
        }

        @Sample
        fun isSortedWithShortArray() {
            val shortArray = shortArrayOf(1, -2, 3, -4, 5)
            assertPrints(shortArray.isSortedWith(compareBy { it * it }), "true")  // 1, 4, 9, 16, 25
            assertPrints(shortArray.isSortedWith(compareBy { abs(it.toInt()) }), "true")  // 1, 2, 3, 4, 5
            assertPrints(shortArray.isSortedWith(compareBy { it }), "false")
        }

        @Sample
        fun isSortedWithIntArray() {
            val intArray = intArrayOf(1, -2, 3, -4, 5)
            assertPrints(intArray.isSortedWith(compareBy { it * it }), "true")  // 1, 4, 9, 16, 25
            assertPrints(intArray.isSortedWith(compareBy { abs(it) }), "true")  // 1, 2, 3, 4, 5
            assertPrints(intArray.isSortedWith(compareBy { it }), "false")
        }

        @Sample
        fun isSortedWithLongArray() {
            val longArray = longArrayOf(1L, -2L, 3L, -4L, 5L)
            assertPrints(longArray.isSortedWith(compareBy { it * it }), "true")  // 1, 4, 9, 16, 25
            assertPrints(longArray.isSortedWith(compareBy { abs(it) }), "true")  // 1, 2, 3, 4, 5
            assertPrints(longArray.isSortedWith(compareBy { it }), "false")
        }

        @Sample
        fun isSortedWithFloatArray() {
            val floatArray = floatArrayOf(-0.5f, 1.0f, -1.5f, 2.0f)
            assertPrints(floatArray.isSortedWith(compareBy { it * it }), "true")  // 0.25, 1.0, 2.25, 4.0
            assertPrints(floatArray.isSortedWith(compareBy { abs(it) }), "true")  // 0.5, 1.0, 1.5, 2.0
            assertPrints(floatArray.isSortedWith(compareBy { it }), "false")
        }

        @Sample
        fun isSortedWithDoubleArray() {
            val doubleArray = doubleArrayOf(-0.5, 1.0, -1.5, 2.0)
            assertPrints(doubleArray.isSortedWith(compareBy { it * it }), "true")  // 0.25, 1.0, 2.25, 4.0
            assertPrints(doubleArray.isSortedWith(compareBy { abs(it) }), "true")  // 0.5, 1.0, 1.5, 2.0
            assertPrints(doubleArray.isSortedWith(compareBy { it }), "false")
        }

        @Sample
        fun isSortedWithBooleanArray() {
            val booleanArray = booleanArrayOf(false, false, true)
            assertPrints(booleanArray.isSortedWith(compareBy { it.toString() }), "true")
            assertPrints(booleanArray.isSortedWith(compareBy { it }), "true")
            assertPrints(booleanArray.isSortedWith(compareBy { !it }), "false")
        }

        @Sample
        fun isSortedWithCharArray() {
            val charArray = charArrayOf('A', 'b', 'C')  // mixed case
            assertPrints(charArray.isSortedWith(compareBy { it.uppercaseChar() }), "true")  // A, B, C
            assertPrints(charArray.isSortedWith(compareBy { it.lowercaseChar() }), "true")  // a, b, c
            assertPrints(charArray.isSortedWith(compareBy { it }), "false")  // 'A'(65), 'b'(98), 'C'(67)
        }

        @Sample
        fun isSortedWithUIntArray() {
            val uintArray = uintArrayOf(3u, 1u, 4u, 2u)
            assertPrints(uintArray.isSortedWith(compareBy { it % 3u }), "true")  // 0, 1, 1, 2
            assertPrints(uintArray.isSortedWith(compareBy { it }), "false")
        }

        @Sample
        fun isSortedWithULongArray() {
            val ulongArray = ulongArrayOf(3u, 1u, 4u, 2u)
            assertPrints(ulongArray.isSortedWith(compareBy { it % 3uL }), "true")  // 0, 1, 1, 2
            assertPrints(ulongArray.isSortedWith(compareBy { it }), "false")
        }

        @Sample
        fun isSortedWithUByteArray() {
            val ubyteArray = ubyteArrayOf(3u, 1u, 4u, 2u)
            assertPrints(ubyteArray.isSortedWith(compareBy { it.toUInt() % 3u }), "true")  // 0, 1, 1, 2
            assertPrints(ubyteArray.isSortedWith(compareBy { it }), "false")
        }

        @Sample
        fun isSortedWithUShortArray() {
            val ushortArray = ushortArrayOf(3u, 1u, 4u, 2u)
            assertPrints(ushortArray.isSortedWith(compareBy { it.toUInt() % 3u }), "true")  // 0, 1, 1, 2
            assertPrints(ushortArray.isSortedWith(compareBy { it }), "false")
        }
    }

    class Constructors {
        @Sample
        fun arrayOfSample() {
            val emptyArray = arrayOf<Any>()
            assertPrints(emptyArray.contentToString(), "[]")

            val strings = arrayOf("Hello", "world")
            assertPrints(strings.contentToString(), "[Hello, world]")

            val numbers: Array<Number> = arrayOf(3.14, 42L, 0.123f)
            assertPrints(numbers.contentToString(), "[3.14, 42, 0.123]")
        }

        @Sample
        fun doubleArrayOfSample() {
            val emptyDoubleArray = doubleArrayOf()
            assertPrints(emptyDoubleArray.contentToString(), "[]")

            val doubleArray = doubleArrayOf(1.0, 2.5, 3.14)
            assertPrints(doubleArray.contentToString(), "[1.0, 2.5, 3.14]")
        }

        @Sample
        fun floatArrayOfSample() {
            val emptyFloatArray = floatArrayOf()
            assertPrints(emptyFloatArray.contentToString(), "[]")

            val floatArray = floatArrayOf(1.0f, 2.5f, 3.14f)
            assertPrints(floatArray.contentToString(), "[1.0, 2.5, 3.14]")
        }

        @Sample
        fun longArrayOfSample() {
            val emptyLongArray = longArrayOf()
            assertPrints(emptyLongArray.contentToString(), "[]")

            val longArray = longArrayOf(1L, 2L, 3L)
            assertPrints(longArray.contentToString(), "[1, 2, 3]")
        }

        @Sample
        fun intArrayOfSample() {
            val emptyIntArray = intArrayOf()
            assertPrints(emptyIntArray.contentToString(), "[]")

            val intArray = intArrayOf(1, 2, 3)
            assertPrints(intArray.contentToString(), "[1, 2, 3]")
        }

        @Sample
        fun charArrayOfSample() {
            val emptyCharArray = charArrayOf()
            assertPrints(emptyCharArray.contentToString(), "[]")

            val charArray = charArrayOf('a', 'b', 'c')
            assertPrints(charArray.contentToString(), "[a, b, c]")
        }

        @Sample
        fun shortArrayOfSample() {
            val emptyShortArray = shortArrayOf()
            assertPrints(emptyShortArray.contentToString(), "[]")

            val shortArray = shortArrayOf(1, 2, 3)
            assertPrints(shortArray.contentToString(), "[1, 2, 3]")
        }

        @Sample
        fun byteArrayOfSample() {
            val emptyByteArray = byteArrayOf()
            assertPrints(emptyByteArray.contentToString(), "[]")

            val byteArray = byteArrayOf(1, 2, 3)
            assertPrints(byteArray.contentToString(), "[1, 2, 3]")
        }

        @Sample
        fun booleanArrayOfSample() {
            val emptyBooleanArray = booleanArrayOf()
            assertPrints(emptyBooleanArray.contentToString(), "[]")

            val booleanArray = booleanArrayOf(true, false, true)
            assertPrints(booleanArray.contentToString(), "[true, false, true]")
        }
    }
}
