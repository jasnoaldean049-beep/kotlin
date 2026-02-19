/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.konan.ConfigChecks
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.ir.hasNonConstInitializer
import org.jetbrains.kotlin.backend.konan.DECLARATION_ORIGIN_ENTRY_POINT
import org.jetbrains.kotlin.backend.konan.KonanFqNames
import org.jetbrains.kotlin.backend.konan.ir.getSuperClassNotAny
import org.jetbrains.kotlin.backend.konan.llvm.*
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.irAttribute
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.getOrSetIfNull
import org.jetbrains.kotlin.utils.addToStdlib.runIf

internal val DECLARATION_ORIGIN_STATIC_GLOBAL_INITIALIZER = IrDeclarationOriginImpl("STATIC_GLOBAL_INITIALIZER")
internal val DECLARATION_ORIGIN_STATIC_THREAD_LOCAL_INITIALIZER = IrDeclarationOriginImpl("STATIC_THREAD_LOCAL_INITIALIZER")
internal val DECLARATION_ORIGIN_STATIC_STANDALONE_THREAD_LOCAL_INITIALIZER = IrDeclarationOriginImpl("STATIC_STANDALONE_THREAD_LOCAL_INITIALIZER")

internal val IrFunction.isStaticInitializer: Boolean
    get() = origin == DECLARATION_ORIGIN_STATIC_GLOBAL_INITIALIZER
            || origin == DECLARATION_ORIGIN_STATIC_THREAD_LOCAL_INITIALIZER
            || origin == DECLARATION_ORIGIN_STATIC_STANDALONE_THREAD_LOCAL_INITIALIZER

internal fun IrBuilderWithScope.irCallFileInitializer(initializer: IrFunctionSymbol) =
        irCall(initializer)

internal fun ConfigChecks.shouldBeInitializedEagerly(irField: IrField): Boolean {
    if (irField.parent is IrFile || irField.correspondingPropertySymbol?.owner?.parent is IrFile) {
        if (!useLazyFileInitializers()) return true
    }
    val annotations = irField.correspondingPropertySymbol?.owner?.annotations ?: irField.annotations
    return annotations.hasAnnotation(KonanFqNames.eagerInitialization)
}

val STATEMENT_ORIGIN_FIELD_GLOBAL_INITIALIZER by IrStatementOriginImpl

var IrClass.clinitTriggerFunction: IrSimpleFunctionSymbol? by irAttribute(copyByDefault = true)

