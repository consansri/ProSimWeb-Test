package prosim.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import kotlinx.coroutines.*
import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import prosim.uilib.styled.tree.CTree
import prosim.uilib.styled.tree.NodeInformationProvider
import prosim.uilib.workspace.Workspace
import java.awt.*
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.JOptionPane
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class COptionPane : JOptionPane() {

    companion object {
        fun <T> showSelector(parent: Component, title: String, entries: Collection<T>, fontType: FontType = FontType.BASIC): Pair<CDialog, Deferred<T?>> {
            val resultDeferred = CompletableDeferred<T?>()

            val (cDialog, contentPanel) = createClosableOptionPane(parent, title) {
                resultDeferred.complete(null)
            }

            contentPanel.layout = GridBagLayout()
            val gbc = GridBagConstraints()
            gbc.gridy = 0
            gbc.gridx = 0
            gbc.gridwidth = 1
            gbc.gridheight = 1
            gbc.weightx = 1.0
            gbc.weighty = 0.0
            gbc.fill = GridBagConstraints.HORIZONTAL

            entries.forEach { value ->
                val button = CTextButton(value.toString(), fontType).apply {
                    addActionListener {
                        resultDeferred.complete(value)
                        cDialog.dispose()
                    }
                }

                contentPanel.add(button, gbc)

                gbc.gridy += 1
            }

            show(cDialog)

            return cDialog to resultDeferred
        }

        fun showInputDialog(parent: Component, message: String): Deferred<String> {
            val resultDeferred = CompletableDeferred<String>()

            val (cDialog, cPanel) = createBasicOptionPane(parent, message)

            val textField = CTextField(FontType.BASIC)

            textField.addKeyListener(object : KeyAdapter() {
                override fun keyReleased(e: KeyEvent?) {
                    if (e?.keyCode == KeyEvent.VK_ENTER) {
                        resultDeferred.complete(textField.text)
                        cDialog.dispose()
                    }
                }
            })

            textField.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent?) {
                    resultDeferred.complete("")
                    cDialog.dispose()
                }
            })

            cPanel.layout = GridBagLayout()
            val gbc = GridBagConstraints()
            gbc.gridx = 0
            gbc.gridy = 0
            gbc.weightx = 1.0
            gbc.weighty = 0.0
            gbc.fill = GridBagConstraints.HORIZONTAL

            cPanel.add(textField, gbc)

            show(cDialog)

            textField.requestFocus()

            return resultDeferred
        }

        fun showDirectoryChooser(parent: Component, message: String): Deferred<File?> {
            val resultDeferred = CompletableDeferred<File?>()

            var selectedFile: File? = null

            val (cDialog, contentPanel) = createBasicOptionPane(parent, message)

            // Scrollable Tree View
            val root = DefaultMutableTreeNode("root")
            val treeModel = DefaultTreeModel(root)
            val tree = CTree(treeModel, FontType.BASIC, object : NodeInformationProvider<File>{
                override fun getIcon(userObject: File): FlatSVGIcon? = null
                override fun getName(userObject: File): String? = userObject.name
                override fun getFgColor(userObject: File): Color? = null
                override val expandedBranchIcon: FlatSVGIcon = UIStates.icon.get().folder
                override val collapsedBranchIcon: FlatSVGIcon = UIStates.icon.get().folder
                override val defaultLeafIcon: FlatSVGIcon = UIStates.icon.get().folder
            }, File::class)
            val cScrollPane = CScrollPane()
            cScrollPane.setViewportView(tree)
            cScrollPane.preferredSize = Dimension(600, 800)

            // Current Path Identificator
            val currPathTextField = CTextField(FontType.BASIC)
            currPathTextField.isEditable = false

            // Select Button
            val selectButton = CTextButton("select", FontType.BASIC)

            // Content Panel
            // Add Components to Content Panel
            contentPanel.layout = GridBagLayout()
            val gbc = GridBagConstraints()
            gbc.gridx = 0
            gbc.gridy = 0
            gbc.weightx = 1.0
            gbc.weighty = 0.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            contentPanel.add(currPathTextField, gbc)

            gbc.gridy = 1
            gbc.weighty = 1.0
            gbc.fill = GridBagConstraints.BOTH
            contentPanel.add(cScrollPane, gbc)

            gbc.gridy = 2
            gbc.weighty = 0.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            contentPanel.add(selectButton, gbc)

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

            // Show

            val size = Dimension(Toolkit.getDefaultToolkit().screenSize.width / 16 * 4, Toolkit.getDefaultToolkit().screenSize.height / 9 * 4)
            show(cDialog, size)

            tree.requestFocus()

            return resultDeferred
        }

        fun confirm(parent: Component, message: String): Deferred<Boolean> {
            val resultDeferred = CompletableDeferred<Boolean>()

            val (cDialog, _, contentPanel) = CDialog.createWithTitle(message, parent)

            val cConfirmBtn = CTextButton("confirm", FontType.BASIC).apply {
                addActionListener {
                    resultDeferred.complete(true)
                    cDialog.dispose()
                }
            }
            val cCancelBtn = CTextButton("cancel", FontType.BASIC).apply {
                addActionListener {
                    resultDeferred.complete(false)
                    cDialog.dispose()
                }
            }

            contentPanel.layout = GridBagLayout()

            val gbc = GridBagConstraints()
            gbc.gridx = 0
            gbc.gridy = 0
            gbc.weightx = 1.0
            gbc.weighty = 1.0
            gbc.fill = GridBagConstraints.HORIZONTAL

            gbc.fill = GridBagConstraints.HORIZONTAL
            contentPanel.add(cConfirmBtn, gbc)

            gbc.gridx = 1
            contentPanel.add(cCancelBtn, gbc)

            val size = Dimension(Toolkit.getDefaultToolkit().screenSize.width / 16 * 4, Toolkit.getDefaultToolkit().screenSize.height / 9 * 1)
            show(cDialog)

            cDialog.requestFocus()

            cDialog.addFocusListener(object : FocusAdapter() {
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

        /**
         * Creates a Basic Dialog and returns it and the content panel.
         */
        private fun createBasicOptionPane(parent: Component, title: String): Pair<CDialog, CPanel> {
            val cDialog = CDialog(parent)
            val cPanel = CPanel(primary = false, isOverlay = true, roundCorners = true)
            val cContentPanel = CPanel(primary = false, roundCorners = true)
            val cLabel = CLabel(title, FontType.BASIC)

            cPanel.layout = BorderLayout()
            cPanel.add(cLabel, BorderLayout.NORTH)
            cPanel.add(cContentPanel, BorderLayout.CENTER)

            cDialog.layout = BorderLayout()
            cDialog.add(cPanel, BorderLayout.CENTER)

            cDialog.setLocationRelativeTo(null)

            return Pair(cDialog, cContentPanel)
        }

        /**
         *
         */
        private fun createClosableOptionPane(parent: Component, title: String, onClose: () -> Unit): Pair<CDialog, CPanel> {
            val cDialog = CDialog(parent)
            val cPanel = CPanel(primary = false, isOverlay = true, roundCorners = true)
            val cContentPanel = CPanel(primary = false, roundCorners = true)
            val cLabel = CLabel(title, FontType.BASIC)
            val closeButton = CIconButton(UIStates.icon.get().close).apply {
                addActionListener {
                    onClose()
                    cDialog.dispose()
                }
            }

            cPanel.layout = GridBagLayout()
            val gbc = GridBagConstraints()
            gbc.weightx = 1.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            cPanel.add(cLabel, gbc)

            gbc.gridx = 1
            gbc.weightx = 0.0
            gbc.fill = GridBagConstraints.NONE
            cPanel.add(closeButton, gbc)

            gbc.gridx = 0
            gbc.gridwidth = 2
            gbc.gridy = 1
            gbc.weightx = 1.0
            gbc.weighty = 1.0
            gbc.fill = GridBagConstraints.BOTH
            cPanel.add(cContentPanel, gbc)

            cDialog.layout = BorderLayout()
            cDialog.add(cPanel, BorderLayout.CENTER)

            cDialog.setLocationRelativeTo(null)

            return Pair(cDialog, cContentPanel)
        }

        /**
         * Creates a Scrollable Dialog and returns it and the scroll panel, and it's a viewport panel.
         */
        private fun createScrollOptionPane(parent: Component, title: String): Triple<CDialog, CScrollPane, CPanel> {
            val cDialog = CDialog(parent)
            cDialog.maximumSize = Dimension(Toolkit.getDefaultToolkit().screenSize.width / 2, Toolkit.getDefaultToolkit().screenSize.height / 2)
            cDialog.minimumSize = Dimension(Toolkit.getDefaultToolkit().screenSize.width / 16, Toolkit.getDefaultToolkit().screenSize.height / 16)
            val cPanel = CPanel(primary = false, isOverlay = true, roundCorners = true)
            val contentPanel = CPanel(primary = false, roundCorners = true)
            val cScrollPane = CScrollPane()
            cScrollPane.setViewportView(contentPanel)
            val cLabel = CLabel(title, FontType.BASIC)

            cPanel.layout = BorderLayout()
            cPanel.add(cLabel, BorderLayout.NORTH)
            cPanel.add(cScrollPane, BorderLayout.CENTER)

            cDialog.layout = BorderLayout()
            cDialog.add(cPanel, BorderLayout.CENTER)

            cDialog.setLocationRelativeTo(null)

            return Triple(cDialog, cScrollPane, contentPanel)
        }

        private fun show(cDialog: CDialog, size: Dimension? = null) {
            cDialog.pack()
            if (size != null) {
                cDialog.contentPane.size = size
            } else {
                cDialog.contentPane.size = Dimension(
                    cDialog.insets.left + cDialog.insets.right + cDialog.layout.preferredLayoutSize(cDialog).width,
                    cDialog.insets.top + cDialog.insets.bottom + cDialog.layout.preferredLayoutSize(cDialog).height
                )
            }
            cDialog.revalidate()
            cDialog.setLocationRelativeTo(null)
            cDialog.isVisible = true
        }
    }

    init {
        this.setUI(COptionPaneUI())
    }


}