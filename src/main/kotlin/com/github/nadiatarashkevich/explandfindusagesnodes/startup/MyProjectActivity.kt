package com.github.nadiatarashkevich.explandfindusagesnodes.startup

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JTree

class MyProjectActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val connection = project.messageBus.connect()
        connection.subscribe(ToolWindowManagerListener.TOPIC, object : ToolWindowManagerListener {
            override fun stateChanged(toolWindowManager: ToolWindowManager) {
                val toolWindow = toolWindowManager.getToolWindow(ToolWindowId.FIND) ?: return
                if (!toolWindow.isVisible) return
                // Expand all trees within the Find Usages tool window
                ApplicationManager.getApplication().invokeLater {
                    val contents = toolWindow.contentManager.contents
                    contents.forEach { content ->
                        expandAllTrees(content.component)
                    }
                    logger<MyProjectActivity>().info("Find Usages tool window trees expanded")
                }
            }
        })
    }

    private fun expandAllTrees(component: Component) {
        val root = component as? JComponent ?: return
        // Find all Swing JTree and IntelliJ Tree components and expand them
        val swingTrees: Collection<JTree> = UIUtil.findComponentsOfType(root, JTree::class.java)
        swingTrees.forEach { tree ->
            try {
                TreeUtil.expandAll(tree)
            } catch (_: Throwable) {
                // ignore individual expansion errors
            }
        }
        val ideaTrees: Collection<Tree> = UIUtil.findComponentsOfType(root, Tree::class.java)
        ideaTrees.forEach { tree ->
            try {
                TreeUtil.expandAll(tree)
            } catch (_: Throwable) {
                // ignore
            }
        }
    }
}