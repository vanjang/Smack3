package com.cochipcho.smack3.Controller

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cochipcho.smack3.R
import com.cochipcho.smack3.Services.AuthService
import com.cochipcho.smack3.Services.UserDataService
import com.cochipcho.smack3.Utilities.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlin.random.Random

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        createSpinner.isVisible = false
    }

    fun generateUserAvatar(view: View) {
        val random = Random
        val color = random.nextInt(2)
        val avatar = random.nextInt(28)

        if (color == 0) {
            userAvatar = "light$avatar"
        } else {
            userAvatar ="dark$avatar"
        }

        val resourceId = resources.getIdentifier(userAvatar, "drawable", packageName)
        createAvatarImageView.setImageResource(resourceId)
    }

    fun generateColorButton(view: View) {
        val random = Random
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        createAvatarImageView.setBackgroundColor(Color.rgb(r,g,b))

        val savedR = r.toDouble() / 255
        val savedG = g.toDouble() / 255
        val savedB = b.toDouble() / 255

        avatarColor = "[$savedR, $savedG, $savedB, 1]"
        println(avatarColor)
    }

    fun createUserClicked(view: View) {
        enableSpinner(true)
        val username = createUsernameText.text.toString()
        val email = createEmailText.text.toString()
        val password = createPasswordText.text.toString()
        if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            AuthService.registerUser(this, email, password) {
                if (it) {
                    AuthService.loginUser(this, email, password) {
                        if (it) {
                            AuthService.createUser(this, username, email, userAvatar, avatarColor) {
                                if (it) {
                                    val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                    LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)

                                    enableSpinner(false)
                                    finish()
                                } else {
                                    errorToast()
                                }
                            }
                        } else {
                            errorToast()
                        }
                    }
                } else {
                    Toast.makeText(this, "Make sure username, email, and password are filled in.", Toast.LENGTH_LONG).show()
                    enableSpinner(false)
                }
            }
        }
    }

    fun errorToast() {
        Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun enableSpinner(enable: Boolean) {
        if (enable) {
            createSpinner.visibility = View.VISIBLE
        } else {
            createSpinner.visibility = View.INVISIBLE
        }
        createUserButton.isEnabled = !enable
        createAvatarImageView.isEnabled = !enable
        backgroundColorButton.isEnabled = !enable
    }


}
