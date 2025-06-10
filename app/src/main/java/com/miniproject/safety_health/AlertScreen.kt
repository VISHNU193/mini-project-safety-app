//package com.miniproject.safety_health
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.Dialog
//import androidx.lifecycle.viewmodel.compose.viewModel
//
//// Data class for emergency contacts
//data class EmergencyContact(
//    val id: String = "",
//    val name: String = "",
//    val phone: String = "",
//    val email: String = "",
//    val relationship: String = ""
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AlertScreen(
//    viewModel: AlertViewModel = viewModel()
//) {
//    val contacts by viewModel.contacts
//    var showDialog by remember { mutableStateOf(false) }
//    var currentContact by remember { mutableStateOf(EmergencyContact()) }
//
//    // Fetch contacts when screen loads
//    LaunchedEffect(Unit) {
//        viewModel.loadContacts()
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Emergency Contacts") },
//                actions = {
//                    IconButton(onClick = {
//                        currentContact = EmergencyContact()
//                        showDialog = true
//                    }) {
//                        Icon(Icons.Default.Add, contentDescription = "Add Contact")
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(16.dp)
//        ) {
//            // Contact list
//            if (contacts.isEmpty()) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text("No emergency contacts added")
//                }
//            } else {
//                LazyColumn(
//                    modifier = Modifier.weight(1f)
//                ) {
//                    items(contacts) { contact ->
//                        ContactItem(
//                            contact = contact,
//                            onEdit = {
//                                currentContact = contact
//                                showDialog = true
//                            },
//                            onDelete = { viewModel.deleteContact(contact.id) }
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                    }
//                }
//            }
//
//            // Add/Edit Contact Dialog
//            if (showDialog) {
//                ContactEditDialog(
//                    contact = currentContact,
//                    onDismiss = { showDialog = false },
//                    onSave = { contact ->
//                        if (contact.id.isEmpty()) {
//                            viewModel.addContact(contact)
//                        } else {
//                            viewModel.updateContact(contact)
//                        }
//                        showDialog = false
//                    }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun ContactItem(
//    contact: EmergencyContact,
//    onEdit: () -> Unit,
//    onDelete: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onEdit() }
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = contact.name,
//                    style = MaterialTheme.typography.titleMedium
//                )
//                Row {
//                    IconButton(onClick = onEdit) {
//                        Icon(Icons.Default.Edit, "Edit")
//                    }
//                    IconButton(onClick = onDelete) {
//                        Icon(Icons.Default.Delete, "Delete")
//                    }
//                }
//            }
//            Text("Relationship: ${contact.relationship}")
//            Text("Phone: ${contact.phone}")
//            Text("Email: ${contact.email}")
//        }
//    }
//}
//
//@Composable
//fun ContactEditDialog(
//    contact: EmergencyContact,
//    onDismiss: () -> Unit,
//    onSave: (EmergencyContact) -> Unit
//) {
//    var name by remember { mutableStateOf(contact.name) }
//    var phone by remember { mutableStateOf(contact.phone) }
//    var email by remember { mutableStateOf(contact.email) }
//    var relationship by remember { mutableStateOf(contact.relationship) }
//
//    Dialog(onDismissRequest = onDismiss) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp)
//            ) {
//                Text(
//                    text = if (contact.id.isEmpty()) "Add Contact" else "Edit Contact",
//                    style = MaterialTheme.typography.titleLarge
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                OutlinedTextField(
//                    value = name,
//                    onValueChange = { name = it },
//                    label = { Text("Full Name") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                OutlinedTextField(
//                    value = relationship,
//                    onValueChange = { relationship = it },
//                    label = { Text("Relationship") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                OutlinedTextField(
//                    value = phone,
//                    onValueChange = { phone = it },
//                    label = { Text("Phone Number") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                OutlinedTextField(
//                    value = email,
//                    onValueChange = { email = it },
//                    label = { Text("Email") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    Button(onClick = onDismiss) {
//                        Text("Cancel")
//                    }
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Button(onClick = {
//                        onSave(
//                            contact.copy(
//                                name = name,
//                                phone = phone,
//                                email = email,
//                                relationship = relationship
//                            )
//                        )
//                    }) {
//                        Text("Save")
//                    }
//                }
//            }
//        }
//    }
//}


package com.miniproject.safety_health

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.util.regex.Pattern

// Data class remains the same
data class EmergencyContact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val relationship: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(
    viewModel: AlertViewModel = viewModel()
) {
    val contacts by viewModel.contacts.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var currentContact by remember { mutableStateOf(EmergencyContact()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Show errors in snackbar
    LaunchedEffect(error) {
        error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    // Fetch contacts when screen loads
    LaunchedEffect(Unit) {
        viewModel.loadContacts()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contacts") },
                actions = {
                    IconButton(onClick = {
                        currentContact = EmergencyContact()
                        showDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Contact")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Contact list
                    if (contacts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No emergency contacts added")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(contacts) { contact ->
                                ContactItem(
                                    contact = contact,
                                    onEdit = {
                                        currentContact = contact
                                        showDialog = true
                                    },
                                    onDelete = { viewModel.deleteContact(contact.id) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // Add/Edit Contact Dialog
            if (showDialog) {
                ContactEditDialog(
                    contact = currentContact,
                    onDismiss = { showDialog = false },
                    onSave = { contact ->
                        viewModel.validateAndSaveContact(contact)
                        showDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun ContactItem(
    contact: EmergencyContact,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
            Text("Relationship: ${contact.relationship}")
            Text("Phone: ${contact.phone}")
            Text("Email: ${contact.email}")
        }
    }
}

@Composable
fun ContactEditDialog(
    contact: EmergencyContact,
    onDismiss: () -> Unit,
    onSave: (EmergencyContact) -> Unit
) {
    var name by remember { mutableStateOf(contact.name) }
    var phone by remember { mutableStateOf(contact.phone) }
    var email by remember { mutableStateOf(contact.email) }
    var relationship by remember { mutableStateOf(contact.relationship) }

    // Validation states
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var relationshipError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (contact.id.isEmpty()) "Add Emergency Contact" else "Edit Contact",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text("Full Name *") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Relationship Field
                OutlinedTextField(
                    value = relationship,
                    onValueChange = {
                        relationship = it
                        if (relationshipError != null) relationshipError = null
                    },
                    label = { Text("Relationship *") },
                    isError = relationshipError != null,
                    supportingText = { relationshipError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Phone Field
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        if (phoneError != null) phoneError = null
                    },
                    label = { Text("Phone Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError != null,
                    supportingText = { phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text("Email *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        // Validate inputs
                        var isValid = true

                        if (name.isBlank()) {
                            nameError = "Name is required"
                            isValid = false
                        }

                        if (relationship.isBlank()) {
                            relationshipError = "Relationship is required"
                            isValid = false
                        }

                        if (phone.isBlank()) {
                            phoneError = "Phone number is required"
                            isValid = false
                        } else if (!isValidPhone(phone)) {
                            phoneError = "Invalid phone number format"
                            isValid = false
                        }

                        if (email.isBlank()) {
                            emailError = "Email is required"
                            isValid = false
                        } else if (!isValidEmail(email)) {
                            emailError = "Invalid email format"
                            isValid = false
                        }

                        if (isValid) {
                            onSave(
                                contact.copy(
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    email = email.trim(),
                                    relationship = relationship.trim()
                                )
                            )
                        }
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// Validation helpers
private fun isValidEmail(email: String): Boolean {
    val emailPattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
    return emailPattern.matcher(email).matches()
}

private fun isValidPhone(phone: String): Boolean {
    // Simple validation - at least 7 digits
    val digitsOnly = phone.filter { it.isDigit() }
    return digitsOnly.length >= 7
}
