package uz.dream.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.dream.app.DreamApp
import uz.dream.app.data.Course
import uz.dream.app.data.DreamRepository
import uz.dream.app.data.InfoCard
import uz.dream.app.data.Lesson
import uz.dream.app.data.Message
import uz.dream.app.data.Question
import uz.dream.app.data.Session
import uz.dream.app.data.University
import uz.dream.app.data.UserError
import uz.dream.app.data.remote.ApiException
import uz.dream.app.data.remote.OfflineException

/** Holds screen state and survives screen rotation. Screens call its functions. */
@OptIn(ExperimentalCoroutinesApi::class)
class DreamViewModel(private val repo: DreamRepository) : ViewModel() {

    val session: StateFlow<Session?> = repo.session

    /** true = connected to Supabase, false = local mode (data only on this phone). */
    val online: Boolean get() = repo.online

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()          // a request is running (login, purchase, sync)

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()   // error or hint on the login screens

    private val _notices = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val notices: SharedFlow<String> = _notices.asSharedFlow()   // short messages shown at the bottom

    // ---------- content ----------
    val roadmap: StateFlow<List<InfoCard>> = repo.cards("roadmap").keep(emptyList())
    val guide: StateFlow<List<InfoCard>> = repo.cards("guide").keep(emptyList())
    val courses: StateFlow<List<Course>> = repo.courses().keep(emptyList())
    val universities: StateFlow<List<University>> = repo.universities().keep(emptyList())

    // ---------- what this user did (changes when another user logs in) ----------
    val doneIds: StateFlow<Set<String>> = session
        .flatMapLatest { s -> if (s == null) flowOf(emptyList<String>()) else repo.doneIds(s.userId) }
        .map { it.toSet() }
        .keep(emptySet())

    val enrolledIds: StateFlow<Set<String>> = session
        .flatMapLatest { s -> if (s == null) flowOf(emptyList<String>()) else repo.enrolledIds(s.userId) }
        .map { it.toSet() }
        .keep(emptySet())

    val interestIds: StateFlow<Set<String>> = session
        .flatMapLatest { s -> if (s == null) flowOf(emptyList<String>()) else repo.interestIds(s.userId) }
        .map { it.toSet() }
        .keep(emptySet())

    fun card(id: String): Flow<InfoCard?> = repo.card(id)
    fun course(id: String): Flow<Course?> = repo.course(id)
    fun lessons(courseId: String): Flow<List<Lesson>> = repo.lessons(courseId)
    fun lesson(id: String): Flow<Lesson?> = repo.lesson(id)
    fun messages(roomId: String): Flow<List<Message>> = repo.messages(roomId)
    fun questions(courseId: String): Flow<List<Question>> =
        session.flatMapLatest { s -> if (s == null) flowOf(emptyList<Question>()) else repo.questions(courseId, s.userId) }

    init {
        viewModelScope.launch {
            try {
                repo.seedIfEmpty()
                repo.sync()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // no internet at start: the app still opens with the saved copy
            }
        }
    }

    // ---------- accounts ----------
    fun clearAuthMessage() {
        _authMessage.value = null
    }

    fun login(email: String, password: String) = auth {
        repo.login(email, password)
    }

    fun register(fullName: String, email: String, password: String) = auth {
        val signedIn = repo.register(fullName, email, password)
        if (!signedIn) {
            _authMessage.value = "Аккаунт создан. Открой письмо от Supabase, подтверди email и войди."
        }
    }

    private fun auth(block: suspend () -> Unit) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            _authMessage.value = null
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _authMessage.value = text(e)
            } finally {
                _busy.value = false
            }
        }
    }

    fun logout() = act { repo.logout() }

    // ---------- actions ----------
    fun refresh() = act(showBusy = true) {
        repo.sync()
        _notices.tryEmit(if (online) "Данные обновлены" else "Локальный режим: сервер не подключён")
    }

    fun setStepDone(cardId: String, done: Boolean) = act { repo.setStepDone(cardId, done) }

    fun buyCourse(courseId: String) = act(showBusy = true) {
        repo.buyCourse(courseId)
        _notices.tryEmit("Курс открыт. Хорошей учёбы!")
    }

    fun setInterest(universityId: String, on: Boolean) = act { repo.setInterest(universityId, on) }

    fun setGoal(goal: String) = act { repo.setGoal(goal) }

    fun sendMessage(roomId: String, text: String) = act { repo.sendMessage(roomId, text) }

    fun askQuestion(courseId: String, text: String) = act { repo.askQuestion(courseId, text) }

    fun loadQuestions(courseId: String) = act { repo.refreshQuestions(courseId) }

    /** Called every few seconds while a room is open. Errors are ignored: the next try may work. */
    suspend fun pollRoom(roomId: String) {
        try {
            repo.refreshRoom(roomId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // keep showing the saved messages
        }
    }

    private fun act(showBusy: Boolean = false, block: suspend () -> Unit) {
        viewModelScope.launch {
            if (showBusy) _busy.value = true
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _notices.tryEmit(text(e))
            } finally {
                if (showBusy) _busy.value = false
            }
        }
    }

    private fun text(e: Exception): String = when (e) {
        is UserError, is ApiException, is OfflineException -> e.message ?: "Что-то пошло не так"
        else -> "Что-то пошло не так. Попробуй ещё раз."
    }

    private fun <T> Flow<T>.keep(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as DreamApp
                DreamViewModel(app.repository)
            }
        }
    }
}
