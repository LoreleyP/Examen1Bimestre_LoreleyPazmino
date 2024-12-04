import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/*Autor: Loreley Pazmiño*/
// Entidad Pelicula
data class Pelicula(
    val id: Int,
    var nombre: String,
    var fechaEstreno: LocalDate,
    var duracion: Double, // Duración en horas
    var genero: String,
    val actores: MutableList<Actor> = mutableListOf() // Relación Uno a Muchos
)

// Entidad Actor
data class Actor(
    val id: Int,
    var nombre: String,
    var fechaNacimiento: LocalDate,
    var esPrincipal: Boolean, // Actor principal o no
    var salario: Double // Salario del actor
)

// CRUD de Pelicula
class PeliculaService {

    private val peliculas = mutableListOf<Pelicula>()
    private val archivoPeliculas = "peliculas.txt"

    init {
        cargarPeliculasDesdeArchivo()
    }

    fun crearPelicula(pelicula: Pelicula) {
        peliculas.add(pelicula)
        guardarPeliculasEnArchivo()
    }

    fun leerPeliculas(): List<Pelicula> {
        return peliculas
    }

    fun actualizarPelicula(id: Int, peliculaActualizada: Pelicula) {
        val index = peliculas.indexOfFirst { it.id == id }
        if (index != -1) {
            peliculas[index] = peliculaActualizada
            guardarPeliculasEnArchivo()
        }
    }

    fun eliminarPelicula(id: Int) {
        peliculas.removeIf { it.id == id }
        guardarPeliculasEnArchivo()
    }

    private fun guardarPeliculasEnArchivo() {
        val file = File(archivoPeliculas)
        file.writeText("") // Limpiar el archivo

        // Cabecera de la película
        file.appendText("ID | Nombre              | Fecha de Estreno | Duración (hrs) | Género\n")
        file.appendText("---------------------------------------------------------------\n")

        // Ordenamos las películas por ID
        peliculas.sortedBy { it.id }.forEach { pelicula ->
            // Escribimos la película
            file.appendText(
                "${pelicula.id}  | ${
                    formatearString(
                        pelicula.nombre,
                        20
                    )
                } | ${
                    formatearString(
                        pelicula.fechaEstreno.toString(),
                        17
                    )
                } | ${formatearString(pelicula.duracion.toString(), 15)} | ${pelicula.genero}\n"
            )

            // Escribimos los actores, si los hay
            if (pelicula.actores.isNotEmpty()) {
                file.appendText("    Actores:\n")
                file.appendText("    ID | Nombre               | Fecha de Nacimiento | Es Principal | Salario\n")
                file.appendText("    -------------------------------------------------------------\n")
                pelicula.actores.sortedBy { it.id }.forEach { actor ->
                    file.appendText(
                        "    ${actor.id}  | ${
                            formatearString(
                                actor.nombre,
                                20
                            )
                        } | ${
                            formatearString(
                                actor.fechaNacimiento.toString(),
                                19
                            )
                        } | ${actor.esPrincipal}        | ${actor.salario}\n"
                    )
                }
            }
            file.appendText("\n")
        }
    }

    private fun cargarPeliculasDesdeArchivo() {
        val file = File(archivoPeliculas)
        if (file.exists()) {
            var peliculaActual: Pelicula? = null
            file.forEachLine { line ->
                // Ignorar cabeceras o líneas que no contienen datos
                if (line.startsWith("ID |")) return@forEachLine  // Ignoramos la cabecera de las películas
                if (line.startsWith("    ID |")) return@forEachLine  // Ignoramos la cabecera de los actores

                // Si la línea es de un actor
                if (line.startsWith("    ")) {
                    val actorData = line.trim().split(" | ")  // Usamos " | " como separador
                    val actor = Actor(
                        id = actorData[0].toInt(),
                        nombre = actorData[1],
                        fechaNacimiento = LocalDate.parse(actorData[2]),
                        esPrincipal = actorData[3].toBoolean(),
                        salario = actorData[4].toDouble()
                    )
                    peliculaActual?.actores?.add(actor)
                } else { // Línea de película
                    val peliculaData = line.split(" | ")
                    peliculaActual = Pelicula(
                        id = peliculaData[0].toInt(),
                        nombre = peliculaData[1],
                        fechaEstreno = LocalDate.parse(peliculaData[2]),
                        duracion = peliculaData[3].toDouble(),
                        genero = peliculaData[4]
                    )
                    peliculas.add(peliculaActual!!)
                }
            }
        }
    }
}

private fun formatearString(texto: String, longitud: Int): String {
    return if (texto.length >= longitud) {
        texto.substring(0, longitud) // Truncamos el texto si es largo
    } else {
        texto.padEnd(longitud) // Agregamos espacios si es corto
    }
}


// Funciones del menú
fun mostrarMenu() {
    println("====== Menú CRUD de Películas ======")
    println("1. Crear Película")
    println("2. Leer todas las Películas")
    println("3. Actualizar Película")
    println("4. Eliminar Película")
    println("5. Salir")
    println("====================================")
    print("Seleccione una opción: ")
}

