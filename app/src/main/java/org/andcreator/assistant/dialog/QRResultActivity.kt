package org.andcreator.assistant.dialog

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_qrresult.*
import org.andcreator.assistant.R
import org.andcreator.assistant.util.OtherUtils

class QRResultActivity : AppCompatActivity() {

    companion object {

        const val ARG_IMAGE_DATA = "ARG_IMAGE_DATA"
        const val ARG_TEXT_VALUE = "ARG_TEXT_VALUE"
        const val ARG_IMAGE_ROTETION = "ARG_IMAGE_ROTETION"

        const val RESULT_EXIT = 99

    }

    private var textValue = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrresult)
        val m = windowManager
        val d = m.defaultDisplay //为获取屏幕宽高
        val p = window.attributes
//        p.height = (int) (d.getHeight()*0.9); //高度设置为屏幕的0.8
        p.width = d.width * 1 //宽度设置为屏幕的0.8
        window.attributes = p

        initView()

    }

    private fun initView() {
        textValue = intent.getStringExtra(ARG_TEXT_VALUE)?:""
        messageView.text = textValue

        openLink.setOnLongClickListener {
            OtherUtils.copy(textValue, this)
            Toast.makeText(this,"已复制到剪贴板",Toast.LENGTH_SHORT).show()
            true
        }

        openLink.setOnClickListener {
            val newIntent = Intent()
            newIntent.action = Intent.ACTION_VIEW
            val contentUrl = Uri.parse(textValue)
            newIntent.data = contentUrl
            startActivity(Intent.createChooser(newIntent, getString(R.string.select_browser)))
            finish()
        }
    }
}
