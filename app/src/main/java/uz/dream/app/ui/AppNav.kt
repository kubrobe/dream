package uz.dream.app.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import uz.dream.app.ui.screens.AuthFlow
import uz.dream.app.ui.screens.CardScreen
import uz.dream.app.ui.screens.CourseScreen
import uz.dream.app.ui.screens.CoursesScreen
import uz.dream.app.ui.screens.GuideScreen
import uz.dream.app.ui.screens.LessonScreen
import uz.dream.app.ui.screens.ProfileScreen
import uz.dream.app.ui.screens.QuestionsScreen
import uz.dream.app.ui.screens.RoadmapScreen
import uz.dream.app.ui.screens.RoomScreen
import uz.dream.app.ui.screens.UniversitiesScreen

/** All screen addresses ("routes") in one place */
object Routes {
    const val ROADMAP = "roadmap"
    const val GUIDE = "guide"
    const val COURSES = "courses"
    const val UNIVERSITIES = "universities"
    const val PROFILE = "profile"
    const val CARD = "card/{id}"
    const val COURSE = "course/{id}"
    const val LESSON = "lesson/{id}"
    const val ROOM = "room/{id}"
    const val QUESTIONS = "questions/{id}"
    fun card(id: String) = "card/$id"
    fun course(id: String) = "course/$id"
    fun lesson(id: String) = "lesson/$id"
    fun room(id: String) = "room/$id"
    fun questions(id: String) = "questions/$id"
}

private class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.ROADMAP, "Путь", Icons.Filled.Place),
    Tab(Routes.GUIDE, "Гид", Icons.Filled.Info),
    Tab(Routes.COURSES, "Курсы", Icons.Filled.PlayArrow),
    Tab(Routes.UNIVERSITIES, "Вузы", Icons.Filled.Star),
    Tab(Routes.PROFILE, "Профиль", Icons.Filled.Person)
)

/** Not signed in -> login screens. Signed in -> the app with its five tabs. */
@Composable
fun DreamRoot(vm: DreamViewModel) {
    val session by vm.session.collectAsStateWithLifecycle()
    if (session == null) AuthFlow(vm) else MainFlow(vm)
}

@Composable
private fun MainFlow(vm: DreamViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val snackbar = remember { SnackbarHostState() }
    val showTabs = tabs.any { it.route == route }

    // short messages from the ViewModel ("Курс открыт", errors) appear at the bottom
    LaunchedEffect(Unit) {
        vm.notices.collect { snackbar.showSnackbar(it) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        // without the tab bar the message must stay above the system navigation buttons
        snackbarHost = { SnackbarHost(snackbar, if (showTabs) Modifier else Modifier.navigationBarsPadding()) },
        bottomBar = {
            if (showTabs) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = { openTab(nav, tab.route) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = Routes.ROADMAP, modifier = Modifier.padding(padding)) {
            composable(Routes.ROADMAP) {
                RoadmapScreen(vm, onCard = { nav.navigate(Routes.card(it)) }, onRoom = { nav.navigate(Routes.room(it)) })
            }
            composable(Routes.GUIDE) {
                GuideScreen(vm, onCard = { nav.navigate(Routes.card(it)) }, onRoom = { nav.navigate(Routes.room(it)) })
            }
            composable(Routes.COURSES) {
                CoursesScreen(vm, onCourse = { nav.navigate(Routes.course(it)) })
            }
            composable(Routes.UNIVERSITIES) { UniversitiesScreen(vm) }
            composable(Routes.PROFILE) { ProfileScreen(vm) }

            composable(Routes.CARD, arguments = idArg) { e ->
                CardScreen(vm, e.arguments?.getString("id").orEmpty(),
                    onBack = { nav.popBackStack() }, onRoom = { nav.navigate(Routes.room(it)) })
            }
            composable(Routes.COURSE, arguments = idArg) { e ->
                CourseScreen(vm, e.arguments?.getString("id").orEmpty(),
                    onBack = { nav.popBackStack() },
                    onLesson = { nav.navigate(Routes.lesson(it)) },
                    onRoom = { nav.navigate(Routes.room(it)) },
                    onQuestions = { nav.navigate(Routes.questions(it)) })
            }
            composable(Routes.LESSON, arguments = idArg) { e ->
                LessonScreen(vm, e.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() })
            }
            composable(Routes.ROOM, arguments = idArg) { e ->
                RoomScreen(vm, e.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() })
            }
            composable(Routes.QUESTIONS, arguments = idArg) { e ->
                QuestionsScreen(vm, e.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() })
            }
        }
    }
}

private val idArg = listOf(navArgument("id") { type = NavType.StringType })

/** Switching tabs keeps each tab's scroll position and never stacks the same tab twice. */
private fun openTab(nav: NavHostController, route: String) {
    nav.navigate(route) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
