//package com.miniproject.safety_health
//
//import androidx.compose.runtime.mutableStateOf
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//
//class AlertViewModel : ViewModel() {
//    private val db = FirebaseFirestore.getInstance()
//    private val contactsCollection = db.collection("emergency_contacts")
//
//    val contacts = mutableStateOf(emptyList<EmergencyContact>())
//
//    fun loadContacts() {
//        viewModelScope.launch {
//            try {
//                val snapshot = contactsCollection.get().await()
//                contacts.value = snapshot.documents.map { doc ->
//                    EmergencyContact(
//                        id = doc.id,
//                        name = doc.getString("name") ?: "",
//                        phone = doc.getString("phone") ?: "",
//                        email = doc.getString("email") ?: "",
//                        relationship = doc.getString("relationship") ?: ""
//                    )
//                }
//            } catch (e: Exception) {
//                // Handle error
//            }
//        }
//    }
//
//    fun addContact(contact: EmergencyContact) {
//        viewModelScope.launch {
//            try {
//                val data = hashMapOf(
//                    "name" to contact.name,
//                    "phone" to contact.phone,
//                    "email" to contact.email,
//                    "relationship" to contact.relationship
//                )
//                contactsCollection.add(data).await()
//                loadContacts() // Refresh list
//            } catch (e: Exception) {
//                // Handle error
//            }
//        }
//    }
//
//    fun updateContact(contact: EmergencyContact) {
//        viewModelScope.launch {
//            try {
//                val data = hashMapOf(
//                    "name" to contact.name,
//                    "phone" to contact.phone,
//                    "email" to contact.email,
//                    "relationship" to contact.relationship
//                )
//                contactsCollection.document(contact.id).set(data).await()
//                loadContacts() // Refresh list
//            } catch (e: Exception) {
//                // Handle error
//            }
//        }
//    }
//
//    fun deleteContact(contactId: String) {
//        viewModelScope.launch {
//            try {
//                contactsCollection.document(contactId).delete().await()
//                loadContacts() // Refresh list
//            } catch (e: Exception) {
//                // Handle error
//            }
//        }
//    }
//}




package com.miniproject.safety_health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AlertViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _contacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val contacts: StateFlow<List<EmergencyContact>> = _contacts.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Get current user's UID safely
    private fun currentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Get user-specific contacts collection
    private fun userContactsCollection() = currentUserId()?.let { uid ->
        db.collection("users").document(uid).collection("emergency_contacts")
    }

    fun loadContacts() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val collection = userContactsCollection()
                if (collection == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }

                val snapshot = collection.get().await()
                _contacts.value = snapshot.documents.mapNotNull { doc ->
                    EmergencyContact(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        phone = doc.getString("phone") ?: "",
                        email = doc.getString("email") ?: "",
                        relationship = doc.getString("relationship") ?: ""
                    )
                }
            } catch (e: Exception) {
                _error.value = "Failed to load contacts: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Central validation and save method
    fun validateAndSaveContact(contact: EmergencyContact) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val collection = userContactsCollection()
                if (collection == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }

                val data = hashMapOf(
                    "name" to contact.name,
                    "phone" to contact.phone,
                    "email" to contact.email,
                    "relationship" to contact.relationship
                )

                if (contact.id.isEmpty()) {
                    // Add new contact
                    collection.add(data).await()
                } else {
                    // Update existing contact
                    collection.document(contact.id).set(data).await()
                }

                loadContacts() // Refresh list
            } catch (e: Exception) {
                _error.value = "Failed to save contact: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val collection = userContactsCollection()
                if (collection == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }

                collection.document(contactId).delete().await()
                loadContacts() // Refresh list
            } catch (e: Exception) {
                _error.value = "Failed to delete contact: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}