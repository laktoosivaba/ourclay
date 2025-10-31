package ee.rofl.ourclay

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import ee.rofl.ourclay.ui.theme.ClayKeyTheme
import com.myclay.claysdk.api.ClaySDK
import com.myclay.claysdk.api.ILockDiscoveryCallback
import com.myclay.claysdk.api.error.ClayException
import com.saltosystems.justinmobile.sdk.model.Result

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent, openLock: ((finish: Boolean) -> Unit)? = null) {
        Log.d("INTENT_LOCKS", "Start ${intent.action}")

        if (Intent.ACTION_VIEW == intent.action) {
            viewModel.setStatus(Status.OPENING)
            openLock?.invoke(true)
        }
    }

    fun initOpener(): (finish: Boolean) -> Unit {
        val tag = "CLAY_SDK"

        val API_PUBLIC_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEkGIqR7bC6cpt9DbMfFt8FpP1BEtlfbgayuMwMhXRrk1bRidhJXcGVvjocuol7xudJ8rWMNJr4sbhoTW3/KprhA=="
        val UUID = "00000000-2965-d2d3-0000-00004642b1b1-keychain"

        val claySDK = ClaySDK.init(applicationContext, API_PUBLIC_KEY, UUID)

        Log.d(tag, claySDK.publicKey.replace("\n", "\\n"))

        val mkey = "MFgwDQYJYIZIAWUDBAIBBQAERzBFAiEA69OWuQe4MaNR5kqcZzlKj13KI8ttIR4MPKiJSI9YhcUCICE2gsGniPj1sfya93COOL2gxHM8vHssnm+kirHCLLH4VklSR0lMLURBVEEtU0lHTkFUVVJFMIIBnwIBADCCAZgGCSqGSIb3DQEHA6CCAYkwggGFAgECMYIBVjCCAVICAQKgIgQg2KQPkMsW9s1yGfWrCw/F/4hGtqOqG5kSuntpsQ/3oTswEwYHKoZIzj0CAQYIKoZIzj0DAQcEggESMIIBDgIBADBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABB+40dVR7A8SsFsEkTwCGGJsORhrpKVuL4mHZCZidxMgQ9Uma/dMOrzRPEiIPKc/wQku2ksMxV8yx9gZhYAmmQswGAYHKIGMcQIFAjANBglghkgBZQMEAgIFADBBMA0GCWCGSAFlAwQCAgUABDAlxHWQK8eu7+Zu8PuuHhi1TelywMCP+o0ITWoLqDIG2lO2y4n8wC7APp3KlQmjr3IwUTAdBglghkgBZQMEASoEENebG2qaH01UTv1Z1Tmj3YIEMLD7buaLKBTUIZggx3AQXpnYHqAsGMqkv3xfQLA64kKqVnakphsmZgWMMx86B6HKNzAmBgkqhkiG9w0BBwEwGQYJYIZIAWUDBAEuBAxTjHHLL3C6ldwidUq5vOFWY9CW30xSEdRPyFs/6Mai67KZ1RhEQLHT/1SU9rPXs3/Y1uUmRQpgfOYTBUvq+WWDYnGX5/Vx0AfYyg+hdoT0ZA2Lf0L9cNX2/cs="

        val openLock = { finish: Boolean ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    0
                )
            }

            viewModel.setStatus(Status.OPENING)

            try {
                claySDK.openDoor(mkey, object : ILockDiscoveryCallback {
                    override fun onPeripheralFound() {
                        viewModel.setStatus(Status.LOCK_FOUND)
                    }

                    override fun onNFCPeripheralFound(installationId: String?) {}
                    override fun onSuccess(result: Result?) {
                        viewModel.setStatus(Status.SUCCESS)

                        if (finish) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                this@MainActivity.finish()
                            }, 1500) // 1.5 seconds delay
                        }
                    }

                    override fun onFailure(exception: ClayException?) {
                        viewModel.setStatus(Status.FAILURE)

                        if (finish) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                this@MainActivity.finish()
                            }, 1500) // 1.5 seconds delay
                        }
                    }
                }, ClaySDK.OPENING_METHOD.BLE)
            } catch (e: Exception) {
                Log.e(tag, e.message, e)
                viewModel.setStatus(Status.FAILURE)

                if (finish) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        this@MainActivity.finish()
                    }, 1500) // 1.5 seconds delay
                }
            }
        }

        return openLock
    }

    fun statusToEmoji(status: Status): String = when (status) {
        Status.OPENING -> "\uD83E\uDDBE" // 🦾 Robotic arm
        Status.LOCK_FOUND -> "\uD83D\uDD75\uFE0F\u200D♂️" // 🕵️‍♂️ Detective
        Status.SUCCESS -> "\uD83C\uDF89" // 🎉 Party popper
        Status.FAILURE -> "\uD83D\uDCA5" // 💥 Collision
        else -> "" //
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val openLock = initOpener()

        handleIntent(intent, openLock)

        enableEdgeToEdge()
        setContent {
            val status by viewModel.status.collectAsState()
            val emoji = statusToEmoji(status)

            ClayKeyTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButtonPosition = FabPosition.End,
                    floatingActionButton = {
                        if (status == Status.UNKNOWN
                            || status == Status.FAILURE
                            || status == Status.SUCCESS
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    openLock(false)
                                }
                            ) {
                                Text("\uD83D\uDD11")
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                emoji,
                                fontSize = 200.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
//                        Spacer(modifier = Modifier.height(32.dp))
//                        Text("Debug Info:")
//                        Column(
//                            modifier = Modifier
//                                .weight(1f)
//                                .verticalScroll(scrollState)
//                        ) {
//                            // Debug info here
//                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ClayKeyTheme {
        Greeting("Clay Key")
    }
}