fun leerInt(mensaje: String): Int {
    while (true) {
        try {
            print(mensaje)
            return readLine()?.toInt() ?: throw NumberFormatException("Entrada no válida")
        } catch (e: NumberFormatException) {
            println("Error: Debe ingresar un número entero válido.")
        }
    }
}

fun leerDouble(mensaje: String): Double {
    while (true) {
        try {
            print(mensaje)
            return readLine()?.toDouble() ?: throw NumberFormatException("Entrada no válida")
        } catch (e: NumberFormatException) {
            println("Error: Debe ingresar un número válido.")
        }
    }
}

fun leerString(mensaje: String): String {
    print(mensaje)
    return readLine()?.trim() ?: ""
}

fun leerFecha(mensaje: String): LocalDate {
    while (true) {
        try {
            print(mensaje)
            val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return LocalDate.parse(readLine(), formato)
        } catch (e: Exception) {
            println("Error: Fecha inválida. Use el formato yyyy-MM-dd.")
        }
    }
}

fun mostrarPeliculas(peliculaService: PeliculaService) {
    val peliculas = peliculaService.leerPeliculas()
    if (peliculas.isEmpty()) {
        println("No hay películas registradas.")
    } else {
        println("===== Lista de Películas =====")
        peliculas.forEach { pelicula ->
            println("ID: ${pelicula.id}")
            println("Nombre: ${pelicula.nombre}")
            println("Fecha de Estreno: ${pelicula.fechaEstreno}")
            println("Duración: ${pelicula.duracion} horas")
            println("Género: ${pelicula.genero}")
            println("Actores:")
            if (pelicula.actores.isEmpty()) {
                println("  Ningún actor registrado.")
            } else {
                pelicula.actores.forEach { actor ->
                    println("  - ID: ${actor.id}")
                    println("    Nombre: ${actor.nombre}")
                    println("    Fecha de Nacimiento: ${actor.fechaNacimiento}")
                    println("    Es principal: ${actor.esPrincipal}")
                    println("    Salario: ${actor.salario}")
                }
            }
            println("===================================")
        }
    }
}

// Función principal
fun main() {
    val peliculaService = PeliculaService()

    while (true) {
        mostrarMenu()
        when (leerInt("")) {
            1 -> {
                val id = leerInt("Ingrese el ID de la película: ")
                val nombre = leerString("Ingrese el nombre de la película: ")
                val fechaEstreno = leerFecha("Ingrese la fecha de estreno (yyyy-MM-dd): ")
                val duracion = leerDouble("Ingrese la duración de la película (horas): ")
                val genero = leerString("Ingrese el género de la película: ")

                val actores = mutableListOf<Actor>()
                while (true) {
                    val opcion = leerInt("¿Desea agregar un actor? 1. Sí 2. No: ")
                    if (opcion == 2) break
                    val actorId = leerInt("Ingrese el ID del actor: ")
                    val actorNombre = leerString("Ingrese el nombre del actor: ")
                    val actorFechaNacimiento = leerFecha("Ingrese la fecha de nacimiento del actor (yyyy-MM-dd): ")
                    val esPrincipal = leerInt("¿Es actor principal? 1. Sí 2. No: ") == 1
                    val salario = leerDouble("Ingrese el salario del actor: ")

                    actores.add(Actor(actorId, actorNombre, actorFechaNacimiento, esPrincipal, salario))
                }

                val pelicula = Pelicula(id, nombre, fechaEstreno, duracion, genero, actores)
                peliculaService.crearPelicula(pelicula)
            }

            2 -> {
                mostrarPeliculas(peliculaService)
            }

            3 -> {
                val id = leerInt("Ingrese el ID de la película a actualizar: ")
                val peliculaExistente = peliculaService.leerPeliculas().find { it.id == id }
                if (peliculaExistente != null) {
                    val nombre =
                        leerString("Ingrese el nuevo nombre de la película (actual: ${peliculaExistente.nombre}): ")
                    val fechaEstreno =
                        leerFecha("Ingrese la nueva fecha de estreno (actual: ${peliculaExistente.fechaEstreno}): ")
                    val duracion =
                        leerDouble("Ingrese la nueva duración (actual: ${peliculaExistente.duracion} horas): ")
                    val genero = leerString("Ingrese el nuevo género (actual: ${peliculaExistente.genero}): ")

                    val actores = mutableListOf<Actor>()
                    peliculaExistente.actores.forEach { actor ->
                        actores.add(
                            Actor(
                                actor.id,
                                actor.nombre,
                                actor.fechaNacimiento,
                                actor.esPrincipal,
                                actor.salario
                            )
                        )
                    }

                    val peliculaActualizada = Pelicula(id, nombre, fechaEstreno, duracion, genero, actores)
                    peliculaService.actualizarPelicula(id, peliculaActualizada)
                } else {
                    println("Película no encontrada.")
                }
            }

            4 -> {
                val id = leerInt("Ingrese el ID de la película a eliminar: ")
                peliculaService.eliminarPelicula(id)
            }

            5 -> {
                println("Saliendo...")
                break
            }

            else -> println("Opción inválida.")
        }
    }
}