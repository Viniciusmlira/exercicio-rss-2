package br.ufpe.cin.if710.rss

import android.app.IntentService
import android.content.Intent
import android.preference.PreferenceManager
import br.ufpe.cin.if710.rss.db.SQLiteRSSHelper
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

class RSSService : IntentService(RSSService::class.simpleName) {

    //busca a referencia definida nas preferencias
    override fun onHandleIntent(p0: Intent?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val rssfeed = prefs.getString("rssfeed", "<unset>")

        if (rssfeed =="<unset>") return

        val feedXML = getRssFeed(rssfeed)
        val feedRSS = ParserRSS.parse(feedXML)
        val hasNewEntries = updateDB(feedRSS)

        val intent = Intent()
        intent.action = "updateFeed"
        intent.putExtra("HasNewEntries", hasNewEntries)
        sendBroadcast(intent)
    }

    @Throws(IOException::class)
    private fun getRssFeed(feed:String):String {
        var input: InputStream? = null
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

    //se encontrar conteudo, atualiza o banco
    fun updateDB(feedRSS:List<ItemRSS>): Boolean {

        var hasNewEntries = false

        val dbHandler = SQLiteRSSHelper(this)

        for (item in feedRSS) {
            val hasInserted = dbHandler.insertItem(item)
            hasNewEntries = hasNewEntries || (hasInserted == 1.0)
        }

        return hasNewEntries
    }

}
