package org.arend.actions

import com.intellij.openapi.actionSystem.DefaultActionGroup
import org.arend.ArendIcons.AREND

class ArendActions : DefaultActionGroup("Arend", "Show Arend Actions", AREND) {
    override fun isPopup(): Boolean = true
}
