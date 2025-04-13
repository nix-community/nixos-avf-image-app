package io.mkg20001.nixosimage.ui

import android.R as R
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.selects.select

class DropdownItem(val id: String, val displayText: String) {
    var placeholder: Boolean = false

    constructor(_placeholder: Boolean, displayText: String): this("", displayText) {
        placeholder = _placeholder
    }

    override fun toString(): String {
        return displayText // This is what the dropdown will show
    }

    companion object {
        fun setItems(ctx: Context, items: List<DropdownItem>, dropdown: Spinner) {
            val adapter = ArrayAdapter(ctx, R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            dropdown.adapter = adapter
        }

        fun getItem(dropdown: Spinner): DropdownItem? {
            return dropdown.selectedItem as DropdownItem?
        }

        fun selectedAndNotPlaceholder(dropdown: Spinner): Boolean {
            val item = getItem(dropdown)
            return item != null && !item.placeholder
        }

        fun onChange(dropdown: Spinner, handler: (item: DropdownItem?) -> Unit) {
            dropdown.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    handler(getItem(dropdown))
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {
                    handler(getItem(dropdown))
                }
            }
        }
    }
}

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
fun ExtendedDropdownMenuItem(
    item: ExtItem,
    onValueChange: (item: ExtItem) -> Unit,
) {
    DropdownMenuItem(
        text = { item.label },
        enabled = item.real,
        onClick = {
            // Notify parent of value change
            onValueChange(item)
        }
    )
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
            /* items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        selectedItem = item
                        expanded = false
                    }
                )
            } */
        }
    }

    /* DropdownMenu(
        modifier = modifier, expanded = expanded.value,
        onDismissRequest = { expanded.value = false }
    ) {
        items.forEach {
            ExtendedDropdownMenuItem(item = it) {
                onValueChange(it)
            }
        }
    } */
}