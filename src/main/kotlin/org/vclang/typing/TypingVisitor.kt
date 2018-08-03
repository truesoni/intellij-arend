package org.vclang.typing

import com.jetbrains.jetpad.vclang.naming.reference.Referable
import com.jetbrains.jetpad.vclang.term.abs.Abstract
import com.jetbrains.jetpad.vclang.term.abs.AbstractExpressionVisitor
import java.math.BigInteger


class TypingVisitor : AbstractExpressionVisitor<Void,Any> {
    override fun visitErrors() = false

    override fun visitApp(data: Any?, expr: Abstract.Expression, arguments: Collection<Abstract.Argument>, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitReference(data: Any?, referent: Referable, level1: Abstract.LevelExpression?, level2: Abstract.LevelExpression?, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitReference(data: Any?, referent: Referable, lp: Int, lh: Int, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitLam(data: Any?, parameters: Collection<Abstract.Parameter>, body: Abstract.Expression?, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitPi(data: Any?, parameters: Collection<Abstract.Parameter>, codomain: Abstract.Expression?, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitUniverse(data: Any?, pLevelNum: Int?, hLevelNum: Int?, pLevel: Abstract.LevelExpression?, hLevel: Abstract.LevelExpression?, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitInferHole(data: Any?, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitGoal(data: Any?, name: String?, expression: Abstract.Expression?, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitTuple(data: Any?, fields: Collection<Abstract.Expression>, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitSigma(data: Any?, parameters: Collection<Abstract.Parameter>, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitBinOpSequence(data: Any?, left: Abstract.Expression, sequence: Collection<Abstract.BinOpSequenceElem>, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitCase(data: Any?, expressions: Collection<Abstract.Expression>, clauses: Collection<Abstract.FunctionClause>, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitFieldAccs(data: Any?, expression: Abstract.Expression, fieldAccs: Collection<Int>, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitClassExt(data: Any?, isNew: Boolean, baseClass: Abstract.Expression?, implementations: Collection<Abstract.ClassFieldImpl>?, sequence: Collection<Abstract.BinOpSequenceElem>, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitLet(data: Any?, clauses: Collection<Abstract.LetClause>, expression: Abstract.Expression?, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }

    override fun visitNumericLiteral(data: Any?, number: BigInteger, errorData: Abstract.ErrorData?, params: Void?): Any? {
        TODO("not implemented")
    }
}