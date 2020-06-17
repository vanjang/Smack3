package com.cochipcho.smack3.Services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cochipcho.smack3.Controller.App
import com.cochipcho.smack3.Utilities.*
import org.json.JSONException
import org.json.JSONObject

object AuthService {

    fun registerUser(context: Context, email: String, password: String, complete: (Boolean) ->  Unit) {

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val registerRequest = object : StringRequest(Method.POST, URL_REGISTER, Response.Listener {
            println(it)
            complete(true)
        }, Response.ErrorListener {
            Log.d("Error", "Could not register user: $it")
            complete(false)
        }) {

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        App.prefs.requestQueue.add(registerRequest)
    }

    fun loginUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val loginRequest = object: JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener {

            try {
                App.prefs.userEmail = it.getString("user")
                App.prefs.authToken = it.getString("token")
                App.prefs.isLoggedIn = true
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "EXC: " + e.localizedMessage)
                complete(false)
            }

        }, Response.ErrorListener {
            Log.d("Error", "Could not login user: $it")
            complete(false)
        }) {

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        App.prefs.requestQueue.add(loginRequest)
    }

    fun createUser(context: Context, name: String, email: String, avatarName: String, avatarColor: String, complete: (Boolean) -> Unit) {

        val jsonBody = JSONObject()
        jsonBody.put("name", name)
        jsonBody.put("email", email)
        jsonBody.put("avatarName", avatarName)
        jsonBody.put("avatarColor", avatarColor)
        val requestBody = jsonBody.toString()

        val createRequest = object : JsonObjectRequest(Method.POST, URL_CREATE_USER, null, Response.Listener {
            try {
                UserDataService.name = it.getString("name")
                UserDataService.email = it.getString("email")
                UserDataService.avatarName = it.getString("avatarName")
                UserDataService.avatarColor = it.getString("avatarColor")
                UserDataService.id = it.getString("_id")
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "EXC" + e.localizedMessage)
                complete(false)
            }
        }, Response.ErrorListener {
            Log.d("Error", "Could not add user: $it")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }
        App.prefs.requestQueue.add(createRequest)
    }

    fun findUserByEmail(context: Context, complete: (Boolean) -> Unit) {
        val findUserRequest = object : JsonObjectRequest(Method.GET, "$URL_GET_USER${App.prefs.userEmail}", null, Response.Listener {

            try {
                UserDataService.name = it.getString("name")
                UserDataService.email = it.getString("email")
                UserDataService.avatarName = it.getString("avatarName")
                UserDataService.avatarColor = it.getString("avatarColor")
                UserDataService.id = it.getString("_id")

                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
                complete(true)

            } catch (e: JSONException) {
                Log.d("JSON", "EXC: " + e.localizedMessage)
            }

        }, Response.ErrorListener {
            Log.d("ERROR", "Could not find user.")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }
        App.prefs.requestQueue.add(findUserRequest)
    }



}