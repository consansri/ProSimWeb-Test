package me.c3.ui.styled.table

import javax.swing.JComponent

abstract class CVirtualTable(
): JComponent() {

    abstract val headers: Array<String>

    init{
        this.setUI(CVirtualTableUI())
    }




}