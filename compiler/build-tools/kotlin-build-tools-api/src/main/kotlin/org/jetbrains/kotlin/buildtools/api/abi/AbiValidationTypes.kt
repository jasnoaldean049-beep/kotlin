/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.buildtools.api.abi


/**
 * Set of filtering rules that restrict ABI declarations included in a dump.
 *
 * The rules combine inclusion and exclusion of declarations.
 * Each filter can be written as a filter for the class name (see [includedNamed] or [excludedNamed]), or an annotation filter (see [includedAnnotatedWith] or [excludedAnnotatedWith]).
 *
 * In order for a declaration (class, field, property, or function) to get into the dump, it must pass the inclusion **and** exclusion filters.
 *
 * A declaration passes the exclusion filters if it does not match any class names (see [excludedNamed]) or annotation  (see [excludedAnnotatedWith]) filter rules.
 *
 * A declaration passes the inclusion filters if there are no inclusion rules, or it matches any inclusion rule, or at least one of its members (actual for class declaration) matches any inclusion rule.
 *
 * @since 2.4.0
 */
public class AbiFilters(

    /**
     * Include a class, file-level property, or file-level function in a dump by its name.
     * Declarations that do not match the specified names, that do not have an annotation from [includedAnnotatedWith]
     * and do not have members marked with an annotation from [includedAnnotatedWith] are excluded from the dump.
     *
     * The name filter compares the qualified class name with the value in the filter:
     *
     * For Kotlin declarations, fully qualified names are used.
     * It is important to keep in mind that dots are used everywhere as separators, even in the case of a nested class.
     * E.g. for qualified name `foo.bar.Container.Value`, here `Value` is a class nested in `Container`.
     *
     * For classes from Java sources, canonical names are used.
     * The main motivation is a similar approach to writing the class name - dots are used everywhere as delimiters.
     *
     * Name templates are allowed, with support for wildcards such as `**`, `*`, and `?`:
     * - `**` - zero or any number of characters
     * - `*` - zero or any number of characters excluding dot. Using to specify simple class name.
     * - `?` - any single character.
     */
    public val includedNamed: Set<String> = emptySet(),

    /**
     * Excludes a class, file-level property, or file-level function from a dump by its name.
     *
     * The name filter compares the qualified class name with the value in the filter:
     *
     * For Kotlin declarations, fully qualified names are used.
     * It is important to keep in mind that dots are used everywhere as separators, even in the case of a nested class.
     * E.g. for qualified name `foo.bar.Container.Value`, here `Value` is a class nested in `Container`.
     *
     * For classes from Java sources, canonical names are used.
     * The main motivation is a similar approach to writing the class name - dots are used everywhere as delimiters.
     *
     * Name templates are allowed, with support for wildcards such as `**`, `*`, and `?`:
     * - `**` - zero or any number of characters
     * - `*` - zero or any number of characters excluding dot. Using to specify simple class name.
     * - `?` - any single character.
     */
    public val excludedNamed: Set<String> = emptySet(),
    /**
     * Includes a declaration by annotations placed on it.
     *
     * Any declaration that is not marked with one of the these annotations and does not match the [includedClasses] is excluded from the dump.
     *
     * The declaration can be a class, a class member (function or property), a top-level function or a top-level property.
     *
     * Name templates are allowed, with support for wildcards such as `**`, `*`, and `?`:
     * - `**` - zero or any number of characters
     * - `*` - zero or any number of characters excluding dot. Using to specify simple class name.
     * - `?` - any single character.
     *
     * The annotation should not have [Retention] equal to [AnnotationRetention.SOURCE], otherwise, filtering by it will not work.
     */
    public val includedAnnotatedWith: Set<String> = emptySet(),

    /**
     * Excludes a declaration by annotations placed on it.
     *
     * It means that a class, a class member (function or property), a top-level function or a top-level property
     * marked by a specific annotation will be excluded from the dump.
     *
     * Name templates are allowed, with support for wildcards such as `**`, `*`, and `?`:
     * - `**` - zero or any number of characters
     * - `*` - zero or any number of characters excluding dot. Using to specify simple class name.
     * - `?` - any single character.
     *
     * The annotation should not have [Retention] equal to [AnnotationRetention.SOURCE], otherwise, filtering by it will not work.
     */
    public val excludedAnnotatedWith: Set<String> = emptySet(),
) {
    public companion object {
        public val EMPTY: AbiFilters = AbiFilters(emptySet(), emptySet(), emptySet(), emptySet())
    }

    public val isEmpty: Boolean =
        includedNamed.isEmpty() && excludedNamed.isEmpty() && includedAnnotatedWith.isEmpty() && excludedAnnotatedWith.isEmpty()
}

/**
 * Target name consisting of two parts: a [customizedName] that could be configured by a user, and a [canonicalName]
 * that names a target platform and could not be configured by a user.
 *
 * When serialized, the target is represented as a tuple `<canonicalName>.<customizedName>`, like `iosArm64.ios`.
 * If both names are the same (they are by default, unless a user decides to use a custom name), the serialized
 * from is shortened to a single term. For example, `macosArm64.macosArm64` and `macosArm64` are a long and a short
 * serialized forms of the same target.
 *
 * @since 2.4.0
 */
public class KlibTargetId(
    /**
     * An actual name of a target that remains unaffected by any custom settings.
     */
    public val canonicalName: String,
    /**
     * A name of a target that could be configured by a user.
     * Usually, it's the same name as [canonicalName].
     */
    public val customizedName: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KlibTargetId) return false

        if (canonicalName != other.canonicalName) return false
        if (customizedName != other.customizedName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = canonicalName.hashCode()
        result = 31 * result + customizedName.hashCode()
        return result
    }
}
