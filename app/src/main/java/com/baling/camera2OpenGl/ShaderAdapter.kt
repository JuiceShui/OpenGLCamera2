package com.baling.camera2OpenGl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.baling.camera2OpenGl.ShaderAdapter.Holder
import com.baling.camera2OpenGl.shader.IShader
import java.util.ArrayList

class ShaderAdapter : RecyclerView.Adapter<Holder>, View.OnClickListener {
    var mRecycerView: RecyclerView? = null
    var mShaders: ArrayList<ShaderInfo?>? = null
    var mListener: OnSelectShaderListener? = null
    var mSelectPosition = 0

    constructor(recyclerView: RecyclerView, shaders: ArrayList<ShaderInfo?>) {
        mRecycerView = recyclerView
        mShaders = shaders
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mShaderName = itemView.findViewById<TextView>(R.id.shader_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shader_select_item, parent, false)
        view.setOnClickListener(this)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return mShaders!!.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.mShaderName.setText(mShaders!!.get(position)!!.name)
        holder.itemView.isSelected = mSelectPosition == position
    }

    class ShaderInfo(
        @param:StringRes val name: Int,
        @NonNull clazz: Class<out IShader?>
    ) {
        private val mClass: Class<out IShader?>
        private var mShader: IShader? = null
        val shader: IShader?
            get() {
                if (mShader == null) {
                    mShader = try {
                        mClass.newInstance()
                    } catch (e: Exception) {
                        throw RuntimeException("can not create shader " + name, e)
                    }
                }
                return mShader
            }

        init {
            mClass = clazz
        }
    }

    interface OnSelectShaderListener {
        fun onSelectShader(shader: IShader?)
    }

    fun setOnSelectShaderListener(listener: OnSelectShaderListener) {
        mListener = listener
    }

    override fun onClick(v: View?) {
        val newSelect = mRecycerView!!.getChildAdapterPosition(v!!)
        if (mSelectPosition != newSelect) {
            val oldSelect = mSelectPosition
            mSelectPosition = newSelect
            notifyItemChanged(oldSelect)
            notifyItemChanged(newSelect)
        }
        if (mListener != null) {
            val shaderInfo = mShaders!!.get(newSelect)
            mListener!!.onSelectShader(shaderInfo!!.shader)
        }
    }
}