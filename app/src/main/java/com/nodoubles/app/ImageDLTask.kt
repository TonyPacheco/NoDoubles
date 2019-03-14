package com.nodoubles.app
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.widget.ImageView
import com.nodoubles.app.R
class ImageDLTask constructor(var img: ImageView?) : AsyncTask<String, Void, Bitmap>() {

    override fun doInBackground(vararg urls: String): Bitmap? {
        var bmp: Bitmap? = null
        try {
            val ins = java.net.URL(urls[0]).openStream()
            bmp = BitmapFactory.decodeStream(ins)
            ins.close()
        } catch (e: Exception) {
            bmp = BitmapFactory.decodeResource(App.Globals.ctx().resources,
                    R.drawable.ic_sword_cross)
        }
        return bmp
    }
    override fun onPostExecute(result: Bitmap) {
        img?.setImageBitmap(result)
    }
}