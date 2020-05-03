package ie.wit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.wit.R
import ie.wit.models.WeightMatesModel
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.card_caloriecounter.view.*

interface CalorieListener {
    fun onCalorieClick(calorie: WeightMatesModel)
}

class WeightMatesAdapter constructor(var calories: ArrayList<WeightMatesModel>,
                                  private val listener: CalorieListener, reportall : Boolean)
    : RecyclerView.Adapter<WeightMatesAdapter.MainHolder>() {

    val reportAll = reportall

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        return MainHolder(
            LayoutInflater.from(parent?.context).inflate(
                R.layout.card_caloriecounter,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val calorie = calories[holder.adapterPosition]
        holder.bind(calorie,listener,reportAll)
    }

    override fun getItemCount(): Int = calories.size

    fun removeAt(position: Int) {
        calories.removeAt(position)
        notifyItemRemoved(position)
    }

    class MainHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(calorie: WeightMatesModel, listener: CalorieListener, reportAll: Boolean) {
            itemView.tag = calorie
            itemView.calorieamount.text = calorie.amount.toString()
            itemView.caloriemethod.text = calorie.calorietype

            if(!reportAll)
                itemView.setOnClickListener { listener.onCalorieClick(calorie) }

            if(!calorie.profilepic.isEmpty()) {
                Picasso.get().load(calorie.profilepic.toUri())
                    //.resize(180, 180)
                    .transform(CropCircleTransformation())
                    .into(itemView.imageIcon)
            }
            else
                itemView.imageIcon.setImageResource(R.mipmap.ic_launcher_weightmates_foreground)
        }
    }
}