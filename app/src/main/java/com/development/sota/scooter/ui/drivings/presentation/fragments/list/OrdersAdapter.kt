package com.development.sota.scooter.ui.drivings.presentation.fragments.list

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.development.sota.scooter.R
import com.development.sota.scooter.ui.drivings.DrivingsActivity
import com.development.sota.scooter.ui.drivings.domain.entities.OrderStatus
import com.development.sota.scooter.ui.drivings.domain.entities.OrderWithStatus
import com.development.sota.scooter.ui.map.data.RateType
import kotlinx.android.synthetic.main.item_scooter_driving.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class OrdersAdapter(
    var data: ArrayList<OrderWithStatus>,
    val context: Context,
    val manipulatorDelegate: OrderManipulatorDelegate
) :
    RecyclerView.Adapter<OrdersAdapter.OrdersViewHolder>() {
    private val tickerJobs = hashMapOf<Long, Job>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder =
        OrdersViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_scooter_driving, parent, false)
        )

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        holder.constaraintViewParent.elevation = 0f
        holder.cardView.linnearLayoutScooterItemFinishButtons.visibility = View.GONE
        holder.cardView.linnearLayoutScooterItemBookingButtons.visibility = View.GONE
        holder.cardView.linnearLayoutScooterItemRentButtons.visibility = View.GONE
        holder.cardView.linnearLayoutScooterItemFirstBookButtons.visibility = View.GONE

        holder.cardView.textViewItemScooterId.text = "#${data[position].order.scooter}"
        holder.cardView.textViewItemScooterBatteryPercent.text =
            data[position].scooter.getBatteryPercentage()

        when (data[position].status) {
            OrderStatus.BOOKED -> {
                holder.cardView.linnearLayoutScooterItemBookingButtons.visibility = View.VISIBLE
                holder.cardView.textViewItemScooterStateLabel.setText(R.string.scooter_booked)

                holder.cardView.buttonScooterItemCancelBook.setOnClickListener {
                    Log.w("FIRST ACTIVATE", "CLICK")
                    manipulatorDelegate.cancelOrder(data[position].order.id)

                    tickerJobs[data[position].order.id]?.cancel()
                }

                holder.cardView.buttonScooterItemActivate.setOnClickListener {
                    Log.w("FIRST ACTIVATE", "CLICK")
                    data[position].status = OrderStatus.CHOOSE_RATE

                    (context as DrivingsActivity).runOnUiThread {
                        notifyDataSetChanged()
                        notifyItemChanged(position)
                    }
                }

                tickerJobs[data[position].order.id] = GlobalScope.launch {
                    try {
                        val orderTime = data[position].order.parseStartTime()

                        while (true) {

                            if(orderTime != null) {
                                val time =
                                    LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - orderTime

                                val rawMinutes = TimeUnit.MILLISECONDS.toMinutes(time)

                                val hours = rawMinutes / 60
                                val minutes = rawMinutes % 60
                                val seconds = time / 1000 - minutes * 60 - hours * 3600

                                (context as DrivingsActivity).runOnUiThread {
                                    holder.cardView.textViewItemScooterStateValue.text =
                                        String.format("%d:%02d:%02d", hours, minutes, seconds)
                                }
                            }

                            delay(1000)
                        }
                    } catch (e: Exception) {
                    }
                }
            }

            OrderStatus.CHOOSE_RATE -> {
                holder.cardView.linnearLayoutScooterItemRentButtons.visibility = View.VISIBLE
                holder.cardView.textViewItemScooterStateLabel.setText(R.string.scooter_booked)

                holder.cardView.buttonScooterItemPerMinute.setOnClickListener {
                    manipulatorDelegate.setRateAndActivate(data[position].order.id, RateType.MINUTE)
                }

                holder.cardView.textViewtextViewItemScooterHourPricing.setOnClickListener {
                    manipulatorDelegate.setRateAndActivate(data[position].order.id, RateType.HOUR)
                }

                tickerJobs[data[position].order.id] = GlobalScope.launch {
                    try {
                        val orderTime = data[position].order.parseStartTime()

                        while (true) {

                            if(orderTime != null) {
                                val time =
                                    LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - orderTime
                                val rawMinutes = TimeUnit.MILLISECONDS.toMinutes(time)

                                val hours = rawMinutes / 60
                                val minutes = rawMinutes % 60
                                val seconds = time / 1000 - minutes * 60 - hours * 3600



                                (context as DrivingsActivity).runOnUiThread {
                                    holder.cardView.textViewItemScooterStateValue.text =
                                        String.format("%d:%02d:%02d", hours, minutes, seconds)
                                }
                            }
                            delay(1000)
                        }
                    } catch (e: Exception) {
                    }
                }
            }

            OrderStatus.ACTIVATED -> {
                tickerJobs[data[position].order.id]?.cancel()

                holder.cardView.linnearLayoutScooterItemFinishButtons.visibility = View.VISIBLE

                holder.cardView.buttonScooterItemPause.setOnClickListener {
                    //  manipulatorDelegate.setRateAndActivate(data[position].order.id, RateType.MINUTE)
                }

                holder.cardView.buttonScooterFinish.setOnClickListener {
                    //  manipulatorDelegate.setRateAndActivate(data[position].order.id, RateType.HOUR)
                }

                holder.cardView.textViewItemScooterStateLabel.setText(R.string.scooter_rented)
                holder.cardView.textViewItemScooterStateValue.text =
                    String.format("%.2f", data[position].order.cost)
            }

        }
    }

    override fun getItemCount(): Int = data.size

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        tickerJobs.values.forEach(Job::cancel)
    }

    inner class OrdersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.cardViewScooterItem
        val constaraintViewParent: ConstraintLayout = itemView.constraintLayoutItemScooterParent
    }
}