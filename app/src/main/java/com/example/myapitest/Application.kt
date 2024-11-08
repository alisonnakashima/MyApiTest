package com.example.myapitest

import android.app.Application
import com.example.myapitest.database.DatabaseBuilder

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        init()
    }


    /**
     * função para inicializar as dependências através do context
     */
    private fun init(){
        DatabaseBuilder.getInstance(this)
    }
}