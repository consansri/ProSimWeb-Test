package me.c3.ui.styled

import kotlinx.coroutines.*
import me.c3.ui.Workspace
import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import me.c3.ui.resources.icons.ProSimIcons
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

class COptionPane(tm: ThemeManager, sm: ScaleManager) : JOptionPane() {

    companion object {
        fun showInputDialog(tm: ThemeManager, sm: ScaleManager, parent: Component, message: String): Deferred<String> {
            val resultDeferred = CompletableDeferred<String>()

            val cDialog = CDialog(tm, sm, parent)
            val cPanel = CPanel(tm, sm, primary = false, isOverlay = true, roundCorners = true)
            val cLabel = CLabel(tm, sm, message, FontType.BASIC)
            val cTextArea = CTextField(tm, sm, FontType.BASIC)

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

        fun showDirectoryChooser(tm: ThemeManager, sm: ScaleManager, icons: ProSimIcons, parent: Component, message: String): Deferred<File?> {
            val resultDeferred = CompletableDeferred<File?>()

            SwingUtilities.invokeLater {
                var selectedFile: File? = null

                // Scrollable Tree View
                val root = DefaultMutableTreeNode("root")
                val treeModel = DefaultTreeModel(root)
                val tree = CTree(tm, sm, icons, treeModel, FontType.BASIC)
                val cScrollPane = CScrollPane(tm, sm, false)
                cScrollPane.setViewportView(tree)
                cScrollPane.size = Dimension(300, 300)

                // Title Label
                val titleLabel = CLabel(tm, sm, message, FontType.TITLE)
                titleLabel.horizontalAlignment = SwingConstants.CENTER

                // Current Path Identificator
                val currPathTextField = CTextField(tm, sm, FontType.BASIC)
                currPathTextField.isEditable = false

                // Select Button
                val selectButton = CTextButton(tm, sm, "select", FontType.BASIC)

                // Content Panel
                val cPanel = CPanel(tm, sm, primary = false, isOverlay = true, roundCorners = true)
                cPanel.layout = GridBagLayout()

                // Dialog Frame
                val cDialog = CDialog(tm, sm, parent)

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

        fun confirm(tm: ThemeManager, sm: ScaleManager, icons: ProSimIcons, parent: Component, message: String): Deferred<Boolean>{
            val resultDeferred = CompletableDeferred<Boolean>()

            val cDialog = CDialog(tm, sm, parent)
            val cPanel = CPanel(tm, sm, primary = false, isOverlay = true, roundCorners = true)
            val cLabel = CLabel(tm, sm, message, FontType.BASIC)
            val cConfirmBtn = CTextButton(tm, sm, "confirm", FontType.BASIC).apply {
                addActionListener {
                    resultDeferred.complete(true)
                    cDialog.dispose()
                }
            }
            val cCancelBtn = CTextButton(tm, sm, "cancel", FontType.BASIC).apply {
                addActionListener {
                    resultDeferred.complete(false)
                    cDialog.dispose()
                }
            }

            cDialog.layout = BorderLayout()
            cPanel.layout = GridBagLayout()

            val gbc = GridBagConstraints()
            gbc.gridx = 0
            gbc.gridy = 0
            gbc.gridwidth = 2
            gbc.weightx = 1.0
            gbc.weighty = 0.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            cPanel.add(cLabel, gbc)

            gbc.gridwidth = 1
            gbc.gridy = 1
            gbc.fill = GridBagConstraints.HORIZONTAL
            cPanel.add(cConfirmBtn, gbc)

            gbc.gridx = 1
            cPanel.add(cCancelBtn, gbc)

            cDialog.add(cPanel, BorderLayout.CENTER)
            cDialog.pack()
            cDialog.setLocationRelativeTo(null)
            cDialog.isVisible = true
            cDialog.requestFocus()

            cDialog.addFocusListener(object : FocusAdapter(){
                override fun focusLost(e: FocusEvent?) {
                    resultDeferred.complete(false)
                    cDialog.dispose()
                }
            })

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
        this.setUI(COptionPaneUI(tm, sm))
    }


}