package com.example.kittenstarter

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import com.squareup.picasso.Picasso


class PhotoAdapter(context: Context?, c: Cursor?, flags: Int) :
    CursorAdapter(context, c, flags) {
    // Для CursorAdapter эта функция вызывается для создания View
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup?): View {
        // View view = context.getLayoutInflater(
        val view: View = LayoutInflater.from(context).inflate(R.layout.item, parent, false)
        val holder = Holder()
        holder.picture = view.findViewById<View>(R.id.image) as ImageView
        populateView(holder, cursor, context)
        view.tag = holder
        return view
    }

    // Для CursorAdapter эта функция вызывается для изменения View
    // Нужно только получить Holder из Tag и поменять ImageView, на
    // который он хранит ссылку
    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val holder = view.tag as Holder
        populateView(holder, cursor, context)
    }

    // Загрузка картинки в ImageView, на который хранит ссылку Holder
    @SuppressLint("Range")
    private fun populateView(holder: Holder, cursor: Cursor, context: Context) {
        Picasso
            .with(context)
            .load( // Получаем URL картинки из курсора
                cursor.getString( // Индекс колонки
                    cursor.getColumnIndex( // С нужным названием
                        PhotosTable.COLUMN_URL
                    )
                )
            )
            .fit()
            .centerCrop()
            .into(holder.picture)
    }
}