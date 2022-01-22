package com.example.cryptotraker

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.cryptotraker.databinding.FragmentCryptoPageBinding

class CryptoPage : Fragment(R.layout.fragment_crypto_page) {

    private val URL = "https://coinmarketcap.com/currencies/"
    val HEADERS : Map<String, String> = mapOf("User-Agent" to
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:96.0) Gecko/20100101 Firefox/96.0")
    private var _binding : FragmentCryptoPageBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCryptoPageBinding.inflate(inflater, container, false)
        val view = binding.root

        val myValue = this.requireArguments().getString("message")

        binding.webView.webViewClient = object : WebViewClient(){

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                binding.idPBFragment.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                binding.idPBFragment.visibility = View.INVISIBLE
            }


            override fun shouldOverrideUrlLoading(view: WebView?, url : String): Boolean {
                view?.loadUrl(url)
                return true
            }
        }
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadUrl("$URL$myValue", HEADERS)



        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action === KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                Log.e("gif--", "fragment back key is clicked")
                requireActivity().supportFragmentManager.popBackStack(
                    "fragment",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                return@OnKeyListener true
            }
            false
        })
    }

}