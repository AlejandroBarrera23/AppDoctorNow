package com.example.appdoctornow.ui.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appdoctornow.R
import com.example.appdoctornow.data.local.database.AppDatabase
import com.example.appdoctornow.model.Cita
import com.example.appdoctornow.model.Medico
import com.example.appdoctornow.model.Usuario
import kotlinx.coroutines.launch
import java.util.Calendar

class AgendarCitaAdminActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var spinnerPacientes: Spinner
    private lateinit var spinnerEspecialidades: Spinner
    private lateinit var spinnerMedicos: Spinner
    private lateinit var etFecha: EditText
    private lateinit var spinnerHoras: Spinner
    private lateinit var btnAgendarCita: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendar_cita_admin)

        // Inicializar vistas
        spinnerPacientes = findViewById(R.id.spinnerPacientes)
        spinnerEspecialidades = findViewById(R.id.spinnerEspecialidades)
        spinnerMedicos = findViewById(R.id.spinnerMedicos)
        etFecha = findViewById(R.id.etFecha)
        spinnerHoras = findViewById(R.id.spinnerHoras)
        btnAgendarCita = findViewById(R.id.btnAgendarCita)

        // Inicializar la base de datos
        database = AppDatabase.getDatabase(this)

        // Cargar datos iniciales
        cargarPacientes()
        cargarEspecialidades()

        // Configurar listeners
        configurarListeners()
    }

    private fun cargarPacientes() {
        lifecycleScope.launch {
            val pacientes = database.usuarioDao().getAllPacientes() // Asume que tienes un metodo para obtener pacientes
            val adapter = ArrayAdapter(this@AgendarCitaAdminActivity, android.R.layout.simple_spinner_item, pacientes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPacientes.adapter = adapter
        }
    }

    private fun cargarEspecialidades() {
        val especialidades = listOf("Cardiología", "Dermatología", "Pediatría") // Ejemplo
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, especialidades)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEspecialidades.adapter = adapter
    }

    private fun configurarListeners() {
        spinnerEspecialidades.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val especialidad = parent?.getItemAtPosition(position) as String
                cargarMedicos(especialidad)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        etFecha.setOnClickListener {
            mostrarDatePicker()
        }

        btnAgendarCita.setOnClickListener {
            if (validarCampos()) {
                agendarCita()
            } else {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarMedicos(especialidad: String) {
        lifecycleScope.launch {
            val medicos = database.medicoDao().getMedicosPorEspecialidad(especialidad)
            val adapter = ArrayAdapter(this@AgendarCitaAdminActivity, android.R.layout.simple_spinner_item, medicos)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMedicos.adapter = adapter
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                val fechaSeleccionada = "$day/${month + 1}/$year"
                etFecha.setText(fechaSeleccionada)
                cargarHorasDisponibles()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun cargarHorasDisponibles() {
        val fecha = etFecha.text.toString()
        val medico = spinnerMedicos.selectedItem as? Medico

        if (medico != null && fecha.isNotEmpty()) {
            lifecycleScope.launch {
                val horasOcupadas = database.citaDao().getHorasOcupadas(fecha, medico.id)
                val todasLasHoras = listOf("09:00", "10:00", "11:00", "12:00") // Ejemplo
                val horasDisponibles = todasLasHoras.filter { !horasOcupadas.contains(it) }
                val adapter = ArrayAdapter(this@AgendarCitaAdminActivity, android.R.layout.simple_spinner_item, horasDisponibles)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerHoras.adapter = adapter
            }
        }
    }

    private fun validarCampos(): Boolean {
        return spinnerPacientes.selectedItem != null &&
                spinnerMedicos.selectedItem != null &&
                etFecha.text.isNotEmpty() &&
                spinnerHoras.selectedItem != null
    }

    private fun agendarCita() {
        val paciente = spinnerPacientes.selectedItem as Usuario
        val medico = spinnerMedicos.selectedItem as Medico
        val fecha = etFecha.text.toString()
        val hora = spinnerHoras.selectedItem as String

        val cita = Cita(
            idUsuario = paciente.id,
            idMedico = medico.id,
            fecha = fecha,
            hora = hora,
            estado = "Confirmada"
        )

        lifecycleScope.launch {
            database.citaDao().insert(cita)
            Toast.makeText(this@AgendarCitaAdminActivity, "Cita agendada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}