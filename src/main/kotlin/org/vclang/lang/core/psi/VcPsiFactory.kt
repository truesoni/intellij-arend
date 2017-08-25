package org.vclang.lang.core.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import org.vclang.lang.VcFileType
import org.vclang.lang.core.psi.ext.VcCompositeElement
import org.vclang.lang.refactoring.VcNamesValidator

class VcPsiFactory(private val project: Project) {

    fun createIdentifier(name: String): VcIdentifier =
            createFunction(name).identifier ?: error("Failed to create identifier: `$name`")

    fun createPrefixName(name: String): VcPrefixName {
        val needsPrefix = !VcNamesValidator().isPrefixName(name)
        return createLiteral(if (needsPrefix) "`$name" else name).prefixName
                ?: error("Failed to create prefix name: `$name`")
    }

    fun createInfixName(name: String): VcInfixName {
        val needsPrefix = !VcNamesValidator().isInfixName(name)
        return createExpression<VcBinOpExpr>("dummy ${if (needsPrefix) "`$name" else name} dummy")
                .binOpLeftList
                .firstOrNull()
                ?.infixName
                ?: error("Failed to create infix name: `$name`")
    }

    fun createPostfixName(name: String): VcPostfixName {
        return createExpression<VcBinOpExpr>("dummy $name`")
                .newExpr
                .postfixNameList
                .firstOrNull()
                ?: error("Failed to create postfix name: `$name`")
    }

    private fun createFunction(
            name: String,
            teles: List<String> = emptyList(),
            expr: String? = null
    ): VcDefFunction {
        val code = buildString {
            append("\\function ")
            append(name)
            append(teles.joinToString(" ", " "))
            expr?.let { append(" : $expr") }
        }.trimEnd()
        return createFromText<VcStatements>(code)?.childOfType()
                ?: error("Failed to create function: `$code`")
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : VcExpr> createExpression(expr: String): T =
            createFunction("dummy", emptyList(), expr).expr as? T
                    ?: error("Failed to create expression: `$expr`")

    private fun createLiteral(literal: String): VcLiteral =
            createFunction("dummy", listOf(literal)).teleList.firstOrNull()?.childOfType()
                    ?: error("Failed to create literal: `$literal`")

    private inline fun <reified T : VcCompositeElement> createFromText(code: String): T? =
            PsiFileFactory.getInstance(project)
                    .createFileFromText("DUMMY.rs", VcFileType, code)
                    .childOfType()
}