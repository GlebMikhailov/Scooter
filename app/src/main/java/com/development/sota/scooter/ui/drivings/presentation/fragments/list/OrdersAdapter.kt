package com.development.sota.scooter.ui.drivings.presentation.fragments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.development.sota.scooter.BASE_IMAGE_URL
import com.development.sota.scooter.R
import com.development.sota.scooter.ui.drivings.domain.entities.OrderStatus
import com.development.sota.scooter.ui.drivings.domain.entities.OrderWithStatus
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_scooter_driving.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class OrdersAdapter(val data: ArrayList<OrderWithStatus>, val manipulatorDelegate: OrderManipulatorDelegate) :
    RecyclerView.Adapter<OrdersAdapter.OrdersViewHolder>() {
    private val tickerJobs = hashMapOf<Long, Job>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder =
        OrdersViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_scooter_driving, parent, false)
        )

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        holder.cardView.linnearLayoutScooterItemFinishButtons.visibility = View.INVISIBLE
        holder.cardView.linnearLayoutScooterItemBookingButtons.visibility = View.INVISIBLE
        holder.cardView.linnearLayoutScooterItemRentButtons.visibility = View.INVISIBLE
        holder.cardView.textViewItemScooterId.text = "#${data[position].order.scooter.id}"
        holder.cardView.textViewItemScooterBatteryPercent.text =
            data[position].order.scooter.getBatteryPercentage()


        Picasso.get()
            .load(BASE_IMAGE_URL + data[position].order.scooter.photo)
            .into(holder.cardView.imageViewScooterItemIcon)


        when (data[position].status) {
            OrderStatus.ACTIVATE -> {
                holder.cardView.linnearLayoutScooterItemRentButtons.visibility = View.VISIBLE
                holder.cardView.textViewItemScooterStateLabel.setText(R.string.scooter_booked)

                holder.cardView.buttonScooterItemCancelBook.setOnClickListener {
                    manipulatorDelegate.cancelOrder(data[position].order.id)
                }

                holder.cardView.buttonScooterFirstActivate.setOnClickListener {
                    manipulatorDelegate.activateOrder(data[position].order.id)
                }

                tickerJobs[data[position].order.id] = GlobalScope.launch {
                    while (data[position].status == OrderStatus.ACTIVATE) {
                        val time = Date().time - data[position].order.parseStartTime().time

                        val minutes = TimeUnit.SECONDS.toMinutes(time.toLong())
                        val seconds = time - minutes * 60

                        delay(1000)

                        holder.cardView.textViewItemScooterStateValue.text =
                            String.format("%02d:%02d", minutes, seconds)
                    }
                }
            }

            OrderStatus.CHOOSE_RATE -> {
                tickerJobs[data[position].order.id]?.cancel()

                holder.cardView.linnearLayoutScooterItemRentButtons.visibility = View.VISIBLE
                holder.cardView.textViewItemScooterStateLabel.setText(R.string.scooter_booked)

                tickerJobs[data[position].order.id] = GlobalScope.launch {
                    while (data[position].status == OrderStatus.ACTIVATE) {
                        val time = Date().time - data[position].order.parseStartTime().time

                        val minutes = TimeUnit.SECONDS.toMinutes(time.toLong())
                        val seconds = time - minutes * 60

                        delay(1000)

                        holder.cardView.textViewItemScooterStateValue.text =
                            String.format("%02d:%02d", minutes, seconds)
                    }
                }
            }

        }
    }

    override fun getItemCount(): Int = data.size

    inner class OrdersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.cardViewScooterItem
    }
}