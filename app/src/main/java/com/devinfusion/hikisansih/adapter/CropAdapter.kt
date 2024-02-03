package com.devinfusion.hikisansih.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devinfusion.hikisansih.R
import com.devinfusion.hikisansih.model.CropDetails

class CropAdapter(private val crops: List<CropDetails>) : RecyclerView.Adapter<CropAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cropNameTextView: TextView = itemView.findViewById(R.id.cropName)
        val cropNameImageView: ImageView = itemView.findViewById(R.id.cropImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.crop_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val crop = crops[position]

        holder.cropNameTextView.text = crop.name
        holder.cropNameImageView.setImageResource(crop.image)
        // Bind other crop details to their respective TextViews here
    }

    override fun getItemCount(): Int {
        return crops.size
    }
}
