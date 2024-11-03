package com.example.myapitest.adapter

import com.google.gson.Gson
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.R
import com.example.myapitest.model.CarDetails
import com.example.myapitest.model.CarItem
import com.example.myapitest.ui.CircleTransform
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class ItemAdapter (

    private val cars: List<CarDetails>,
    private val itemClickListener: (CarDetails) -> Unit,

) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>(){

    class ItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image)
        val carYearTextView: TextView = view.findViewById(R.id.year)
        val carModelTextView: TextView = view.findViewById(R.id.model)
        val carLicenseTextView: TextView = view.findViewById(R.id.license)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = cars.size



    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val car = cars[position]
        holder.itemView.setOnClickListener{
            itemClickListener.invoke(car)
        }
        holder.carYearTextView.text = "${car.year}"
        holder.carModelTextView.text = "${car.name}"
        holder.carLicenseTextView.text = "${car.license}"
        print(car.imageUrl)
        Picasso.get()
            .load(car.imageUrl)
            .placeholder(R.drawable.ic_download)
            .error(R.drawable.ic_error)
            .transform(CircleTransform())
            .into(holder.imageView)
    }
}