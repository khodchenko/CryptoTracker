package com.example.cryptotraker

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient

import androidx.fragment.app.Fragment
import com.example.cryptotraker.databinding.FragmentCryptoPageBinding

class CryptoPage : Fragment(R.layout.fragment_crypto_page) {

    private val URL = "https://coinmarketcap.com/currencies/"
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

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, url : String): Boolean {
                view?.loadUrl(url)
                return true
            }
        }
        binding.webView.loadUrl("$URL$myValue")

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}