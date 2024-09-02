package ui.uilib.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import emulator.kit.nativeLog
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.label.CLabel
import ui.uilib.params.FontType
import ui.uilib.text.CTextField


@Composable
fun InputDialog(title: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit, valid: (String) -> Boolean) {

    var text by remember { mutableStateOf("") }

    Dialog(onDismissRequest = {
        onDismiss()
    }) {

        BoxWithConstraints {
            Column(
                Modifier
                    .wrapContentWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CLabel(text = title, fontType = FontType.MEDIUM, modifier = Modifier.fillMaxWidth())
                }

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CTextField(
                        text,
                        onValueChange = {
                            text = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        error = text.isEmpty()
                    )
                }

                Spacer(Modifier.height(UIState.Scale.value.SIZE_INSET_MEDIUM))

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CButton(
                        text = "Confirm",
                        onClick = {
                            if (text.isNotEmpty()) {
                                nativeLog("Confirm: -> ")
                                onConfirm(text)
                            }
                        }, active = text.isNotEmpty(),
                        modifier = Modifier.weight(1.0f)
                    )
                    CButton(
                        text = "Cancel", onClick = {
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.0f)
                    )
                }
            }
        }


    }

}
