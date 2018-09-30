package br.ufpe.cin.if710.rss

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import java.util.*
import android.content.ComponentName
import android.app.ActivityManager.RunningTaskInfo
import android.app.ActivityManager

class RSSReceiver : BroadcastReceiver() {

    //faz broadcast pra novas entradas e cria a notificacao
    override fun onReceive(context: Context, intent: Intent) {

        val hasNewEntries:Boolean = intent.getExtras().getBoolean("HasNewEntries")

        if (hasNewEntries && !isAppForeground(context)) {
            var mBuilder = NotificationCompat.Builder(context, "notification_item")
                    .setSmallIcon(R.mipmap.notification_icon)
                    .setContentTitle("My RSS Feed")
                    .setContentText("Você tem notícias novas, confira!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            var notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            //cria um id novo pra cada notificacao
            var random = Random()
            var id: Int = random.nextInt(1000 + 1)

            notificationManager.notify(id, mBuilder.build())
        }
    }

    //checa se o app esta em segundo plano
    fun isAppForeground(mContext: Context): Boolean {

        val am = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = am.getRunningTasks(1)
        if (!tasks.isEmpty()) {
            val topActivity = tasks[0].topActivity
            if (topActivity.packageName != mContext.packageName) {
                return false
            }
        }
        return true
    }
}