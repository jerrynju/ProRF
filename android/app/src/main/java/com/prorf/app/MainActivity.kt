package com.prorf.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prorf.app.data.Workflow
import com.prorf.app.data.WorkflowStore
import com.prorf.app.R
import com.prorf.app.ui.screens.*
import com.prorf.app.ui.theme.AppTheme
import com.prorf.app.ui.theme.Prf
import com.prorf.app.ui.theme.ProRFTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var store: WorkflowStore

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        store = WorkflowStore(applicationContext)
        val prefs = getSharedPreferences("prorf", MODE_PRIVATE)

        // File picker for JSON import — result count stored for Snackbar feedback
        val importResultCount = androidx.compose.runtime.mutableStateOf<Int?>(null)
        val importLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                val count = runCatching {
                    val text = contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                    if (text != null) store.importJson(text) else 0
                }.getOrDefault(0)
                importResultCount.value = count
            }
        }

        setContent {
            var theme by remember {
                mutableStateOf(
                    runCatching { AppTheme.valueOf(prefs.getString("theme", "Light")!!) }.getOrDefault(AppTheme.Light),
                )
            }
            val sizeClass = calculateWindowSizeClass(this)
            val expanded = sizeClass.widthSizeClass != WindowWidthSizeClass.Compact

            ProRFTheme(theme) {
                AppRoot(
                    store = store,
                    expanded = expanded,
                    theme = theme,
                    onThemeChange = { theme = it; prefs.edit().putString("theme", it.name).apply() },
                    onImport = { importLauncher.launch("application/json") },
                    importResultCount = importResultCount,
                    onExport = { wf ->
                        val json = store.exportJson(wf)
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(android.content.Intent.EXTRA_TEXT, json)
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "${wf.name}.json")
                        }
                        startActivity(android.content.Intent.createChooser(intent, getString(R.string.action_export)))
                    },
                    onBatchExport = { wfs ->
                        val json = store.exportAllJson(wfs)
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(android.content.Intent.EXTRA_TEXT, json)
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "ProRF_links.json")
                        }
                        startActivity(android.content.Intent.createChooser(intent, getString(R.string.action_export)))
                    },
                )
            }
        }
    }
}

private enum class Tab(@StringRes val labelRes: Int, val icon: ImageVector) {
    Home(R.string.tab_home, Icons.Default.Home),
    Templates(R.string.tab_templates, Icons.AutoMirrored.Filled.LibraryBooks),
    Profile(R.string.tab_profile, Icons.Default.Person),
}

