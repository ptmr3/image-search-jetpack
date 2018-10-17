package com.jneuberger.imagesearchjetpack.data.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import com.jneuberger.imagesearchjetpack.data.Image
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class SearchImagesRequest(private val mAsyncResultListener: AsyncResult) : AsyncTask<String, ArrayList<Image>, ArrayList<Image>?>() {
    private val mImageList = ArrayList<Image>()

    override fun doInBackground(vararg params: String): ArrayList<Image>? {
        val imageSearchUrl = Uri.parse(PHOTO_SEARCH_URL).buildUpon()
                .appendQueryParameter(CLIENT_ID, TOKEN)
                .appendQueryParameter(QUERY, params[0])
                .appendQueryParameter(PER_PAGE, ITEMS_PER_PAGE)
        try {
            val jsonResponse = request(URL(imageSearchUrl.build().toString()))
            val pages = JSONObject(jsonResponse).getString(TOTAL_PAGES).toInt()
            if (pages > 1) {
                for (page in 0 until pages) {
                    if (isCancelled) { break }
                    buildImageList(request(URL(imageSearchUrl.appendQueryParameter(PAGE, page.toString()).build().toString())))
                }
            } else {
                buildImageList(jsonResponse)
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            Handler(Looper.getMainLooper()).post { mAsyncResultListener.onError(ioException) }
        }
        return mImageList
    }

    override fun onProgressUpdate(vararg values: ArrayList<Image>) {
        super.onProgressUpdate(*values)
        mAsyncResultListener.onProgressUpdate(values[0])
    }

    override fun onPostExecute(result: ArrayList<Image>?) {
        super.onPostExecute(result)
        mAsyncResultListener.onProcessComplete(result)
    }

    private fun request(url: URL): String {
        val connection = url.openConnection() as HttpURLConnection
        connection.apply { requestMethod = GET }.connect()
        val inputStreamReader = InputStreamReader(connection.inputStream)
        val reader = BufferedReader(inputStreamReader)
        val stringBuilder = StringBuilder()
        var inputLine = reader.readLine()
        while ((inputLine) != null) {
            stringBuilder.append(inputLine)
            inputLine = reader.readLine()
        }
        reader.close()
        inputStreamReader.close()
        return stringBuilder.toString()
    }

    private fun buildImageList(jsonResponse: String) {
        var description: String? = null
        var small: String? = null
        var smallImage: Bitmap? = null
        var userFullName: String? = null
        var downloadLink: String? = null
        val results = JSONObject(jsonResponse).getJSONArray(RESULTS)
        for (imageItem in 0 until results.length()) {
            results.getJSONObject(imageItem).apply {
                description = getString(DESCRIPTION)
                getJSONObject(URLS).apply { small = getString(SMALL_IMAGE) }
                smallImage = BitmapFactory.decodeStream(URL(Uri.parse(small).buildUpon()
                        .appendQueryParameter(CLIENT_ID, TOKEN)
                        .build().toString()).openConnection().getInputStream())
                getJSONObject(LINKS).apply { downloadLink = getString(DOWNLOAD) }
                getJSONObject(USER).apply { userFullName = getString(NAME) }
            }
            mImageList.add(Image(smallImage!!, description, userFullName!!, downloadLink!!))
            publishProgress(mImageList)
        }
    }

    companion object {
        private const val DESCRIPTION = "description"
        private const val DOWNLOAD = "download"
        private const val GET = "GET"
        private const val CLIENT_ID = "client_id"
        private const val ITEMS_PER_PAGE = "30"
        private const val LINKS = "links"
        private const val NAME = "name"
        private const val PAGE = "page"
        private const val PER_PAGE = "per_page"
        private const val PHOTO_SEARCH_URL = "https://api.unsplash.com/search/photos"
        private const val QUERY = "query"
        private const val RESULTS = "results"
        private const val SMALL_IMAGE = "small"
        private const val TOKEN = "7c08f9cb51eea74a997b5590cd9b6c0d9806b49de9680da104d99fb2b1f33a63"
        private const val TOTAL_PAGES = "total_pages"
        private const val URLS = "urls"
        private const val USER = "user"
    }
}