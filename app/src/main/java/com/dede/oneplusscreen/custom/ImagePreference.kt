package com.dede.oneplusscreen.custom

import android.animation.Animator
import android.content.Context
import android.graphics.drawable.Drawable
import android.preference.Preference
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.widget.ImageView
import com.dede.oneplusscreen.R

/**
 * @author hsh
 * @time 2017/6/27 027 下午 02:15.
 * @doc
 */
class ImagePreference(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
    : Preference(context, attrs, defStyleAttr, defStyleRes) {

    private var drawable: Drawable
    private lateinit var imageView: ImageView

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    init {
        layoutResource = R.layout.layout_image_preference
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.imagePreference, defStyleAttr, defStyleRes)
        drawable = typedArray.getDrawable(R.styleable.imagePreference_src) ?: ContextCompat.getDrawable(context, R.drawable.img_test_bg)
        typedArray.recycle()
    }

    var compled: Boolean = false

    val listener: ViewTreeObserver.OnGlobalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            compled = true
            setImageDrawable(drawable)
            imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    }

    override fun onBindView(view: View) {
        super.onBindView(view)
        imageView = view as ImageView
        setImageDrawable(drawable)
        if (!compled)
            imageView.viewTreeObserver.addOnGlobalLayoutListener(listener)
        if (longClickListener != null)
            imageView.setOnLongClickListener(longClickListener)
        if (clickListener != null)
            imageView.setOnClickListener(clickListener)
    }

    fun setImageDrawable(drawable: Drawable) {
        this.drawable = drawable
        if (compled) {
            imageView.setImageDrawable(this.drawable)
        }
    }

    fun setImageDrawable(drawable: Drawable, showAnim: Boolean) {
        setImageDrawable(drawable)
        if (compled && showAnim) startShowAnim()
    }

    fun startShowAnim() {
        val animator = ViewAnimationUtils.createCircularReveal(imageView, imageView.width / 2,
                imageView.height / 2, 0f, imageView.width * 1f)
        animator.duration = 1000
        animator.start()
    }

    fun startHideAnim(listener: Animator.AnimatorListener) {
        val animator = ViewAnimationUtils.createCircularReveal(imageView, imageView.width / 2,
                imageView.height / 2, imageView.width * 1f, 0f)
        animator.duration = 1000L
        animator.addListener(listener)
        animator.start()
    }

    var longClickListener: View.OnLongClickListener? = null
    var clickListener: View.OnClickListener? = null

    fun setOnLongClickListener(listener: View.OnLongClickListener) {
        this.longClickListener = listener
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        this.clickListener = listener
    }
}