private sealed interface Overlay {
    data class Editor(val workflow: Workflow) : Overlay
    data class Results(val workflow: Workflow) : Overlay
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot(
    store: WorkflowStore,
    expanded: Boolean,
    theme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onImport: () -> Unit = {},
    importResultCount: androidx.compose.runtime.MutableState<Int?> = androidx.compose.runtime.mutableStateOf(null),
    onExport: (Workflow) -> Unit = {},
    onBatchExport: (List<Workflow>) -> Unit = {},
) {
    val p = Prf.colors
    var tab by remember { mutableStateOf(Tab.Home) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("prorf", android.content.Context.MODE_PRIVATE) }
    var onboardingDone by remember { mutableStateOf(prefs.getBoolean("onboarding_done", false)) }

    // CX17: Import feedback Snackbar
    val importCount = importResultCount.value
    val importSuccessMsg = stringResource(R.string.import_success, importCount ?: 0)
    val importFailedMsg = stringResource(R.string.import_failed)
    LaunchedEffect(importCount) {
        if (importCount != null) {
            val msg = if (importCount > 0) importSuccessMsg else importFailedMsg
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
            importResultCount.value = null
        }
    }

    BackHandler(enabled = overlay != null) {
        overlay = when (overlay) {
            is Overlay.Results -> if (expanded) null else (overlay as Overlay.Results).let { Overlay.Editor(it.workflow) }
            else -> null
        }
    }

    val content: @Composable (Modifier) -> Unit = { modifier ->
        Box(modifier.background(p.bg)) {
            if (!onboardingDone) {
                OnboardingScreen(onGetStarted = {
                    prefs.edit().putBoolean("onboarding_done", true).apply()
                    onboardingDone = true
                })
            } else {
            when (val ov = overlay) {
                is Overlay.Editor -> {
                    // CX2: refresh workflow reference from store to avoid stale data
                    val freshWf = store.get(ov.workflow.id) ?: ov.workflow
                    EditorScreen(
                        workflow = freshWf,
                        onBack = { overlay = null },
                        onSave = { store.save(it) },
                        onViewResults = { wf -> overlay = Overlay.Results(wf) },
                        onRename = { wf, newName -> store.save(wf.copy(name = newName)) },
                        showResultsInline = expanded,
                        onExport = { onExport(freshWf) },
                    )
                }
                is Overlay.Results -> {
                    val freshWf = store.get(ov.workflow.id) ?: ov.workflow
                    ResultsScreen(freshWf) {
                        overlay = if (expanded) null else Overlay.Editor(freshWf)
                    }
                }
                null -> when (tab) {
                    Tab.Home -> HomeScreen(
                        workflows = store.workflows,
                        onOpen = { overlay = Overlay.Editor(it) },
                        onCreate = { template -> overlay = Overlay.Editor(store.create(template)) },
                        onDelete = { wf ->
                            store.delete(wf.id)
                            // CX2: clear overlay if it referenced the deleted workflow
                            if (overlay is Overlay.Editor && (overlay as Overlay.Editor).workflow.id == wf.id) {
                                overlay = null
                            }
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.link_deleted),
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        },
                        onDuplicate = { wf ->
                            val newId = "wf-" + System.nanoTime()
                            val cloned = wf.copy(
                                id = newId,
                                name = store.uniqueCopyName(wf.name),
                                nodes = wf.nodes.mapIndexed { i, n -> n.copy(id = "$newId-n$i") },
                            )
                            store.save(cloned)
                            // CX5: show snackbar feedback
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.link_duplicated) + ": " + cloned.name,
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        },
                        onRename = { wf, newName ->
                            store.save(wf.copy(name = newName))
                        },
                        onToggleFav = { wf ->
                            store.save(wf.copy(fav = !wf.fav))
                        },
                        onImport = onImport,
                        onBatchDelete = { wfs ->
                            wfs.forEach { store.delete(it.id) }
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.batch_deleted, wfs.size),
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        },
                        onBatchExport = onBatchExport,
                        onEditTags = { wf, tags ->
                            store.save(wf.copy(tags = tags))
                        },
                        onSeeAllTemplates = { tab = Tab.Templates },
                        twoColumn = expanded,
                    )
                    Tab.Templates -> TemplatesScreen(
                        onCreate = { tplId -> overlay = Overlay.Editor(store.create(tplId)) },
                        twoColumn = expanded,
                    )
                    Tab.Profile -> ProfileScreen(theme, onThemeChange, onClearData = {
                        store.clearAllData()
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.data_reset),
                                duration = SnackbarDuration.Short,
                            )
                        }
                    })
                }
            }
            }
        }
    }

    if (expanded) {
        // Large screens / desktop: navigation rail on the left
        Row(Modifier.fillMaxSize()) {
            NavigationRail(containerColor = p.surf) {
                Spacer(Modifier.height(12.dp))
                Tab.entries.forEach { t ->
                    val label = stringResource(t.labelRes)
                    NavigationRailItem(
                        selected = tab == t && overlay == null,
                        onClick = { tab = t; overlay = null },
                        icon = { Icon(t.icon, label) },
                        label = { Text(label) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = p.prim, selectedTextColor = p.prim,
                            indicatorColor = p.primTint,
                            unselectedIconColor = p.txt3, unselectedTextColor = p.txt3,
                        ),
                    )
                }
            }
            VerticalDivider(color = p.line2)
            Scaffold(
                containerColor = p.bg,
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { pad ->
                content(Modifier.fillMaxSize().padding(pad))
            }
        }
    } else {
        // Phones: bottom navigation bar (hidden when an overlay screen is open)
        Scaffold(
            containerColor = p.bg,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (overlay == null) {
                    NavigationBar(containerColor = p.surf) {
                        Tab.entries.forEach { t ->
                            val label = stringResource(t.labelRes)
                            NavigationBarItem(
                                selected = tab == t,
                                onClick = { tab = t; overlay = null },
                                icon = { Icon(t.icon, label) },
                                label = { Text(label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = p.prim, selectedTextColor = p.prim,
                                    indicatorColor = p.primTint,
                                    unselectedIconColor = p.txt3, unselectedTextColor = p.txt3,
                                ),
                            )
                        }
                    }
                }
            },
        ) { pad ->
            content(Modifier.fillMaxSize().padding(pad))
        }
    }
}
