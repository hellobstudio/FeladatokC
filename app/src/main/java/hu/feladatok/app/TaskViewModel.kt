package hu.feladatok.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class TaskViewModel(app: Application) : AndroidViewModel(app) {
    private val store = TaskStore(app)

    var tasks by mutableStateOf(store.load())
        private set
    var sortMode by mutableStateOf(store.loadSort())
        private set
    var hideDone by mutableStateOf(store.loadHideDone())
        private set

    private fun update(newList: List<Task>) {
        tasks = newList
        store.save(newList)
    }

    fun upsert(t: Task) {
        if (t.id == 0L) {
            update(tasks + t.copy(id = System.currentTimeMillis()))
        } else {
            update(tasks.map { if (it.id == t.id) t else it })
        }
    }

    fun toggleDone(t: Task) = update(tasks.map { if (it.id == t.id) it.copy(done = !it.done) else it })

    fun delete(t: Task) = update(tasks.filter { it.id != t.id })

    fun clearDone() = update(tasks.filter { !it.done })

    fun changeSort(m: SortMode) {
        sortMode = m
        store.saveSort(m)
    }

    fun changeHideDone(v: Boolean) {
        hideDone = v
        store.saveHideDone(v)
    }
}
