package com.example.shebetar.DataBase

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.shebetar.Classes.Post.Post
import com.example.shebetar.Classes.User.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset


@SuppressLint("StaticFieldLeak")

//Database initialization
val db = Firebase.firestore
val MODEL: String
= android.os.Build.MODEL

// Write data to the database
suspend fun addUser(user: User, context: Context){
    // Defining a query to find the last added user
    val query = db.collection("users").orderBy("dateOfJoin", Query.Direction.DESCENDING).limit(1)

    // Executing the query and getting the results
    val querySnapshot: QuerySnapshot = query.get().await()

    // Getting the last user from the results
    val lastDocument: DocumentSnapshot? = querySnapshot.documents.first()

    // Getting the data from the last document
    val data = lastDocument?.data

    // Do something with the data
    Log.d("DataBaseAddUserMethod","Last added document: $data")
    user.id = data?.get("id").toString().toLong() + 1
    db.collection("users").document(user.id.toString())
        .set(user.toMap())
        .addOnSuccessListener {
            Log.d("DataBaseAddUserMethod", "User added with id: ${user.id}")
        }
        .addOnFailureListener { e ->
            Log.w("DataBaseAddUserMethod", "Error adding user", e)
        }
        writeDataToJson(user, context, fileName = "LoginedUser")
}

suspend fun updateUser(user: User){
    db.collection("users").document(user.id.toString()).update(user.toMap())
        .addOnSuccessListener{
            Log.d("DataBaseUpdateUserMethod", "success")
        }
        .addOnFailureListener{
            Log.d("DataBaseUpdateUserMethod", "failure")
        }
        .await()
}

suspend fun getUserById(userId: Any?): DocumentSnapshot? {
    val query = db.collection("users").document(userId.toString())
        .get()
        .addOnSuccessListener {
            Log.d("DataBaseUserGetMethod", "found")
        }
        .addOnFailureListener { Log.d("DataBaseUserGetMethod", "not found")}
        .await()
    return query
}

suspend fun getUserByPost(postId: Int): User {
    val query = db.collection("posts").document(postId.toString()).get()
        .addOnSuccessListener {
            Log.d("getUserByPost", "Post found")
        }
        .addOnFailureListener{
            Log.d("getUserByPost", "Post not found")
        }
        .await()
    val userId = query.get("authorId")
    val user = User()
    user.toUserFromDocumentSnapshot(getUserById(userId))
    return user
}

/*
suspend fun getUserByDevice(): User {
    val query = db.collection("LoginedDevices").document(MODEL)
        .get()
        .addOnSuccessListener {
            Log.d("DataBaseUserGetMethod", "found")
        }
        .addOnFailureListener { Log.d("DataBaseUserGetMethod", "not found")}
        .await()
    Log.d("QueryGetUserMethod", query.toString())
    val id = query.get("userId")
    Log.d("IDGetUserMethod", id.toString())
    val user = User()
    user.toUserFromDocumentSnapshot(getUserById(id))
    return user
}
*/

suspend fun getUserByEmail(context: Context, email: String, password: String): User{
    val query = db.collection("users").whereEqualTo("email", email)
        .whereEqualTo("password", password)
        .get()
        .addOnSuccessListener { Log.d("GetUserByEmail", "User found") }
        .addOnFailureListener { Log.d("GetUserByEmail", "User not found") }
        .await()
    val user = User()
    user.toUserFromQuerySnapshot(query)
    Log.d("GetUserByEmail", user.toString())
    writeDataToJson(user, context, fileName = "LoginedUser")
    return user
}
suspend fun deleteUser(user: User){
    db.collection("users").document(user.id.toString()).delete()
        .addOnSuccessListener {
            Log.d("DataBaseDeleteUserMethod", "user deleted")
        }
        .addOnFailureListener{
            Log.d("DataBaseDeleteUserMethod", "user is not deleted")
        }
        .await()
}

