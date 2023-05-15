package com.example.kittenstarter

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
import android.widget.Toolbar

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// INFO https://www.flickr.com/services/api/explore/flickr.photos.search

class MainActivity : AppCompatActivity(), Callback<Result?>,
    OnItemClickListener {
    private var grid: GridView? = null
    private var adapter: CursorAdapter? = null
    private var helper: PhotosDBHelper? = null
    private var retrofit: Retrofit? = null
    private var service: FlickrService? = null
    private var statement: SQLiteStatement? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)

        // Сделаем тулбар
        val bar: Toolbar = findViewById<View>(R.id.top_toolbar) as Toolbar
        setSupportActionBar(bar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setTitle("Kitten")
        grid = findViewById<View>(R.id.grid) as GridView

        // Чтобы скроллинг "вверх" грида вызывал
        // исчезновение тулбара.
        grid!!.isNestedScrollingEnabled = true
        adapter = PhotoAdapter(this, null, 0)
        helper = PhotosDBHelper(this)
        statement = helper.getWritableDatabase().compileStatement(sql)
        grid!!.adapter = adapter
        retrofit = Builder()
            .baseUrl("https://api.flickr.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(FlickrService::class.java)
        grid!!.onItemClickListener = this
        grid!!.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                if (!loading) {
                    if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                        val last = grid!!.lastVisiblePosition
                        val total = grid!!.count
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
                totalItemCount: Int
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
        helper!!.writableDatabase.delete(
            PhotosTable.TABLE_PHOTOS,
            null,
            null
        )

        // Начнем с первой страницы
        currentPage = 1

        // Загрузим
        loadMore(currentPage, term)
    }

    // Исользуется для загрузки новой порции изобразений из сервиса
    // Вызов Retrofit
    private fun loadMore(page: Int, search: String) {
        loading = true
        val call: Call<Result> = service.search(
            "flickr.photos.search",
            "7a8da45e81bb8153e93030525b30595b",
            search,
            "json",
            1,
            page
        )
        call.enqueue(this)
    }

    // Только один процесс загрузки данных с сервера
    private var loading = false
    fun onResponse(call: Call<Result?>?, response: Response<Result?>) {
        val result: Result = response.body()
        if (result.getStat().equals("ok")) {
            currentPage = result.getPhotos().getPage()
            val photos: Photos = result.getPhotos()
            val db: SQLiteDatabase = helper.getWritableDatabase()
            db.beginTransaction()
            try {
                for (p in photos.getPhoto()) {
                    // Log.d("happy", p.getTitle());
                    val url = createUrl(p)
                    // insert into photo (url) values (?)
                    statement!!.bindString(1, url)
                    statement!!.execute()
                }
                db.setTransactionSuccessful()
            } catch (e: Exception) {
            } finally {
                db.endTransaction()
            }
        }
        val cursor = photoCursor
        adapter.swapCursor(cursor)
        loading = false
    }

    private val photoCursor: Cursor
        private get() = helper!!.getReadableDatabase().query(
            PhotosTable.TABLE_PHOTOS,
            null,
            null,
            null,
            null,
            null,
            null
        )

    fun onFailure(call: Call<Result?>?, t: Throwable) {
        Log.d("happy", t.message!!)
        loading = false
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val intent = Intent(this, Detail::class.java)
        val cursor: Cursor = adapter.getCursor()
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
        private val sql = "  insert into   " +
                PhotosTable.TABLE_PHOTOS +
                "   (   " +
                PhotosTable.COLUMN_URL +
                "   )   " +
                " values (  ?  ) ;  "

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
                p.getFarm(),
                p.getServer(),
                p.getId(),
                p.getSecret()
            )
        }
    }
}