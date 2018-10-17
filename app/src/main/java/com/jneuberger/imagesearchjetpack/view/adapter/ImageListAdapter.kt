package com.jneuberger.imagesearchjetpack.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jneuberger.imagesearchjetpack.R
import com.jneuberger.imagesearchjetpack.data.Image
import kotlinx.android.synthetic.main.image_grid_item.view.*


class ImageListAdapter(private val mContext: Context) : RecyclerView.Adapter<ImageListAdapter.ViewHolder>() {
    private val mImageList = ArrayList<Image>()
    private lateinit var mHolder: ViewHolder
    private lateinit var mOnClickListener: View.OnClickListener
    private lateinit var mOnLongClickListener: View.OnLongClickListener

    override fun getItemCount(): Int {
        return mImageList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.image_grid_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mHolder = holder
        mHolder.bindItems(mImageList[position])
        mHolder.itemView.setOnClickListener { view -> mOnClickListener.onClick(view) }
        mHolder.itemView.setOnLongClickListener { view -> mOnLongClickListener.onLongClick(view) }
    }

    fun setOnClickListener(onClickListener: View.OnClickListener) {
        mOnClickListener = onClickListener
    }

    fun setOnLongClickListener(onLongClickListener: View.OnLongClickListener) {
        mOnLongClickListener = onLongClickListener
    }

    fun updateImageList(imageList: List<Image>) {
        mImageList.clear()
        mImageList.addAll(imageList)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindItems(image: Image) {
            itemView.imageView.setImageBitmap(image.smallImage)
            image.description?.let {
                itemView.description.text = if (it == NULL) {
                    ""
                } else {
                    it
                }
                itemView.userFullName.text = image.user
                if (image.isDownloading!!) {
                    itemView.progressBar.visibility = View.VISIBLE
                    itemView.progressBar.isIndeterminate = true
                } else {
                    itemView.progressBar.visibility = View.GONE
                    itemView.progressBar.isIndeterminate = false
                }
                if (image.isDownloaded!!) {
                    itemView.downloaded.visibility = View.VISIBLE
                } else {
                    itemView.downloaded.visibility = View.GONE
                }
                if (image.isDownloading!! || image.isDownloaded!!) {
                    itemView.itemLayout.alpha = .5f
                } else {
                    itemView.itemLayout.alpha = 1f
                }
            }
        }
    }

    companion object {
        const val NULL = "null"
    }
}