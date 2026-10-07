package hu.feladatok.app

enum class Priority(val label: String, val rank: Int) {
    URGENT("Sürgős", 0),
    NORMAL("Normál", 1),
    LATER("Ráér", 2)
}

enum class SortMode { DATE, URGENCY }

/** dueDate = LocalDate.toEpochDay(), null ha nincs határidő. id = 0 az új feladatnál. */
data class Task(
    val id: Long = 0,
    val title: String,
    val note: String = "",
    val dueDate: Long? = null,
    val priority: Priority = Priority.NORMAL,
    val done: Boolean = false
)

/** A kész feladatok mindig a lista végére kerülnek. */
fun sortTasks(list: List<Task>, mode: SortMode): List<Task> {
    val cmp = when (mode) {
        SortMode.DATE -> compareBy<Task>(
            { it.done }, { it.dueDate ?: Long.MAX_VALUE }, { it.priority.rank }, { it.id }
        )
        SortMode.URGENCY -> compareBy<Task>(
            { it.done }, { it.priority.rank }, { it.dueDate ?: Long.MAX_VALUE }, { it.id }
        )
    }
    return list.sortedWith(cmp)
}
