package io.mkg20001.nixosimage.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

data class ExtItem(
    val id: String,
    val label: String,
    // This is inverted so we can do "stateBla.value?.real" to check if anything valid is selected
    val real: Boolean = false
) {
    constructor(label: String): this("", label, false)
    constructor(id: String, label: String): this(id, label, true)
}


@Composable
fun MyDropdown(modifier: Modifier, style: TextStyle, selectedItem: ExtItem?, items: List<ExtItem>, onValueChange: (item: ExtItem) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    if (selectedItem == null && !items.isEmpty()) {
        onValueChange(items[0])
    }

    Box(modifier = modifier.wrapContentSize(Alignment.TopStart)) {
        Text(
            text = selectedItem?.label ?: "(none)",
            style = style,
            modifier = modifier
                .clickable { expanded = true }
                .padding(16.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = modifier
        ) {
            items.forEach {
                DropdownMenuItem(
                    text = { Text(it.label, style = style) },
                    enabled = it.real,
                    onClick = {
                        expanded = false
                        onValueChange(it)
                    }
                )
            }
        }
    }
}