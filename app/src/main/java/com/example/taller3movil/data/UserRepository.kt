package com.example.taller3movil.data

import com.example.taller3movil.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class UserRepository {
    private val usersReference = FirebaseDatabase.getInstance().getReference("users")

    fun createUser(user: User): Task<Void> {
        return usersReference.child(user.uid).setValue(user)
    }

    fun getUser(uid: String): Task<DataSnapshot> {
        return usersReference.child(uid).get()
    }

    fun updateUserProfile(uid: String, name: String, phone: String): Task<Void> {
        val updates = mapOf(
            "name" to name,
            "phone" to phone
        )

        return usersReference.child(uid).updateChildren(updates)
    }
}
