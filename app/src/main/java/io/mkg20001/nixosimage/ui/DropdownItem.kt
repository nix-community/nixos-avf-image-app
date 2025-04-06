package io.mkg20001.nixosimage.ui

import android.R as R
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
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
