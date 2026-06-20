package com.profilefinder.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.profilefinder.data.models.ProfileResult
import com.profilefinder.ui.ProfileSearchViewModel

enum class SearchTab { USERNAME, URL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSearchScreen(
    onBack: () -> Unit,
    viewModel: ProfileSearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(SearchTab.USERNAME) }
    val focusManager = LocalFocusManager.current

    fun doSearch() {
        focusManager.clearFocus()
        if (query.isBlank()) return
        when (selectedTab) {
            SearchTab.USERNAME -> viewModel.searchByUsername(query)
            SearchTab.URL -> viewModel.searchByUrl(query)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Search", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == SearchTab.USERNAME,
                    onClick = { selectedTab = SearchTab.USERNAME; viewModel.reset(); query = "" },
                    text = { Text("Username") },
                    icon = { Icon(Icons.Default.AlternateEmail, null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == SearchTab.URL,
                    onClick = { selectedTab = SearchTab.URL; viewModel.reset(); query = "" },
                    text = { Text("URL / Website") },
                    icon = { Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(Modifier.height(20.dp))

            // Search input
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        when (selectedTab) {
                            SearchTab.USERNAME -> "e.g. torvalds, spez"
                            SearchTab.URL -> "e.g. https://github.com/torvalds"
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        when (selectedTab) {
                            SearchTab.USERNAME -> Icons.Default.Person
                            SearchTab.URL -> Icons.Default.Link
                        }, null
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = ""; viewModel.reset() }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { doSearch() })
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { doSearch() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = query.isNotBlank() && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(10.dp))
                    Text("Searching...")
                } else {
                    Icon(Icons.Default.Search, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Search Public Profiles", style = MaterialTheme.typography.titleSmall)
                }
            }

            // Privacy note
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Text(
                    "Only public data from official APIs is retrieved",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Error
            state.error?.let { error ->
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(10.dp))
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Results
            if (state.results.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Found ${state.results.size} result(s) for \"${state.query}\"",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                state.results.forEach { profile ->
                    ProfileResultCard(profile = profile)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileResultCard(profile: ProfileResult) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (profile.avatarUrl != null) {
                    AsyncImage(
                        model = profile.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(52.dp)
                            .padding(2.dp)
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            profile.displayName ?: profile.username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (profile.verifiedBadge) {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text("@${profile.username}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AssistChip(
                    onClick = {},
                    label = { Text(profile.platform, fontSize = 11.sp) }
                )
            }

            // Bio
            if (!profile.bio.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(profile.bio, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Stats
            if (profile.publicStats.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    profile.publicStats.entries.take(3).forEach { (key, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(key, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Links
            if (profile.publicLinks.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                profile.publicLinks.take(3).forEach { link ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)) {
                        Icon(Icons.Default.Link, null, modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            link.take(50) + if (link.length > 50) "…" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, null, modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.width(4.dp))
                    Text(profile.sourceNote, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)
                }
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(profile.profileUrl))
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Profile")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
