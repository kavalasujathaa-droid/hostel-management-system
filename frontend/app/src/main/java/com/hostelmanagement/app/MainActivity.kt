package com.hostelmanagement.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hostelmanagement.app.core.datastore.SessionManager
import com.hostelmanagement.app.core.theme.HostelManagementTheme
import com.hostelmanagement.app.ui.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HostelManagementTheme {
                AppNavGraph(sessionManager = sessionManager)
            }
        }
    }
}
