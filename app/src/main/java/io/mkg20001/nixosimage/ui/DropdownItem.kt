package io.mkg20001.nixosimage.ui

import android.R as R
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner

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
    }
}
