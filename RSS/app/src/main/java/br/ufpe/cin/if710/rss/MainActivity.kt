package br.ufpe.cin.if710.rss

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import br.ufpe.cin.if710.rss.db.SQLiteRSSHelper
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.android.synthetic.main.activity_main.*

import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var RSS_FEED: String
    private lateinit var linearLayoutManager: LinearLayoutManager
    var feedRSS:List<ItemRSS> = emptyList()

    protected override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        RSS_FEED = getString(R.string.rssfeed)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        // Now get the support action bar
        val actionBar = supportActionBar

        // Set toolbar title/app title
        actionBar!!.title = "Hello APP"

        // Set action bar/toolbar sub title
        actionBar.subtitle = "App subtitle"

        // Set action bar elevation
        actionBar.elevation = 4.0F

        // Display the app icon in action bar/toolbar
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setLogo(R.mipmap.ic_launcher)
        actionBar.setDisplayUseLogoEnabled(true)


        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.getString("rssfeed", "<unset>")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun readRSSFEED() {
        val dbHandler = SQLiteRSSHelper(this)

    }

    fun updateRSSFEED() {
        val dbHandler = SQLiteRSSHelper(this)
    }

    fun markAsRead(link: String) {
        val dbHandler = SQLiteRSSHelper(this)

        dbHandler.markAsRead(link)
    }

    fun markAsUnread(link: String) {
        val dbHandler = SQLiteRSSHelper(this)

        dbHandler.markAsUnread(link)
    }

    fun getUnreadItems():List<ItemRSS> {
        val dbHandler = SQLiteRSSHelper(this)

        return dbHandler.getItems()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_config -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected override fun onStart() {
        super.onStart()

        //uso de doasync com anko
        doAsync {

            //val feedXML = getRssFeed(RSS_FEED)
            //feedRSS = ParserRSS.parse(feedXML)

            feedRSS = getUnreadItems()

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
                markAsRead(data.link)

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