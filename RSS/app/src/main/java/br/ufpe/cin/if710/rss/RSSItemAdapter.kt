package br.ufpe.cin.if710.rss

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.itemlista.view.*


//inicializa o adapter com os itens RSS
class RSSItemAdapter(private val feedItems: List<ItemRSS>,
                     private val context: Context) : Adapter<RSSItemAdapter.ViewHolder>() {

    lateinit var listener: OnItemClickListener

    override fun getItemCount(): Int {
        return feedItems.size
    }

    //exibe as informacoes de titulo e data requisitadas
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {

        // dá nome aos atributos do xml do itemlista
        val title = itemView.item_titulo
        val pubdate = itemView.item_data

    }

    //detecta que a acao de clique foi realizada no titulo da noticia
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rssItem = feedItems[position]

        // pareia o atributo no xml com seu respectivo valor para cada item no loop
        holder.let {
            it.title.text = rssItem.title
            it.title.setOnClickListener({listener.onClick(it, rssItem)})
            it.pubdate.text = rssItem.pubDate
        }
    }

    //organiza os conteudos em itens separados na interface
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.itemlista, parent, false)
        return ViewHolder(view)
    }


    interface OnItemClickListener {
        fun onClick(view: View, data: ItemRSS)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

}
