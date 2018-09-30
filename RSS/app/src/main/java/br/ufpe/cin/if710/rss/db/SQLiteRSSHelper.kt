package br.ufpe.cin.if710.rss.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import br.ufpe.cin.if710.rss.ItemRSS
class SQLiteRSSHelper constructor(context:Context):SQLiteOpenHelper(context, DATABASE_NAME, null, DB_VERSION) {
    //alternativa
    internal var c:Context
    val items: Cursor?
        @Throws(SQLException::class)
        //retorna todos os itens nao lidos
        get() {
            val query =
                    "SELECT * FROM " + RssProviderContract.ITEMS_TABLE + " WHERE " + RssProviderContract.UNREAD + " =  \"true\""

            val db = this.writableDatabase

            val cursor = db.rawQuery(query, null)

            return cursor
        }
    init{
        c = context
    }
    override fun onCreate(db:SQLiteDatabase) {
        //Executa o comando de criação de tabela
        db.execSQL(CREATE_DB_COMMAND)
    }
    override fun onUpgrade(db:SQLiteDatabase, oldVersion:Int, newVersion:Int) {
        //estamos ignorando esta possibilidade no momento
        throw RuntimeException("nao se aplica")
    }
    //IMPLEMENTAR ABAIXO
    //Implemente a manipulação de dados nos métodos auxiliares para não ficar criando consultas manualmente
    fun insertItem(item:ItemRSS): Double {
        return insertItem(item.title, item.pubDate, item.description, item.link)
    }

    //checa se existe algum item com o mesmo link. classifica como unread for um item novo
    fun insertItem(title:String, pubDate:String, description:String, link:String): Double {

        val alreadyExists = getItemRSS(link) != null

        if (alreadyExists) return 0.0

        val values = ContentValues()

        values.put(RssProviderContract.TITLE, title)
        values.put(RssProviderContract.DATE, pubDate)
        values.put(RssProviderContract.DESCRIPTION, description)
        values.put(RssProviderContract.LINK, link)
        values.put(RssProviderContract.UNREAD, 1)

        val db = this.writableDatabase

        db.insert(RssProviderContract.ITEMS_TABLE, null, values)
        db.close()

        return 1.0
    }

    @Throws(SQLException::class)
    //busca todos os itens
    fun getItemRSS(link:String):ItemRSS? {

        val query =
                "SELECT * FROM " + RssProviderContract.ITEMS_TABLE + " WHERE " + RssProviderContract.LINK + " =  \"$link\""

        val db = this.writableDatabase

        val cursor = db.rawQuery(query, null)

        var item: ItemRSS? = null

        if (cursor.moveToFirst()) {
            cursor.moveToFirst()

            val title = cursor.getString(cursor.getColumnIndex(RssProviderContract.TITLE))
            val pubDate = cursor.getString(cursor.getColumnIndex(RssProviderContract.DATE))
            val description = cursor.getString(cursor.getColumnIndex(RssProviderContract.DESCRIPTION))
            val link = cursor.getString(cursor.getColumnIndex(RssProviderContract.LINK))
            item = ItemRSS(title, link, pubDate, description)
            cursor.close()
        }

        db.close()

        return item
    }

    //mostra um item na lista, mostrando-o como nao lido
    fun markAsUnread(link:String):Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(RssProviderContract.UNREAD, 1)

        val _success = db.update(RssProviderContract.ITEMS_TABLE, values, (RssProviderContract.LINK + "=?"), arrayOf(link)).toLong()

        db.close()

        return Integer.parseInt("$_success") != -1
    }

    //retira um item da lista, mostrando-o como lido
    fun markAsRead(link:String):Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(RssProviderContract.UNREAD, 0)

        val _success = db.update(RssProviderContract.ITEMS_TABLE, values, (RssProviderContract.LINK + "=?"), arrayOf(link)).toLong()

        db.close()

        return Integer.parseInt("$_success") != -1
    }

    //retorna todos os itens nao lidos
    fun getItems():List<ItemRSS> {
        val items = ArrayList<ItemRSS>()

        val query =
                "SELECT * FROM " + RssProviderContract.ITEMS_TABLE + " WHERE " + RssProviderContract.UNREAD + " = 1"

        val db = this.writableDatabase

        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(query, null)
        } catch (e:SQLiteException){
            return ArrayList()
        }

        var title:String
        var pubDate:String
        var description:String
        var link:String

        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                title = cursor.getString(cursor.getColumnIndex(RssProviderContract.TITLE))
                pubDate = cursor.getString(cursor.getColumnIndex(RssProviderContract.DATE))
                description = cursor.getString(cursor.getColumnIndex(RssProviderContract.DESCRIPTION))
                link = cursor.getString(cursor.getColumnIndex(RssProviderContract.LINK))
                items.add(ItemRSS(title, link, pubDate, description))

                cursor.moveToNext()
            }
        }

        cursor.close()

        return items
    }
    companion object {
        //Nome do Banco de Dados
        private val DATABASE_NAME = "rss"
        //Nome da tabela do Banco a ser usada
        val DATABASE_TABLE = "items"
        //Versão atual do banco
        private val DB_VERSION = 1
        private lateinit var db:SQLiteRSSHelper
        //Definindo Singleton
        fun getInstance(c:Context):SQLiteRSSHelper {
            if (db == null)
            {
                db = SQLiteRSSHelper(c.getApplicationContext())
            }
            return db
        }
        //Definindo constantes que representam os campos do banco de dados
        val ITEM_ROWID = RssProviderContract._ID
        val ITEM_TITLE = RssProviderContract.TITLE
        val ITEM_DATE = RssProviderContract.DATE
        val ITEM_DESC = RssProviderContract.DESCRIPTION
        val ITEM_LINK = RssProviderContract.LINK
        val ITEM_UNREAD = RssProviderContract.UNREAD
        //Definindo constante que representa um array com todos os campos
        val columns = arrayOf<String>(ITEM_ROWID, ITEM_TITLE, ITEM_DATE, ITEM_DESC, ITEM_LINK, ITEM_UNREAD)
        //Definindo constante que representa o comando de criação da tabela no banco de dados
        private val CREATE_DB_COMMAND = ("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + " (" +
                ITEM_ROWID + " integer primary key autoincrement, " +
                ITEM_TITLE + " text not null, " +
                ITEM_DATE + " text not null, " +
                ITEM_DESC + " text not null, " +
                ITEM_LINK + " text not null, " +
                ITEM_UNREAD + " boolean not null);")
    }
}