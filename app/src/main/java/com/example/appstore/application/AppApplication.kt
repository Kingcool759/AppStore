package com.example.appstore.application

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.example.appstore.DownloadViewModel
import com.example.appstore.repository.AppRepository
import com.example.appstore.repository.AppRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * @Author : zhaojianwei02
 * @Date : 2022/8/1 6:40 下午
 * @Description :
 */
class AppApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        ctx = this
        initKoin()
    }

    companion object{
        @SuppressLint("StaticFieldLeak")
        var ctx: Context? = null
        fun getContext(): Context? {
            return ctx
        }
    }

    fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@AppApplication)
            androidFileProperties()
            modules(appModule)
        }
    }

    /**
     * 定义组件
     */
    val appModule = module {

        viewModel { DownloadViewModel(get()) }

        single<AppRepository> { AppRepositoryImpl() }
    }
}