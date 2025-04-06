package k4ustu3h.dedupe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import k4ustu3h.dedupe.R
import java.io.File

class DuplicateFileAdapter(private val duplicateFiles: List<Pair<File, File>>) :
    RecyclerView.Adapter<DuplicateFileAdapter.DuplicateFileViewHolder>() {

    private val selectedFiles = mutableSetOf<File>()

    class DuplicateFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val file1TextView: TextView = itemView.findViewById(R.id.file1TextView)
        val file2TextView: TextView = itemView.findViewById(R.id.file2TextView)
        val checkBox1: CheckBox = itemView.findViewById(R.id.checkBox1)
        val checkBox2: CheckBox = itemView.findViewById(R.id.checkBox2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DuplicateFileViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.duplicate_item, parent, false)
        return DuplicateFileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DuplicateFileViewHolder, position: Int) {
        val pair = duplicateFiles[position]
        holder.file1TextView.text = pair.first.absolutePath
        holder.file2TextView.text = pair.second.absolutePath

        holder.checkBox1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedFiles.add(pair.first) else selectedFiles.remove(pair.first)
        }
        holder.checkBox2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedFiles.add(pair.second) else selectedFiles.remove(pair.second)
        }

        holder.checkBox1.isChecked = selectedFiles.contains(pair.first)
        holder.checkBox2.isChecked = selectedFiles.contains(pair.second)
    }

    override fun getItemCount(): Int {
        return duplicateFiles.size
    }

    fun getSelectedFiles(): Set<File> {
        return selectedFiles
    }
}