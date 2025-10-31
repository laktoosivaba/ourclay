package ee.rofl.ourclay

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class Status {
    OPENING,
    LOCK_FOUND,
    SUCCESS,
    FAILURE,
    UNKNOWN
}

class MainViewModel : ViewModel() {
    private val _status = MutableStateFlow(Status.UNKNOWN)
    val status: StateFlow<Status> = _status

    fun setStatus(status: Status) {
        _status.value = status
    }
}