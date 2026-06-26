package com.dasong.commerce.online

import com.dasong.commerce.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase 客户端单例，通过 Hilt 注入。
 */
@Singleton
class SupabaseProvider @Inject constructor() {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Postgrest)
        }
    }
}
