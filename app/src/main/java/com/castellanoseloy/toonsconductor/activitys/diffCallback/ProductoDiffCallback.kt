package com.castellanoseloy.toonsconductor.activitys.diffCallback

import androidx.recyclerview.widget.DiffUtil
import com.castellanoseloy.toonsconductor.modelos.CarModel

class ProductoDiffCallback (
    private val oldList: List<CarModel>,
private val newList: List<CarModel>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldProduct = oldList[oldItemPosition]
        val newProduct = newList[newItemPosition]
        return oldProduct.id == newProduct.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldProduct = oldList[oldItemPosition]
        val newProduct = newList[newItemPosition]
        return oldProduct == newProduct
    }
}
