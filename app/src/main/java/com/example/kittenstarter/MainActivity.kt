package com.example.kittenstarter

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import android.os.Bundle
import android.provider.Contacts.Photos
import android.view.WindowManager
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.CursorAdapter
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity

import android.util.Log;
import android.view.View;
import androidx.appcompat.widget.Toolbar


import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.create
import java.lang.Exception

// INFO https://www.flickr.com/services/api/explore/flickr.photos.search

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), Callback<Result>, OnItemClickListener {
    private lateinit var grid: GridView
    private lateinit var adapter: CursorAdapter
    private lateinit var helper: PhotosDBHelper
    private lateinit var retrofit: Retrofit
    private lateinit var service: FlickrService
    private lateinit var statement: SQLiteStatement

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)

        // Сделаем тулбар
        val bar = findViewById<View>(R.id.top_toolbar) as Toolbar
        setSupportActionBar(bar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.title = "Kitten"
        grid = findViewById<View>(R.id.grid) as GridView

        // Чтобы скроллинг "вверх" грида вызывал
        // исчезновение тулбара.
        grid.isNestedScrollingEnabled = true

        adapter = PhotoAdapter(this, null, 0)
        helper = PhotosDBHelper(this)

        statement = helper.writableDatabase.compileStatement(sql)

        grid.adapter = adapter

        retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(FlickrService::class.java)

        grid.onItemClickListener

        grid.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                if (!loading) {
                    if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                        val last = grid.lastVisiblePosition
                        val total = grid.count
                        if (total - last < threshold) {
                            loadMore(currentPage + 1, term)
                            Log.d("happy", "onScrollStateChanged")
                        }
                    }
                }
            }

            override fun onScroll(
                view: AbsListView,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int,
            ) {
            }
        })

        // Загрузить картинки
        startOver()
    }

    // Начинаем с первой страницы
    private var currentPage = 1

    // Запрос по-умолчанию
    private val term = "kitten"

    // Выполняется при старте приложения
    // или изменении поискового запроса
    private fun startOver() {
        helper.writableDatabase.delete(
            PhotosTable.TABLE_PHOTOS,
            null,
            null
        )

        // Начнём с первой страницы
        currentPage = 1

        // Загрузим
        loadMore(currentPage, term)
    }

    // Используется для загрузки новой порции изображений из сервиса
    // Вызов Retrofit
    private fun loadMore(page: Int, search: String) {
        loading = true

        val call: Call<Result> = service.search(
            "flickr.photos.search",
            "1694c8371b676e2b1cf9000245f9b1f2",
            search,
            "json",
            1,
            page
        )
        call.enqueue(this)
    }

    // Только один процесс загрузки данных с сервера
    private var loading = false
    override fun onResponse(call: Call<Result>, response: Response<Result>) {
        val result = response.body()
        if (result!!.stat.equals("ok")) {

            currentPage = result.photos!!.page!!

            val photos = result.photos

            val db = helper.writableDatabase
            db.beginTransaction()

            try {
                for (p in photos!!.photo!!) {
//                Log.d("happySDK", p.title!!)
                    val url = createUrl(p)
                    // insert into photo (url) values (?)
                    statement.bindString(1, url)
                    statement.execute()
                }
                db.setTransactionSuccessful()
            } catch (_: Exception) {

            } finally {
                db.endTransaction()
            }

        }
        val cursor = photoCursor
        adapter.swapCursor(cursor)
        loading = false
    }

    private val photoCursor: Cursor
        get() = helper.readableDatabase.query(
            PhotosTable.TABLE_PHOTOS,
            null,
            null,
            null,
            null,
            null,
            null
        )

    override fun onFailure(call: Call<Result?>, t: Throwable) {
        Log.d("happySDK", t.message!!)
        loading = false
    }

    @SuppressLint("Range")
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val intent = Intent(this, Detail::class.java)
        val cursor = adapter.cursor
        cursor.moveToPosition(position)
        val url = cursor.getString(
            cursor.getColumnIndex(PhotosTable.COLUMN_URL)
        )
        intent.putExtra(IMAGE_URL, url)
        startActivity(intent)
    }

    companion object {
        // Константа для передачи URL в детальную Activity
        // через Intent
        const val IMAGE_URL = "IMAGE_URL"

        private const val sql =
            "  insert into   ${PhotosTable.TABLE_PHOTOS}  (  ${PhotosTable.COLUMN_URL}  )  values  (  ?  )  ;  "

        // Порог
        // Если разница между крайним видимым элементом GridView и количеством
        // элементов в GridView меньше порога, запросим
        // еще картинки с сервера
        private const val threshold = 40
        private fun createUrl(p: Photo): String {
            // Сервисная функция для получения URL картинки по объекту
            // Подробности https://www.flickr.com/services/api/misc.urls.html
            //Log.d("happy", url);
            return java.lang.String.format(
                "https://farm%s.staticflickr.com/%s/%s_%s_q.jpg",
                p.farm,
                p.server,
                p.id,
                p.secret
            )
        }
    }
}