package pl.mareklangiewicz.myintent


import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import kotlinx.android.synthetic.main.mi_re_rule_layout.view.*
import kotlinx.android.synthetic.main.mi_re_rule_ro_details.view.*
import kotlinx.android.synthetic.main.mi_re_rule_rw_details.view.*
import pl.mareklangiewicz.myloggers.MY_DEFAULT_ANDRO_LOGGER
import pl.mareklangiewicz.myutils.*

/**
 * Created by Marek Langiewicz on 15.10.15.
 */
class RERulesAdapter() : RecyclerView.Adapter<RERulesAdapter.ViewHolder>(), View.OnClickListener {

    val RE_RULE_VIEW_TAG_HOLDER = R.id.mi_re_rule_view_tag_holder

    val log = MY_DEFAULT_ANDRO_LOGGER

    private var explained: RERule? = null
    // if some rule can not be removed or moved it displays snackbar only once in a row.
    // we remember this rule here so we do not display an error for it more than once in a row.

    init {
        setHasStableIds(false)
    }

    constructor(rules: MutableList<RERule>) : this() {
        this.rules = rules
    }

    var rules: MutableList<RERule>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val v = parent.inflate<View>(R.layout.mi_re_rule_layout)!!
        v.setOnClickListener(this)
        val holder = ViewHolder(v)
        v.setTag(RE_RULE_VIEW_TAG_HOLDER, holder)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (rules === null) {
            log.e("Rules not set.")
        }
        else {
            val rule = rules!![position]
            holder.itemView.rule_name_view.text = "rule: ${rule.name}"
            holder.itemView.rule_content_view.text = "match: \"${rule.match}\"\nreplace: \"${rule.replace}\""
        }
    }

    override fun getItemCount(): Int = rules?.size ?: 0

    fun move(pos1: Int, pos2: Int): Boolean {
        val rs = rules
        if (rs === null) {
            log.e("Rules not set.")
            return false
        }
        try {
            val rule = rs.removeAt(pos1)
            rs.add(pos2, rule)
            notifyItemMoved(pos1, pos2)
        } catch (e: UnsupportedOperationException) {
            if (rs[pos1] != explained && rs[pos2] != explained)
                log.i("[SNACK]This group is not editable.")
            explained = rs[pos1]
            return false
        }
        return true
    }

    fun remove(pos: Int) {
        val rs = rules
        if (rs == null) {
            log.e("Rules not set.")
            return
        }
        try {
            rs.removeAt(pos)
            notifyItemRemoved(pos)
        } catch (e: UnsupportedOperationException) {
            if (rs[pos] != explained)
                log.i("[SNACK]This group is not editable.")
            explained = rs[pos]
            notifyDataSetChanged() // so it redraws swiped rule at original position
        }

    }

    override fun onClick(view: View) {

        val tag = view.getTag(RE_RULE_VIEW_TAG_HOLDER) ?: return

        val rs = rules ?: return

        val pos = (tag as ViewHolder).adapterPosition

        val rule = rs[pos]


        if(rule.editable) {

            val dialogContentView = inflate(view.context, R.layout.mi_re_rule_rw_details, null).apply {
                re_rule_rw_name.setText(rule.name)
                re_rule_rw_description.setText(rule.description)
                re_rule_rw_match.setText(rule.match)
                re_rule_rw_replace.setText(rule.replace)
            }

            MaterialDialog(view.context)
                .title(text = "RE Rule ${(pos + 1).str}")
                .customView(view = dialogContentView, scrollable = true)
                .positiveButton(text = "Apply") {dialog ->
                    rule.name = dialogContentView.re_rule_rw_name.text.toString()
                    rule.description = dialogContentView.re_rule_rw_description.text.toString()
                    rule.match = dialogContentView.re_rule_rw_match.text.toString()
                    rule.replace = dialogContentView.re_rule_rw_replace.text.toString()
                    notifyItemChanged(pos)
                }
                .negativeButton(text = "Cancel")
                .icon(R.mipmap.mi_ic_launcher)
                .show()
        }
        else {
            val dialogContentView = inflate(view.context, R.layout.mi_re_rule_ro_details, null).apply {
                re_rule_ro_name.text = rule.name
                re_rule_ro_description.text = rule.description
                re_rule_ro_match.text = rule.match
                re_rule_ro_replace.text = rule.replace
            }

            MaterialDialog(view.context)
                .title(text = "RE Rule ${(pos + 1).str}")
                .customView(view = dialogContentView, scrollable = true)
                .icon(R.mipmap.mi_ic_launcher)
                .show()
        }
    }


    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) { }

}
