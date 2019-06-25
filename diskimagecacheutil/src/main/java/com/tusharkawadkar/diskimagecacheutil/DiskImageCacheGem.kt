package com.tusharkawadkar.diskimagecacheutil

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import android.util.LruCache
import android.widget.ImageView
import android.content.ContentValues
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream


class DiskImageCacheGem{


    private fun addBitmapToCache(key: String, bitmap: Bitmap) {
        bitmapCache!!.put(key,bitmap)
    }

    private fun getBitmapFromDiskCache(key: String): Bitmap?{
        return bitmapCache!!.get(key)
    }




    //------PUBLIC METHODS

    fun preFetchImages(context: Context){
        Log.e("Cashe","Prefetch")
        val list = CacheDBHelper(context).getImageBytesList()
        Log.e("Cashe","Prefetch list size  "+ list.size)
        if(list.size != 0 ){
            for(index in 0..(list.size-1)){
                Log.e("Cashe","Prefetch OBJ "+ index)
                val dataObj = list[index]
                DecodeImageTask(dataObj.key, dataObj.byteString).execute()
            }
        }
    }

    fun setImage(context: Context, url:String, iv:ImageView){
        syncDBWithLRU(context)
        val bitmap:Bitmap? = getBitmapFromDiskCache(url)
        Log.e("Cashe","BITMAP---"+bitmap)
        if(bitmap == null){
            Log.e("Cashe","----LOAD")
            LoadImageTask(context,url,iv).execute()
        }else{
            Log.e("Cashe","----IMAGE FETCHED From CACHE")
            iv.setImageBitmap(bitmap)
        }
    }

    fun setMaxImageLimit(limit:Int){
        maxImageLimit = limit
    }

    fun setDiskCacheSize(size:Int){
        DISK_CACHE_SIZE = size
    }

    //---------------------------

    private fun syncDBWithLRU(context: Context){
        if(currentLengthOfDB > maxImageLimit){
            val dbHelper = CacheDBHelper(context)
            val db = dbHelper.writableDatabase
            dbHelper.recreateDB(db)
            bitmapCache!!.evictAll()
        }
    }


    inner class LoadImageTask(val context: Context,val url:String,val iv:ImageView):AsyncTask<Void,Void,Bitmap>(){
        override fun doInBackground(vararg p0: Void?): Bitmap? {
            /*
            val bitmap = Picasso.with(context)
                    .load(url)
                    .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                    .get()
                    */

            var bitmap: Bitmap? = null
            try {
                val inputStream = java.net.URL(url).openStream()
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                Log.e("Error", e.message)
                e.printStackTrace()
            }

            return bitmap
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            if(bitmap == null){
                return
            }

            iv.setImageBitmap(bitmap)
            addBitmapToCache(url,bitmap!!)
            // add image to DB
            EncodeImageTask(context,url,bitmap).execute()
            Log.e("Cashe","----Added to Cache,DB and loaded into IV")
        }
    }


    inner class DecodeImageTask( val key:String, val byteImage:String):AsyncTask<Void,Void,Bitmap>(){
        override fun doInBackground(vararg p0: Void?): Bitmap? {
            val imageBytes = Base64.decode(byteImage, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            return decodedImage
        }

        override fun onPostExecute(result: Bitmap?) {
            addBitmapToCache(key,result!!)
            Log.e("Cashe  ","Bitmaps loaded to cashe "+key)
        }
    }

    inner class EncodeImageTask(val context: Context,val key:String, val bitmap:Bitmap):AsyncTask<Void,Void,String>(){

        override fun doInBackground(vararg p0: Void?): String {

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageBytes = baos.toByteArray()
            val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            return imageString
        }

        override fun onPostExecute(result: String?) {
            CacheDBHelper(context).addCache(key,result!!)
            Log.e("Cashe  ","ADDED TO DB_-----------")
        }

    }

    inner class CacheDBHelper: SQLiteOpenHelper {

        private val COLUMN_KEY = "COLUMN_KEY"
        private val COLUMN_BYTEIMAGE = "COLUMN_BYTEIMAGE"

        constructor(context: Context) : super(context, DB_NAME,null,2)

        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(
                    "CREATE TABLE CACHE " +
                            "(id integer primary key AUTOINCREMENT,"+ COLUMN_KEY+" text,"+ COLUMN_BYTEIMAGE +" text)")

        }

        override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
            recreateDB(db!!)
        }

        fun recreateDB(db: SQLiteDatabase?){
            db!!.execSQL("DROP TABLE IF EXISTS CACHE")
            onCreate(db)
        }

        fun getImageBytesList(): ArrayList<CacheDbColumn> {
            val array_list = ArrayList<CacheDbColumn> ()

            //hp = new HashMap();
            val db = this.readableDatabase
            val res = db.rawQuery("select * from CACHE", null)
            res.moveToFirst()

            if (res.moveToFirst()) {
                do {
                    array_list.add(CacheDbColumn(res.getString(res.getColumnIndex(COLUMN_KEY)),
                            res.getString(res.getColumnIndex(COLUMN_BYTEIMAGE))))
                } while (res.moveToNext())
            }
            currentLengthOfDB = array_list.size

            return array_list
        }

        fun addCache(key:String, value:String) {
            val db = this.writableDatabase
            val contentValues = ContentValues()
            contentValues.put(COLUMN_KEY, key)
            contentValues.put(COLUMN_BYTEIMAGE, value)
            db.insert("CACHE", null, contentValues)
        }

    }

    data class CacheDbColumn(var key:String,
                             var byteString:String)

    companion object {
        private var mInstance: DiskImageCacheGem? = null
        private var DISK_CACHE_SIZE = 1024 * 1024 * 5 // 5MB
        private var bitmapCache: LruCache<String, Bitmap> ? = null
        private var maxImageLimit = 10
        private val DB_NAME = "MyCacheDB.db"

        private var currentLengthOfDB = 0

        fun getInstance(): DiskImageCacheGem {
            if(mInstance == null){
                mInstance = DiskImageCacheGem()
                bitmapCache = LruCache<String, Bitmap>(DISK_CACHE_SIZE)
                return mInstance!!
            }
            return mInstance!!
        }
    }

}