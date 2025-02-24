package com.example.appdoctornow.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.appdoctornow.model.Medico

class MedicosAdapter(context: Context, private val medicos: List<Medico>) :
    ArrayAdapter<Medico>(context, android.R.layout.simple_spinner_item, medicos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        view.text = "${medicos[position].nombres} ${medicos[position].apellidos} - ${medicos[position].especialidad}"
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        view.text = "${medicos[position].nombres} ${medicos[position].apellidos} - ${medicos[position].especialidad}"
        return view
    }
}