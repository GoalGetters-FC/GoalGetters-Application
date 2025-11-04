// app/src/main/java/com/ggetters/app/ui/shared/extensions/UserExtensions.kt
package com.ggetters.app.ui.shared.extensions

import com.ggetters.app.data.model.User

fun User.getFullName(): String = "${this.name} ${this.surname}".trim()

