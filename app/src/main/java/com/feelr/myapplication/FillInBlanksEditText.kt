package com.feelr.myapplication

import android.content.Context
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewTreeObserver.OnPreDrawListener
import androidx.appcompat.widget.AppCompatEditText
import com.feelr.myapplication.FillInBlanksEditText.BlanksSpan

class FillInBlanksEditText : AppCompatEditText, OnFocusChangeListener, TextWatcher {
    private var mLastSelStart = 0
    private var mLastSelEnd = 0
    private var mSpans: Array<BlanksSpan>? = null
    private var mUndoChange: Editable? = null
    private var mWatcherSpan: BlanksSpan? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        mSpans = setSpans()
        onFocusChangeListener = this
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        mSpans = null
        super.onRestoreInstanceState(state)
        val e = editableText
        mSpans = e.getSpans(0, e.length, BlanksSpan::class.java)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            addTextChangedListener(this)
            if (findInSpan(selectionStart, selectionEnd) != null) {
                mLastSelStart = selectionStart
                mLastSelEnd = selectionEnd
            } else if (findInSpan(mLastSelStart, mLastSelEnd) == null) {
                setSelection(editableText.getSpanStart(mSpans!![0]))
            }
        } else {
            removeTextChangedListener(this)
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (!isFocused || mSpans == null ||
            selectionStart == mLastSelStart && selectionEnd == mLastSelEnd
        ) {
            return
        }

        // The selection must be completely within a Blankspan.
        val span = findInSpan(selStart, selEnd)
        if (span == null) {
            // Current selection is not within a Blankspan. Restore selection to prior location.
            moveCursor(mLastSelStart)
        } else if (selStart > editableText.getSpanStart(span) + span.dataLength) {
            // Acceptable location for selection (within a Blankspan).
            // Make sure that the cursor is at the end of the entered data.  mLastSelStart = getEditableText().getSpanStart(span) + span.getDataLength();
            mLastSelEnd = mLastSelStart
            moveCursor(mLastSelStart)
        } else {
            // Just capture the placement.
            mLastSelStart = selStart
            mLastSelEnd = selEnd
        }
        super.onSelectionChanged(mLastSelStart, mLastSelEnd)
    }

    // Safely move the cursor without directly invoking setSelection from onSelectionChange.
    private fun moveCursor(selStart: Int) {
        post { setSelection(selStart) }
        // Stop cursor form jumping on move.
        viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)
                return false
            }
        })
    }

    private fun findInSpan(selStart: Int, selEnd: Int): BlanksSpan? {
        for (span in mSpans!!) {
            if (selStart >= editableText.getSpanStart(span) &&
                selEnd <= editableText.getSpanEnd(span)
            ) {
                return span
            }
        }
        return null
    }

    // Set up a Blankspan to cover each occurrence of BLANKS_TOKEN.
    private fun setSpans(): Array<BlanksSpan> {
        val e = editableText
        val s = e.toString()
        var offset = 0
        var blanksOffset: Int
        while (s.substring(offset).indexOf(BLANKS_TOKEN)
                .also { blanksOffset = it } != -1
        ) {
            offset += blanksOffset
            e.setSpan(
                BlanksSpan(Typeface.BOLD),
                offset,
                offset + BLANKS_TOKEN.length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            offset += BLANKS_TOKEN.length
        }
        return e.getSpans(0, e.length, BlanksSpan::class.java)
    }

    // Check change to make sure that it is acceptable to us.
    override fun beforeTextChanged(
        s: CharSequence,
        start: Int,
        count: Int,
        after: Int
    ) {
        mWatcherSpan = findInSpan(start, start + count)
        if (mWatcherSpan == null) {
            // Change outside of a Blankspan. Just put things back the way they were.
            // Do this in afterTextChaanged.  mUndoChange = Editable.Factory.getInstance().newEditable(s);
        } else {
            // Change is OK. Track data length.
            mWatcherSpan!!.adjustDataLength(count, after)
        }
    }

    override fun onTextChanged(
        s: CharSequence,
        start: Int,
        before: Int,
        count: Int
    ) {
        // Do nothing...
    }

    override fun afterTextChanged(s: Editable) {
        if (mUndoChange == null) {
            // The change is legal. Modify the contents of the span to the format we want.
            val newContents = mWatcherSpan!!.getFormattedContent(s)
            if (newContents != null) {
                removeTextChangedListener(this)
                val selection = selectionStart
                s.replace(s.getSpanStart(mWatcherSpan), s.getSpanEnd(mWatcherSpan), newContents)
                setSelection(selection)
                addTextChangedListener(this)
            }
        } else {
            // Illegal change - put things back the way they were.
            removeTextChangedListener(this)
            text = mUndoChange
            mUndoChange = null
            addTextChangedListener(this)
        }
    }

    class BlanksSpan : StyleSpan {
        var dataLength = 0
            private set

        constructor(style: Int) : super(style) {}
        constructor(src: Parcel) : super(src) {}

        fun adjustDataLength(count: Int, after: Int) {
            dataLength += after - count
        }

        fun getFormattedContent(e: Editable): CharSequence? {
            if (dataLength == 0) {
                return BLANKS_TOKEN
            }
            val spanStart = e.getSpanStart(this)
            return if (e.getSpanEnd(this) - spanStart > dataLength) e.subSequence(
                spanStart,
                spanStart + dataLength
            ) else null
        }

    }

    companion object {
        private const val TAG = "FillInBlanksEditText"
        private const val BLANKS_TOKEN = "_____"
    }
}