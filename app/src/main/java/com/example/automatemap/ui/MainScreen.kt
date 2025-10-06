package com.example.automatemap.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.automatemap.R
import com.example.automatemap.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel(), lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,){
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Listen for toast messages
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.clearToastMessage()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(
                text = stringResource(R.string.show_map)
            )
            Button(
                onClick = {
                    //navigate to next page
                    viewModel.launchAutomateMapScreen(context)
                },
                modifier = Modifier
                    .padding(15.dp)
            ) {
                Text(text = stringResource(R.string.show_map))
            }
        }
    }
}

@Preview(showSystemUi = true, name = "main screen")
@Composable
fun MainScreenPreview(){
    MainScreen()
}

