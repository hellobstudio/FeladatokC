package hu.feladatok.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Egyszerű helyi tárolás (SharedPreferences + JSON). */
class TaskStore(context: Context) {
    private val p = context.getSharedPreferences("feladatok", Context.MODE_PRIVATE)

    fun load(): List<Task> {
        val raw = p.getString("tasks", null) ?: return emptyList()
        return try {
            val a = JSONArray(raw)
            (0 until a.length()).map { i ->
                val o = a.getJSONObject(i)
                Task(
                    id = o.getLong("id"),
                    title = o.getString("title"),
                    note = o.optString("note", ""),
                    dueDate = if (o.has("due")) o.getLong("due") else null,
                    priority = runCatching { Priority.valueOf(o.getString("priority")) }
                        .getOrDefault(Priority.NORMAL),
                    done = o.optBoolean("done", false)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(list: List<Task>) {
        val a = JSONArray()
        list.forEach { t ->
            val o = JSONObject()
            o.put("id", t.id)
            o.put("title", t.title)
            o.put("note", t.note)
            t.dueDate?.let { o.put("due", it) }
            o.put("priority", t.priority.name)
            o.put("done", t.done)
            a.put(o)
        }
        p.edit().putString("tasks", a.toString()).apply()
    }

    fun loadSort(): SortMode =
        runCatching { SortMode.valueOf(p.getString("sort", "DATE")!!) }.getOrDefault(SortMode.DATE)

    fun saveSort(m: SortMode) = p.edit().putString("sort", m.name).apply()

    fun loadHideDone(): Boolean = p.getBoolean("hideDone", false)

    fun saveHideDone(v: Boolean) = p.edit().putBoolean("hideDone", v).apply()
}
