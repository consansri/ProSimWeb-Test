package me.c3.ui.styled

import kotlinx.coroutines.*
import me.c3.ui.Workspace
import me.c3.ui.components.styled.*
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.icons.ProSimIcons
import java.awt.*
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.JOptionPane
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class COptionPane(themeManager: ThemeManager, scaleManager: ScaleManager) : JOptionPane() {

    companion object {
        fun showInputDialog(themeManager: ThemeManager, scaleManager: ScaleManager, parent: Component, message: String): Deferred<String> {
            val resultDeferred = CompletableDeferred<String>()

            val cDialog = CDialog(themeManager, scaleManager, parent)
            val cPanel = CPanel(themeManager, scaleManager, primary = false, isOverlay = true, roundCorners = true)
            val cLabel = CLabel(themeManager, scaleManager, message)
            val cTextArea = CTextField(themeManager, scaleManager, mode = CTextFieldUI.Type.TEXT)

            cTextArea.addKeyListener(object : KeyAdapter() {
                override fun keyReleased(e: KeyEvent?) {
                    if (e?.keyCode == KeyEvent.VK_ENTER) {
                        resultDeferred.complete(cTextArea.text)
                        cDialog.dispose()
                    }
                }
            })

            cTextArea.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent?) {
                    resultDeferred.complete("")
                    cDialog.dispose()
                }
            })

            cDialog.layout = BorderLayout()

            cPanel.layout = GridBagLayout()
            val gbc = GridBagConstraints()
            gbc.gridx = 0
            gbc.gridy = 0
            gbc.weightx = 1.0
            gbc.weighty = 0.0
            gbc.fill = GridBagConstraints.HORIZONTAL

            cPanel.add(cLabel, gbc)
            gbc.gridy = 1
            gbc.fill = GridBagConstraints.HORIZONTAL

            cPanel.add(cTextArea, gbc)

            cDialog.add(cPanel, BorderLayout.CENTER)
            cDialog.pack()
            cDialog.setLocationRelativeTo(null)
            cDialog.isVisible = true

            cTextArea.requestFocus()

            return resultDeferred
        }

        fun showDirectoryChooser(themeManager: ThemeManager, scaleManager: ScaleManager, icons: ProSimIcons, parent: Component, message: String): Deferred<File?> {
            val resultDeferred = CompletableDeferred<File?>()

            SwingUtilities.invokeLater {
                var selectedFile: File? = null

                // Scrollable Tree View
                val root = DefaultMutableTreeNode("root")
                val treeModel = DefaultTreeModel(root)
                val tree = CTree(themeManager, scaleManager, icons, treeModel)
                val cScrollPane = CScrollPane(themeManager, scaleManager, false)
                cScrollPane.setViewportView(tree)
                cScrollPane.size = Dimension(300, 300)

                // Title Label
                val titleLabel = CLabel(themeManager, scaleManager, message)
                titleLabel.horizontalAlignment = SwingConstants.CENTER

                // Current Path Identificator
                val currPathTextField = CTextField(themeManager, scaleManager, CTextFieldUI.Type.TEXT)
                currPathTextField.isEditable = false

                // Select Button
                val selectButton = CTextButton(themeManager, scaleManager, "select")

                // Content Panel
                val cPanel = CPanel(themeManager, scaleManager, primary = false, isOverlay = true, roundCorners = true)
                cPanel.layout = GridBagLayout()

                // Dialog Frame
                val cDialog = CDialog(themeManager, scaleManager, parent)

                // Add Components to Content Panel
                val gbc = GridBagConstraints()
                gbc.gridx = 0
                gbc.gridy = 0
                gbc.weightx = 1.0
                gbc.weighty = 0.0
                gbc.fill = GridBagConstraints.HORIZONTAL
                cPanel.add(titleLabel, gbc)

                gbc.gridx = 0
                gbc.gridy = 1
                cPanel.add(currPathTextField, gbc)

                gbc.gridy = 2
                gbc.weighty = 1.0
                gbc.fill = GridBagConstraints.BOTH
                cPanel.add(cScrollPane, gbc)

                gbc.gridy = 3
                gbc.weighty = 0.0
                gbc.fill = GridBagConstraints.HORIZONTAL
                cPanel.add(selectButton, gbc)

                // Lazyload Tree Files
                CoroutineScope(Dispatchers.Main).launch {
                    val rootDirectories = File.listRoots()
                    for (rootDirectory in rootDirectories) {
                        val dirRoot = DefaultMutableTreeNode(Workspace.TreeFile(rootDirectory, displayPath = true))
                        root.add(dirRoot)
                        if (rootDirectory.isDirectory) {
                            dirRoot.add(DefaultMutableTreeNode("Loading ..."))
                        }
                        treeModel.reload(root)
                    }
                }

                tree.addTreeExpansionListener(object : TreeExpansionListener {
                    override fun treeExpanded(event: TreeExpansionEvent) {
                        val node = event.path.lastPathComponent as? DefaultMutableTreeNode ?: return
                        if (node.childCount == 1) {
                            CoroutineScope(Dispatchers.IO).launch {
                                lazyLoadDirectory(treeModel, node, showOnlyDirectories = true)
                            }
                        }
                    }

                    override fun treeCollapsed(event: TreeExpansionEvent?) {}
                })

                // Attach Listeners
                tree.addTreeSelectionListener {
                    val selectedNode = tree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return@addTreeSelectionListener
                    val treeFile = (selectedNode.userObject as? Workspace.TreeFile) ?: return@addTreeSelectionListener
                    selectedFile = treeFile.file
                    currPathTextField.text = treeFile.file.absolutePath
                }

                selectButton.addActionListener {
                    resultDeferred.complete(selectedFile)
                    cDialog.dispose()
                }

                tree.addKeyListener(object : KeyAdapter() {
                    override fun keyTyped(e: KeyEvent?) {
                        if (e?.keyCode == KeyEvent.VK_ENTER) {
                            resultDeferred.complete(selectedFile)
                            cDialog.dispose()
                        }
                        if (e?.keyCode == KeyEvent.VK_ESCAPE) {
                            resultDeferred.complete(null)
                            cDialog.dispose()
                        }
                    }
                })

                tree.addFocusListener(object : FocusAdapter() {
                    override fun focusLost(e: FocusEvent?) {
                        resultDeferred.complete(null)
                        cDialog.dispose()
                    }
                })

                // Add Content Panel to Dialog Frame
                cDialog.layout = BorderLayout()
                cDialog.add(cPanel, BorderLayout.CENTER)
                cDialog.size = Dimension(Toolkit.getDefaultToolkit().screenSize.width / 16 * 4, Toolkit.getDefaultToolkit().screenSize.height / 9 * 4)
                cDialog.setLocationRelativeTo(null)
                cDialog.isVisible = true

                tree.requestFocus()
            }

            return resultDeferred
        }

        private fun lazyLoadDirectory(treeModel: DefaultTreeModel, parentNode: DefaultMutableTreeNode, showOnlyDirectories: Boolean) {
            val directory = parentNode.userObject as? Workspace.TreeFile ?: return
            val files = directory.file.listFiles() ?: emptyArray()
            parentNode.removeAllChildren()
            for (file in files) {
                if (!showOnlyDirectories || file.isDirectory) {
                    val subFileNode = DefaultMutableTreeNode(Workspace.TreeFile(file))
                    parentNode.add(subFileNode)
                    if (file.isDirectory) {
                        subFileNode.add(DefaultMutableTreeNode("Loading ..."))
                    }
                }
            }
            treeModel.reload(parentNode)
        }
    }

    init {
        this.setUI(COptionPaneUI(themeManager, scaleManager))
    }


}