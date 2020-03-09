package com.ibashkimi.wheel.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentAboutBinding.inflate(inflater, container, false).run {
            toolbar.apply {
                setTitle(R.string.title_about)
                setNavigationIcon(R.drawable.ic_back_nav)
                setNavigationOnClickListener { requireActivity().onBackPressed() }
            }
            sendFeedback.setOnClickListener {
                sendFeedback()
            }
            root
        }
    }

    private fun sendFeedback() {
        val address = getString(R.string.developer_email)
        val subject = getString(R.string.feedback_subject)

        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)

        val chooserTitle = getString(R.string.feedback_chooser_title)
        startActivity(Intent.createChooser(emailIntent, chooserTitle))
    }
}