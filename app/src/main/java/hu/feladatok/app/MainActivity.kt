package hu.feladatok.app

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TasksApp()
                }
            }
        }
    }
}

private val HU = Locale("hu", "HU")

private fun priorityColor(p: Priority): Color = when (p) {
    Priority.URGENT -> Color(0xFFD32F2F)
    Priority.NORMAL -> Color(0xFF1976D2)
    Priority.LATER -> Color(0xFF388E3C)
}

private fun formatDate(epochDay: Long): String {
    val today = LocalDate.now().toEpochDay()
    val d = LocalDate.ofEpochDay(epochDay)
    val base = d.format(DateTimeFormatter.ofPattern("yyyy. MM. dd.")) +
        " (" + d.dayOfWeek.getDisplayName(TextStyle.SHORT, HU) + ")"
    return when (epochDay) {
        today -> "Ma · $base"
        today + 1 -> "Holnap · $base"
        today - 1 -> "Tegnap · $base"
        else -> base
    }
}

private fun headerFor(t: Task, mode: SortMode, today: Long): String {
    val due = t.dueDate
    return when {
        t.done -> "Kész"
        mode == SortMode.URGENCY -> t.priority.label
        due == null -> "Nincs határidő"
        due < today -> "Lejárt"
        due == today -> "Ma"
        due == today + 1 -> "Holnap"
        else -> "Később"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksApp(vm: TaskViewModel = viewModel()) {
    var editing by remember { mutableStateOf<Task?>(null) }
    var showNew by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    val today = LocalDate.now().toEpochDay()

    val visible = sortTasks(vm.tasks.filter { !vm.hideDone || !it.done }, vm.sortMode)
    val openCount = vm.tasks.count { !it.done }
    val doneCount = vm.tasks.size - openCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Feladatok", fontWeight = FontWeight.Bold)
                        Text(
                            "$openCount nyitott · $doneCount kész",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    Box {
                        TextButton(onClick = { menuOpen = true }) { Text("Menü") }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Kész feladatok törlése ($doneCount)") },
                                enabled = doneCount > 0,
                                onClick = { vm.clearDone(); menuOpen = false }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showNew = true }) { Text("+ Új feladat") }
        }
    ) { pad ->
        Column(Modifier.padding(pad)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = vm.sortMode == SortMode.DATE,
                    onClick = { vm.changeSort(SortMode.DATE) },
                    label = { Text("Dátum") }
                )
                FilterChip(
                    selected = vm.sortMode == SortMode.URGENCY,
                    onClick = { vm.changeSort(SortMode.URGENCY) },
                    label = { Text("Sürgősség") }
                )
                FilterChip(
                    selected = vm.hideDone,
                    onClick = { vm.changeHideDone(!vm.hideDone) },
                    label = { Text("Kész elrejtése") }
                )
            }

            if (visible.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Nincs megjeleníthető feladat.\nKoppints az + Új feladat gombra!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp, 4.dp, 12.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var last: String? = null
                    visible.forEach { t ->
                        val h = headerFor(t, vm.sortMode, today)
                        if (h != last) {
                            last = h
                            item(key = "h_$h") {
                                Text(
                                    h.uppercase(HU),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (h == "Lejárt") priorityColor(Priority.URGENT)
                                    else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 4.dp)
                                )
                            }
                        }
                        item(key = t.id) {
                            TaskCard(
                                t = t,
                                today = today,
                                onToggle = { vm.toggleDone(t) },
                                onClick = { editing = t }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNew || editing != null) {
        val current = editing
        TaskDialog(
            initial = current,
            onDismiss = { showNew = false; editing = null },
            onSave = { vm.upsert(it); showNew = false; editing = null },
            onDelete = if (current != null) ({ vm.delete(current); editing = null }) else null
        )
    }
}

@Composable
fun TaskCard(t: Task, today: Long, onToggle: () -> Unit, onClick: () -> Unit) {
    val due = t.dueDate
    val overdue = !t.done && due != null && due < today
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (t.done) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = t.done, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    t.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (t.done) TextDecoration.LineThrough else null,
                    color = if (t.done) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                if (t.note.isNotBlank()) {
                    Text(
                        t.note,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (due != null) {
                    Text(
                        (if (overdue) "Lejárt: " else "") + formatDate(due),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (overdue) FontWeight.Bold else FontWeight.Normal,
                        color = if (overdue) priorityColor(Priority.URGENT)
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = priorityColor(t.priority).copy(alpha = if (t.done) 0.5f else 1f)
            ) {
                Text(
                    t.priority.label,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun TaskDialog(
    initial: Task?,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit,
    onDelete: (() -> Unit)?
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var due by remember { mutableStateOf(initial?.dueDate) }
    var prio by remember { mutableStateOf(initial?.priority ?: Priority.NORMAL) }
    val ctx = LocalContext.current

    fun pickDate() {
        val cur = due
        val d = if (cur != null) LocalDate.ofEpochDay(cur) else LocalDate.now()
        DatePickerDialog(
            ctx,
            { _, y, m, day -> due = LocalDate.of(y, m + 1, day).toEpochDay() },
            d.year, d.monthValue - 1, d.dayOfMonth
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        title = { Text(if (initial == null) "Új feladat" else "Feladat szerkesztése") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Feladat") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Megjegyzés (nem kötelező)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                Text("Határidő", style = MaterialTheme.typography.labelLarge)
                OutlinedButton(onClick = { pickDate() }, modifier = Modifier.fillMaxWidth()) {
                    val cur = due
                    Text(if (cur != null) formatDate(cur) else "Dátum kiválasztása")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(
                        onClick = { due = LocalDate.now().toEpochDay() },
                        label = { Text("Ma") }
                    )
                    AssistChip(
                        onClick = { due = LocalDate.now().plusDays(1).toEpochDay() },
                        label = { Text("Holnap") }
                    )
                    AssistChip(
                        onClick = { due = LocalDate.now().plusDays(7).toEpochDay() },
                        label = { Text("+1 hét") }
                    )
                    if (due != null) {
                        AssistChip(onClick = { due = null }, label = { Text("Nincs") })
                    }
                }
                Text("Fontosság", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Priority.values().forEach { p ->
                        FilterChip(
                            selected = prio == p,
                            onClick = { prio = p },
                            label = { Text(p.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = priorityColor(p),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    onSave(
                        (initial ?: Task(title = "")).copy(
                            title = title.trim(),
                            note = note.trim(),
                            dueDate = due,
                            priority = prio
                        )
                    )
                }
            ) { Text("Mentés") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Törlés") }
                }
                TextButton(onClick = onDismiss) { Text("Mégse") }
            }
        }
    )
}