suspend fun isDeviceLogined(): Boolean {
    // Defining a query to check if device has already logined
    val query = db.collection("LoginedDevices").whereEqualTo("name", MODEL)
        .get()
        .addOnSuccessListener {
            Log.d("DataBaseIsDeviceLoginedMethod", "found")
        }
        .addOnFailureListener { Log.d("DataBaseIsDeviceLoginedMethod", "not found")}
        .await()
    Log.d("Query", query.isEmpty.toString())
    // if query is empty -> device is not logined
    return !query.isEmpty
}

fun loginDevice(user: User){
    val device: HashMap<String, String> = hashMapOf(
        "name" to MODEL,
        "userId" to user.id.toString()
    )

    // Adding new device to the database of logined devices
    db.collection("LoginedDevices").document(MODEL).set(device).addOnSuccessListener {
        Log.d("DataBaseLoginDeviceMethod", "Device logined")
    }
        .addOnFailureListener { e ->
            Log.w("DataBaseLoginDeviceMethod", "Error adding device", e)
        }
}

fun logoutDevice(){
    db.collection("LoginedDevices").document(MODEL).delete().addOnSuccessListener {
        Log.d("DataBaseLogoutDeviceMethod", "Device logined")
    }
        .addOnFailureListener { e ->
            Log.w("DataBaseLogoutDeviceMethod", "Error adding device", e)
        }
}

suspend fun createPostDB(post: Post){
    val query = db.collection("posts")
        .orderBy("dateOfPublication", Query.Direction.DESCENDING).limit(1)
        .get()
        .addOnSuccessListener {
            Log.d("createPost", "Previous post id got")
        }
        .addOnFailureListener {
            Log.d("createPost", "Previous post id hasn't got")
        }
        .await()
    val data = query.documents.first()
    post.id = data.get("id").toString().toLong() + 1
    Log.d("createPost", post.id.toString())
    db.collection("posts").document(post.id.toString())
        .set(post.toMap())
        .addOnSuccessListener {
            Log.d("CreatePost", "Post has been created")
        }
        .addOnFailureListener {
            Log.d("CreatePost", "Post has not been created")
        }
}

suspend fun getPostDB(postId: Long): Post {
    val query = db.collection("posts").document(postId.toString())
        .get()
        .addOnSuccessListener{
            Log.d("getPostDB", "Post found")
        }
        .addOnFailureListener {
            Log.d("getPostDB", "Post not found")
        }
        .await()
    val post = Post()
    post.toPostFromDocumentSnapshot(query)
    return post
}

suspend fun getLastPostId(): Long {
    val query = db.collection("posts")
        .orderBy("dateOfPublication", Query.Direction.DESCENDING).limit(1)
        .get()
        .addOnSuccessListener {
            Log.d("getLastPostId", "Previous post id got")
        }
        .addOnFailureListener {
            Log.d("getLastPostId", "Previous post id hasn't got")
        }
        .await()
    val data = query.documents.first()
    return data.get("id").toString().toLong()
}

fun getPostsDB(): List<Post>{
    val posts: MutableList<Post> = mutableListOf()
    var lastId: Long = 1
    runBlocking{ launch{ lastId = getLastPostId() }}
    var currentId: Long = 1
    while(currentId <= lastId){
        var post = Post()
        runBlocking { launch { post = getPostDB(currentId) } }
        posts += post
        currentId++
    }
    Log.d("getPostsDB", posts.toString())
    return posts
}

suspend fun writeDataToJson(user: User, context: Context, fileName: String): String? {
    val gson = Gson()
    val jsonString = gson.toJson(user)
    val filesDir = context.filesDir
    val file = File(filesDir, fileName)
    if (!file.exists()) {
        withContext(Dispatchers.IO) {
            file.createNewFile()
        }
    }
    val fileOutputStream: FileOutputStream =
        context.openFileOutput(fileName, Context.MODE_PRIVATE)
    withContext(Dispatchers.IO) {
        fileOutputStream.write(jsonString.toByteArray(Charset.forName("UTF-8")))
    }
    Log.d("writeDataToJson", File(context.filesDir, fileName).readText())
    return jsonString
}

fun readUserDataFromJson(fileName: String, context: Context): User? {
    val jsonString: String = File(context.filesDir, fileName).readText()
    val gson = Gson()
    return gson.fromJson(jsonString, User::class.java)
}
