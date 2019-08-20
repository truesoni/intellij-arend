package org.arend.highlight

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx
import com.intellij.codeInsight.daemon.impl.HighlightInfoProcessor
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.arend.editor.ArendOptions
import org.arend.psi.ArendDefinition
import org.arend.psi.ArendFile
import org.arend.psi.ext.impl.ArendGroup
import org.arend.quickfix.AbstractEWCCAnnotator
import org.arend.term.concrete.Concrete
import org.arend.term.group.Group
import org.arend.typechecking.DefinitionBlacklistService
import org.arend.typechecking.SilentTypechecking
import org.arend.typechecking.TypeCheckingService
import org.arend.typechecking.error.ErrorService
import org.arend.typechecking.error.NotificationErrorReporter
import org.arend.typechecking.typecheckable.provider.EmptyConcreteProvider
import org.arend.typechecking.visitor.DesugarVisitor
import org.arend.typechecking.visitor.DumbTypechecker
import org.arend.util.FullName

class SilentTypecheckerPass(file: ArendFile, group: ArendGroup, editor: Editor, textRange: TextRange, highlightInfoProcessor: HighlightInfoProcessor, private val ignoredPassIds: List<Int>)
    : BaseGroupPass(file, group, editor, "Arend silent typechecker annotator", textRange, highlightInfoProcessor) {

    private val typeCheckingService = TypeCheckingService.getInstance(myProject)
    private val definitionBlackListService = ServiceManager.getService(DefinitionBlacklistService::class.java)
    private val definitionsToTypecheck = ArrayList<ArendDefinition>()
    private val errorService = ErrorService.getInstance(myProject)

    override fun visitDefinition(definition: Concrete.Definition, progress: ProgressIndicator) {
        DesugarVisitor.desugar(definition, file.concreteProvider, errorService)

        progress.checkCanceled()
        definition.accept(object : DumbTypechecker(errorService) {
            override fun visitFunction(def: Concrete.FunctionDefinition, params: Void?): Void? {
                super.visitFunction(def, params)
                AbstractEWCCAnnotator.makeAnnotator(def.data.data as? PsiElement)?.doAnnotate(holder)
                return null
            }

            override fun visitClassFieldImpl(classFieldImpl: Concrete.ClassFieldImpl, params: Void?) {
                AbstractEWCCAnnotator.makeAnnotator(classFieldImpl.data as? PsiElement)?.doAnnotate(holder)
                super.visitClassFieldImpl(classFieldImpl, params)
            }

            override fun visitClassExt(expr: Concrete.ClassExtExpression, params: Void?): Void? {
                AbstractEWCCAnnotator.makeAnnotator(expr.data as? PsiElement)?.doAnnotate(holder)
                super.visitClassExt(expr, params)
                return null
            }

            override fun visitNew(expr: Concrete.NewExpression, params: Void?): Void? {
                if (expr.expression !is Concrete.ClassExtExpression) {
                    AbstractEWCCAnnotator.makeAnnotator(expr.data as? PsiElement)?.doAnnotate(holder)
                }
                super.visitNew(expr, params)
                return null
            }
        }, null)
    }

    private fun typecheckDefinition(typechecking: SilentTypechecking, definition: ArendDefinition, progress: ProgressIndicator): Concrete.Definition? {
        val result = (typechecking.concreteProvider.getConcrete(definition) as? Concrete.Definition)?.let {
            val ok = definitionBlackListService.runTimed(definition, progress) {
                typechecking.typecheckDefinitions(listOf(it)) {
                    progress.isCanceled
                }
            }

            if (!ok) {
                NotificationErrorReporter(myProject).warn("Typechecking of ${FullName(it.data)} was interrupted after ${ArendOptions.instance.typecheckingTimeLimit} second(s)")
                if (definitionsToTypecheck.last() != definition) {
                    DaemonCodeAnalyzer.getInstance(myProject).restart(file)
                }
            }

            it
        }

        advanceProgress(1)
        return result
    }

    override fun collectInfo(progress: ProgressIndicator) {
        when (ArendOptions.instance.typecheckingMode) {
            ArendOptions.TypecheckingMode.SMART -> if (definitionsToTypecheck.isNotEmpty()) {
                val typechecking = SilentTypechecking.create(myProject, this)
                if (definitionsToTypecheck.size == 1) {
                    val onlyDef = typecheckDefinition(typechecking, definitionsToTypecheck[0], progress)

                    // If the only definition is the last modified definition and it was successfully typechecked,
                    // we will collect definitions again and typecheck them.
                    val typechecked = onlyDef?.let { typeCheckingService.typecheckerState.getTypechecked(it.data) }
                    if (onlyDef != null && (typechecked?.status()?.withoutErrors() == true) && definitionsToTypecheck[0] == file.lastModifiedDefinition) {
                        file.lastModifiedDefinition = null
                        setProgressLimit(super.numberOfDefinitions(group).toLong())
                    }
                    definitionsToTypecheck.clear()
                }

                for (definition in definitionsToTypecheck) {
                    typecheckDefinition(typechecking, definition, progress)
                }

                val daemon = DaemonCodeAnalyzerEx.getInstanceEx(myProject)
                daemon.restart(file) // To update line markers
                if (!progress.isCanceled) {
                    for (passId in ignoredPassIds) {
                        daemon.fileStatusMap.markFileUpToDate(document, passId)
                    }
                }
            }
            ArendOptions.TypecheckingMode.DUMB ->
                for (definition in definitionsToTypecheck) {
                    visitDefinition(definition, progress)
                }
            ArendOptions.TypecheckingMode.OFF -> {}
        }

        file.concreteProvider = EmptyConcreteProvider.INSTANCE
    }

    override fun countDefinition(def: ArendDefinition) =
        if (!definitionBlackListService.isBlacklisted(def) && typeCheckingService.getTypechecked(def) == null) {
            definitionsToTypecheck.add(def)
            true
        } else false

    override fun numberOfDefinitions(group: Group): Int =
        when (ArendOptions.instance.typecheckingMode) {
            ArendOptions.TypecheckingMode.OFF -> 0
            ArendOptions.TypecheckingMode.SMART -> {
                val def = file.lastModifiedDefinition
                if (def != null) {
                    if (countDefinition(def)) 1 else 0
                } else {
                    super.numberOfDefinitions(group)
                }
            }
            ArendOptions.TypecheckingMode.DUMB -> super.numberOfDefinitions(group)
        }
}