package uz.dream.app.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import uz.dream.app.R
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
import uz.dream.app.ui.theme.DreamTheme

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

/** A bottom tab. The icon is a hangul sign: 길 road, 삶 life, 한 Korean, 학 study, 나 me. */
private class Tab(val route: String, @StringRes val label: Int, val glyph: String)

private val tabs = listOf(
    Tab(Routes.ROADMAP, R.string.tab_path, "길"),
    Tab(Routes.GUIDE, R.string.tab_guide, "삶"),
    Tab(Routes.COURSES, R.string.tab_courses, "한"),
    Tab(Routes.UNIVERSITIES, R.string.tab_universities, "학"),
    Tab(Routes.PROFILE, R.string.tab_profile, "나")
)

private val tabRoutes = tabs.map { it.route }.toSet()

/** Not signed in -> login screens. Signed in -> the app with its five tabs. The two cross-fade. */
@Composable
fun DreamRoot(vm: DreamViewModel) {
    val session by vm.session.collectAsStateWithLifecycle()
    AnimatedContent(
        targetState = session == null,
        transitionSpec = {
            (fadeIn(tween(420)) + scaleIn(initialScale = 0.96f, animationSpec = tween(420))) togetherWith
                fadeOut(tween(220))
        },
        label = "auth-or-app"
    ) { signedOut ->
        if (signedOut) AuthFlow(vm) else MainFlow(vm)
    }
}

@Composable
private fun MainFlow(vm: DreamViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val snackbar = remember { SnackbarHostState() }
    val showTabs = route == null || route in tabRoutes
    val context = LocalContext.current

    // short messages from the ViewModel ("Course unlocked", errors) appear at the bottom, in the current language
    LaunchedEffect(Unit) {
        vm.notices.collect { snackbar.showSnackbar(it.resolve(context)) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbar, if (showTabs) Modifier else Modifier.navigationBarsPadding()) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showTabs,
                enter = slideInVertically(tween(260)) { it } + fadeIn(tween(260)),
                exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(160))
            ) {
                DreamTabBar(
                    selected = tabs.indexOfFirst { it.route == route }.coerceAtLeast(0),
                    onSelect = { openTab(nav, tabs[it].route) }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.ROADMAP,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            enterTransition = { enter() },
            exitTransition = { exit() },
            popEnterTransition = { popEnter() },
            popExitTransition = { popExit() }
        ) {
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

// ---------- screen transitions ----------
// Between tabs: a quick cross-fade with a hint of zoom. Into a detail screen: it slides in from the right.

private fun AnimatedContentTransitionScope<NavBackStackEntry>.betweenTabs(): Boolean =
    initialState.destination.route in tabRoutes && targetState.destination.route in tabRoutes

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enter(): EnterTransition =
    if (betweenTabs()) {
        fadeIn(tween(240, delayMillis = 60)) + scaleIn(initialScale = 0.97f, animationSpec = tween(300, easing = FastOutSlowInEasing))
    } else {
        slideInHorizontally(tween(360, easing = FastOutSlowInEasing)) { it / 3 } + fadeIn(tween(300))
    }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exit(): ExitTransition =
    if (betweenTabs()) {
        fadeOut(tween(140))
    } else {
        slideOutHorizontally(tween(360, easing = FastOutSlowInEasing)) { -it / 6 } + fadeOut(tween(260))
    }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnter(): EnterTransition =
    if (betweenTabs()) {
        fadeIn(tween(240))
    } else {
        slideInHorizontally(tween(360, easing = FastOutSlowInEasing)) { -it / 6 } + fadeIn(tween(300))
    }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExit(): ExitTransition =
    if (betweenTabs()) {
        fadeOut(tween(140))
    } else {
        slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { it / 3 } + fadeOut(tween(240)) +
            scaleOut(targetScale = 0.98f, animationSpec = tween(320))
    }

/** Bottom bar: hangul icons, a saffron highlight that slides to the chosen tab. */
@Composable
private fun DreamTabBar(selected: Int, onSelect: (Int) -> Unit) {
    val palette = DreamTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp), clip = false)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val itemWidth = maxWidth / tabs.size
            val x by animateDpAsState(
                itemWidth * selected,
                spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
                label = "tab-indicator"
            )
            // the sliding highlight behind the selected icon
            Box(
                Modifier
                    .offset(x = x + (itemWidth - 52.dp) / 2, y = 2.dp)
                    .size(width = 52.dp, height = 34.dp)
                    .background(palette.accentSoft, RoundedCornerShape(50))
            )
            Row(Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { i, tab ->
                    val on = i == selected
                    val scale by animateFloatAsState(
                        if (on) 1.12f else 1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium), label = "tab-scale"
                    )
                    val glyphColor by animateColorAsState(
                        if (on) palette.onAccentSoft else MaterialTheme.colorScheme.onSurfaceVariant, tween(220), label = "tab-glyph"
                    )
                    val labelColor by animateColorAsState(
                        if (on) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, tween(220), label = "tab-label"
                    )
                    Column(
                        Modifier
                            .weight(1f)
                            .bounceClick(shape = RoundedCornerShape(18.dp)) { onSelect(i) }
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.height(38.dp), contentAlignment = Alignment.Center) {
                            Text(
                                tab.glyph,
                                color = glyphColor,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            stringResource(tab.label),
                            color = labelColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
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
