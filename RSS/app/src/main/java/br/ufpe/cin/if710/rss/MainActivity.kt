package br.ufpe.cin.if710.rss

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.android.synthetic.main.activity_main.*

import java.nio.charset.Charset

class MainActivity : Activity() {

    private lateinit var RSS_FEED: String
    private lateinit var linearLayoutManager: LinearLayoutManager
    var feedRSS:List<ItemRSS> = emptyList()

    protected override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        RSS_FEED = getString(R.string.rssfeed)
        setContentView(R.layout.activity_main)
    }

    protected override fun onStart() {
        super.onStart()

        //uso de doasync com anko
        doAsync {

            val feedXML = getRssFeed(RSS_FEED)
            feedRSS = ParserRSS.parse(feedXML)

            //atualiza a interface do user apos carregar os dados necessarios
            uiThread {
                try
                {
                    updateView()
                }
                catch (e:IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    //chama o conteudo carregado do xml getRSSFeed e organiza na interface
    private fun updateView() {

        // seta o adapter/os dados formatados da lista
        val recyclerView = conteudoRSS
        var adapter = RSSItemAdapter(feedRSS, this)
        recyclerView.adapter = adapter

        // seta o formato da lista
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        // define o listener para o clique no título do item do rss
        adapter.setOnItemClickListener(object : RSSItemAdapter.OnItemClickListener {
            override fun onClick(view: View, data: ItemRSS) {
                loadWebpage(data.link)
            }
        })
    }

    //carrega o link numa webview
    fun loadWebpage(url: String) {
        webview.loadUrl("")
        try {
            webview.loadUrl(url)
        } catch(e: UnsupportedOperationException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun getRssFeed(feed:String):String {
        var input:InputStream? = null
        var rssFeed = ""

        //requisita conteudo do link
        try
        {
            val url = URL(feed)
            val conn = url.openConnection() as HttpURLConnection
            input = conn.getInputStream()
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var count:Int = input.read(buffer)
            while (count != -1) {
                out.write(buffer, 0, count)
                count = input.read(buffer)
            }

            val response = out.toByteArray()

            val charset: Charset = Charsets.UTF_8

            rssFeed = String(response, charset)
        }

        finally {
            if (input != null)
            {
                input.close()
            }
        }
        return rssFeed
    }
}