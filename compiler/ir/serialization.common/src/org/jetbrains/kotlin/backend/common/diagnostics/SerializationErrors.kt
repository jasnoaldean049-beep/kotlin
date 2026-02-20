/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.diagnostics

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.backend.common.diagnostics.SerializationDiagnosticRenderers.CONFLICTING_KLIB_SIGNATURES_DATA
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.rendering.Renderer
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.renderer.ConeIdFullRenderer
import org.jetbrains.kotlin.fir.renderer.ConeIdShortRenderer
import org.jetbrains.kotlin.fir.renderer.ConeTypeRendererForReadability
import org.jetbrains.kotlin.fir.renderer.FirCallNoArgumentsRenderer
import org.jetbrains.kotlin.fir.renderer.FirCallableSignatureRendererForReadability
import org.jetbrains.kotlin.fir.renderer.FirDeclarationRenderer
import org.jetbrains.kotlin.fir.renderer.FirNoClassMemberRenderer
import org.jetbrains.kotlin.fir.renderer.FirPartialModifierRenderer
import org.jetbrains.kotlin.fir.renderer.FirRenderer
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrMetadataSourceOwner
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.DeclarationSymbolOwner
import org.jetbrains.kotlin.ir.descriptors.IrBasedDeclarationDescriptor
import org.jetbrains.kotlin.ir.descriptors.toIrBasedDescriptor
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.renderer.render
import org.jetbrains.kotlin.resolve.MemberComparator

internal object SerializationErrors : KtDiagnosticsContainer() {
    val CONFLICTING_KLIB_SIGNATURES_ERROR by error1<PsiElement, ConflictingKlibSignaturesData>()

    override fun getRendererFactory(): BaseDiagnosticRendererFactory {
        return KtDefaultSerializationErrorMessages
    }
}

internal object KtDefaultSerializationErrorMessages : BaseDiagnosticRendererFactory() {
    override val MAP by KtDiagnosticFactoryToRendererMap("KT") { map ->
        map.put(
            SerializationErrors.CONFLICTING_KLIB_SIGNATURES_ERROR,
            "Platform declaration clash: {0}",
            CONFLICTING_KLIB_SIGNATURES_DATA,
        )
    }
}

internal object SerializationDiagnosticRenderers {
    val CONFLICTING_KLIB_SIGNATURES_DATA =
        CommonRenderers.renderConflictingSignatureData<DeclarationDescriptor, ConflictingKlibSignaturesData>(
            signatureKind = "IR",
            sortUsing = MemberComparator.INSTANCE,
            declarationRenderer = Renderer { descriptor ->
                getFirSymbol(descriptor)?.let { firSymbolWithLocationRenderer().render(it) }
                    ?: DescriptorRenderer.WITHOUT_MODIFIERS.render(descriptor)
            },
            renderSignature = { append(it.signature.render()) },
            declarations = { it.declarations.map(IrDeclaration::toIrBasedDescriptor) },
            declarationKind = { data ->
                when {
                    data.declarations.all { it is IrSimpleFunction } -> "functions"
                    data.declarations.all { it is IrProperty } -> "properties"
                    data.declarations.all { it is IrField } -> "fields"
                    else -> "declarations"
                }
            },
        )


    private fun firSymbolWithLocationRenderer() = Renderer { symbol: FirBasedSymbol<*> ->
        when (symbol) {
            is FirClassLikeSymbol, is FirCallableSymbol -> {
                buildString {
                    append(renderFirSymbol(symbol))
                    val packageName = if (symbol.packageFqName().isRoot) "root package" else symbol.packageFqName().render()
                    val moduleName = symbol.moduleData.name
                    append(" defined in $packageName in module $moduleName")
                }
            }
            is FirTypeParameterSymbol -> symbol.name.asString()
            else -> "???"
        }
    }

    @OptIn(SymbolInternals::class)
    private fun renderFirSymbol(symbol: FirBasedSymbol<*>) = FirRenderer(
        typeRenderer = ConeTypeRendererForReadability { ConeIdFullRenderer() },
        idRenderer = ConeIdShortRenderer(),
        classMemberRenderer = FirNoClassMemberRenderer(),
        bodyRenderer = null,
        propertyAccessorRenderer = null,
        callArgumentsRenderer = FirCallNoArgumentsRenderer(),
        modifierRenderer = FirPartialModifierRenderer(),
        callableSignatureRenderer = FirCallableSignatureRendererForReadability(),
        declarationRenderer = FirDeclarationRenderer("local ", renderVerboseAccessors = true),
        contractRenderer = null,
        annotationRenderer = null,
        lineBreakAfterContextParameters = false,
        renderFieldAnnotationSeparately = false,
    ).renderElementAsString(symbol.fir, trim = true)

    private fun getFirSymbol(descriptor: DeclarationDescriptor): FirBasedSymbol<*>? {
        val irDeclaration = (descriptor as? IrBasedDeclarationDescriptor<*>)?.owner
        val metadata = (irDeclaration as? IrMetadataSourceOwner)?.metadata
        return (metadata as? DeclarationSymbolOwner)?.symbol as? FirBasedSymbol<*>?
    }
}
