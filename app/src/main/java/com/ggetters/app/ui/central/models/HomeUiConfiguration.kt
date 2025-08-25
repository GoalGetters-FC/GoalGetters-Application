package com.ggetters.app.ui.central.models

data class HomeUiConfiguration(
    var appBarColor: AppbarTheme = AppbarTheme.WHITE,
    var appBarTitle: String = "GoalGetters",
    var appBarShown: Boolean = true,
) {
    companion object {
        private const val TAG = "HomeUiConfiguration"
    }
}