// TODO: ExplicitlyExported for IR proto are not longer needed.
internal class StaticInitializersLowering(val context: Context) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        irFile.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }
            override fun visitFile(declaration: IrFile) {
                processDeclarationContainter(declaration)
                declaration.acceptChildrenVoid(this)
            }
            override fun visitClass(declaration: IrClass) {
                declaration.addChild(declaration.getClinitTriggerFunction().owner.apply {
                    body = context.irFactory.createBlockBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET)
                })
                processDeclarationContainter(declaration)
                declaration.acceptChildrenVoid(this)
            }
        })
    }

    fun IrClass.getClinitTriggerFunction(): IrSimpleFunctionSymbol {
        return ::clinitTriggerFunction.getOrSetIfNull {
            context.irFactory.buildFun {
                name = Name.identifier("\$clinit_trigger")
                visibility = DescriptorVisibilities.PUBLIC
            }.apply {
                parent = this@getClinitTriggerFunction
                returnType = context.irBuiltIns.unitType
            }.symbol
        }
    }

    fun processDeclarationContainter(container: IrDeclarationContainer) {
        val threadLocalInitializers = mutableListOf<IrExpression>()
        val globalInitializers = mutableListOf<IrExpression>()

        val builder = context.irBuiltIns.createIrBuilder((container as IrSymbolOwner).symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET)

        if (container is IrClass && !container.isInterface) {
            // Implemented as defined in
            // https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-5.html#jvms-5.5
            // Next, if C is a class rather than an interface, then let SC be its superclass and let SI1, ..., SIn be all superinterfaces of C (whether direct or indirect) that declare at least one non-abstract, non-static method.
            // The order of superinterfaces is given by a recursive enumeration over the superinterface hierarchy of each interface directly implemented by C. F
            // or each interface I directly implemented by C (in the order of the interfaces array of C), the enumeration recurs on I's superinterfaces (in the order of the interfaces array of I) before returning I.
            //
            // For each S in the list [ SC, SI1, ..., SIn ], if S has not yet been initialized, then recursively perform this entire procedure for S. If necessary, verify and prepare S first.

            val superClassesToInitialize = buildList {
                container.getSuperClassNotAny()?.let { add(it) }
                fun IrDeclaration.triggersInterfaceInitialization(): Boolean {
                    if (this !is IrOverridableDeclaration<*>) return false
                    if (isFakeOverride) return false
                    if (modality == Modality.ABSTRACT) return false
                    if (this is IrSimpleFunction && !isStatic) return true
                    if (this is IrProperty && (getter?.isStatic ?: backingField?.isStatic) != true) return true
                    return false
                }
                val seen = mutableSetOf<IrClassSymbol>()
                fun collectSuperInterfacesInOrder(irClass: IrClass) {
                    if (seen.add(irClass.symbol)) {
                        for (superType in irClass.superTypes) {
                            val superClass = superType.classOrFail.owner
                            if (superClass.isInterface) {
                                collectSuperInterfacesInOrder(superClass)
                            }
                        }
                        if (irClass.isInterface && irClass.declarations.any { it.triggersInterfaceInitialization() }) {
                            add(irClass)
                        }
                    }
                }
                collectSuperInterfacesInOrder(container)
            }
            for (superClass in superClassesToInitialize) {
                val trigger = superClass.getClinitTriggerFunction()
                globalInitializers.add(builder.irCall(trigger))
                threadLocalInitializers.add(builder.irCall(trigger))
            }
        }

        for (declaration in container.declarations) {
            val irField = (declaration as? IrField) ?: (declaration as? IrProperty)?.backingField
            if (irField == null || !irField.isStatic || context.shouldBeInitializedEagerly(irField)) continue
            if (!irField.hasNonConstInitializer && !irField.needsGCRegistration) continue
            val isThreadLocal = irField.storageKind == FieldStorageKind.THREAD_LOCAL
            val initializers = if (isThreadLocal) threadLocalInitializers else globalInitializers
            initializers.add(builder.irSetField(
                    receiver = null,
                    field = irField,
                    // it can be null, if we are here needsGCRegistration branch and need to set something
                    value = irField.initializer?.expression ?: builder.irNull(),
                    origin = STATEMENT_ORIGIN_FIELD_GLOBAL_INITIALIZER.takeUnless { isThreadLocal }
            ))
            irField.initializer = null
        }
        val requireGlobalInitializer = globalInitializers.isNotEmpty()
        val requireThreadLocalInitializer = threadLocalInitializers.isNotEmpty()
        // TODO: think about pure initializers.
        if (!requireGlobalInitializer && !requireThreadLocalInitializer) {
            return
        }

        val globalInitFunction = runIf(requireGlobalInitializer) {
            buildInitFunction(
                    container = container,
                    name = "\$init_global",
                    origin = DECLARATION_ORIGIN_STATIC_GLOBAL_INITIALIZER,
                    initializers = globalInitializers
            )
        }
        val threadLocalInitFunction = runIf(requireThreadLocalInitializer) {
            buildInitFunction(
                    container = container,
                    name = "\$init_thread_local",
                    origin = when {
                        requireGlobalInitializer -> DECLARATION_ORIGIN_STATIC_THREAD_LOCAL_INITIALIZER
                        else -> DECLARATION_ORIGIN_STATIC_STANDALONE_THREAD_LOCAL_INITIALIZER
                    },
                    initializers = threadLocalInitializers
            )
        }
        
        fun IrFunction.addInitializersCall() {
            val body = body ?: return
            val statements = (body as IrBlockBody).statements
            context.createIrBuilder(symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).run {
                // The order of calling initializers: first global, then thread-local.
                // It is ok for a thread local top level property to reference a global, but not vice versa.
                threadLocalInitFunction?.let { statements.add(0, irCallFileInitializer(it.symbol)) }
                globalInitFunction?.let { statements.add(0, irCallFileInitializer(it.symbol)) }
            }
        }

        for (function in container.simpleFunctions()) {
            if (function.dispatchReceiverParameter != null) continue // already initialized when instance was created
            if (function.origin == DECLARATION_ORIGIN_ENTRY_POINT) continue // is not really in any class
            if (function.isStaticInitializer) continue // don't initialize recursively
            function.addInitializersCall()
        }
        if (container is IrClass) {
            for (constructor in container.constructors) {
                constructor.addInitializersCall()
            }
        }
    }

    private fun buildInitFunction(
            container: IrDeclarationContainer,
            name: String,
            origin: IrDeclarationOrigin,
            initializers: List<IrExpression>
    ) = context.irFactory.buildFun {
        startOffset = SYNTHETIC_OFFSET
        endOffset = SYNTHETIC_OFFSET
        this.origin = origin
        this.name = Name.identifier(name)
        visibility = DescriptorVisibilities.PRIVATE
        returnType = context.irBuiltIns.unitType
    }.apply {
        parent = container
        body = context.irFactory.createBlockBody(startOffset, endOffset, initializers).setDeclarationsParent(this)
        container.declarations.add(0, this)
    }

}
