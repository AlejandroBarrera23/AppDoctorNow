package com.example.appdoctornow.ui.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appdoctornow.R
import com.example.appdoctornow.data.local.database.AppDatabase
import com.example.appdoctornow.model.Cita
import kotlinx.coroutines.launch
import java.util.Calendar

class EditarCitaActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var cita: Cita
    private lateinit var tvPaciente: TextView
    private lateinit var tvEspecialidad: TextView
    private lateinit var tvMedico: TextView
    private lateinit var etFecha: EditText
    private lateinit var spinnerHoras: Spinner
    private lateinit var btnGuardarCambios: Button
    private lateinit var btnCancelarCita: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_cita)

        // Inicializar vistas
        tvPaciente = findViewById(R.id.tvPaciente)
        tvEspecialidad = findViewById(R.id.tvEspecialidad)
        tvMedico = findViewById(R.id.tvMedico)
        etFecha = findViewById(R.id.etFecha)
        spinnerHoras = findViewById(R.id.spinnerHoras)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)
        btnCancelarCita = findViewById(R.id.btnCancelarCita)

        // Inicializar la base de datos
        database = AppDatabase.getDatabase(this)

        // Obtener la cita a editar
        val citaId = intent.getIntExtra("cita_id", -1)
        if (citaId == -1) {
            Toast.makeText(this, "Cita no válida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            cita = database.citaDao().getCitaById(citaId) ?: run {
                Toast.makeText(this@EditarCitaActivity, "Cita no encontrada", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            // Cargar datos de la cita en los campos
            cargarDatosCita()
        }

        // Configurar listeners
        configurarListeners()
    }

    private fun cargarDatosCita() {
        // Obtener el paciente, médico y especialidad
        lifecycleScope.launch {
            val paciente = database.usuarioDao().getUsuarioById(cita.idUsuario)
            val medico = database.medicoDao().getMedicoById(cita.idMedico)

            tvPaciente.text = "${paciente?.nombres} ${paciente?.apellidos}"
            tvEspecialidad.text = medico?.especialidad
            tvMedico.text = "${medico?.nombres} ${medico?.apellidos}"
            etFecha.setText(cita.fecha)

            // Cargar horas disponibles
            cargarHorasDisponibles()
        }
    }

    private fun cargarHorasDisponibles() {
        val fecha = etFecha.text.toString()
        val medico = database.medicoDao().getMedicoById(cita.idMedico)

        if (medico != null && fecha.isNotEmpty()) {
            lifecycleScope.launch {
                val horasOcupadas = database.citaDao().getHorasOcupadas(fecha, medico.id)
                val todasLasHoras = listOf("09:00", "10:00", "11:00", "12:00") // Ejemplo
                val horasDisponibles = todasLasHoras.filter { !horasOcupadas.contains(it) }

                val adapter = ArrayAdapter(this@EditarCitaActivity, android.R.layout.simple_spinner_item, horasDisponibles)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerHoras.adapter = adapter

                // Seleccionar la hora actual de la cita
                val posicionHora = horasDisponibles.indexOf(cita.hora)
                if (posicionHora != -1) {
                    spinnerHoras.setSelection(posicionHora)
                }
            }
        }
    }

    private fun configurarListeners() {
        etFecha.setOnClickListener {
            mostrarDatePicker()
        }

        btnGuardarCambios.setOnClickListener {
            if (validarCampos()) {
                guardarCambios()
            } else {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelarCita.setOnClickListener {
            mostrarDialogoCancelarCita()
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

    private fun validarCampos(): Boolean {
        return etFecha.text.isNotEmpty() && spinnerHoras.selectedItem != null
    }

    private fun guardarCambios() {
        val nuevaFecha = etFecha.text.toString()
        val nuevaHora = spinnerHoras.selectedItem as String

        lifecycleScope.launch {
            cita.fecha = nuevaFecha
            cita.hora = nuevaHora
            database.citaDao().update(cita)
            Toast.makeText(this@EditarCitaActivity, "Cambios guardados", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun mostrarDialogoCancelarCita() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Cancelar Cita")
            .setMessage("¿Está seguro de que desea cancelar esta cita?")
            .setPositiveButton("Sí") { _, _ ->
                lifecycleScope.launch {
                    cita.estado = "Cancelada"
                    database.citaDao().update(cita)
                    Toast.makeText(this@EditarCitaActivity, "Cita cancelada", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("No", null)
            .create()
        dialog.show()
    }
}