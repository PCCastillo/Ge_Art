package com.example.geart_20.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.geart_20.R
import com.example.geart_20.model.Commission

class CommissionAdapter(
    private val commissions: List<Commission>,
    private val onItemClick: (Commission) -> Unit = {}
) : RecyclerView.Adapter<CommissionAdapter.CommissionViewHolder>() {

    class CommissionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Ahora capturamos AMBOS campos de texto
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvDescription)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommissionViewHolder {
        // CORRECCIÓN: Cambiamos R.id por R.layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_commission, parent, false)
        return CommissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommissionViewHolder, position: Int) {
        val commission = commissions[position]

        // Asignamos el título. Si es un dato antiguo de prueba y está vacío, mostramos "Sin título"
        holder.tvTitle.text = if (commission.title.isNotEmpty()) commission.title else "Sin título"

        // Asignamos la descripción al texto más pequeño
        holder.tvDesc.text = commission.description

        holder.tvPrice.text = "$${commission.price}"
        holder.tvStatus.text = commission.status
        holder.tvStatus.setTextColor(
            ContextCompat.getColor(holder.itemView.context, statusColorRes(commission.status))
        )

        holder.itemView.setOnClickListener {
            onItemClick(commission)
        }
    }

    override fun getItemCount() = commissions.size

    private fun statusColorRes(status: String): Int = when (status) {
        "PENDING" -> R.color.geart_status_pending
        "DIRECT_REQUEST" -> R.color.geart_status_direct
        "ACCEPTED" -> R.color.geart_status_accepted
        "COMPLETED" -> R.color.geart_status_completed
        else -> R.color.geart_pencil
    }
}