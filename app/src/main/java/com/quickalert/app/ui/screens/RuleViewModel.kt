package com.quickalert.app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quickalert.app.QuickAlertApp
import com.quickalert.app.data.AlertRule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RuleViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = (application as QuickAlertApp).database.alertRuleDao()

    val rules = dao.getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleEnabled(rule: AlertRule) {
        viewModelScope.launch {
            dao.setEnabled(rule.id, !rule.enabled)
        }
    }

    fun deleteRule(rule: AlertRule) {
        viewModelScope.launch {
            dao.delete(rule)
        }
    }

    fun getRuleById(id: Long, onResult: (AlertRule?) -> Unit) {
        viewModelScope.launch {
            val rule = dao.getRuleById(id)
            onResult(rule)
        }
    }

    fun saveRule(rule: AlertRule, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (rule.id == 0L) {
                dao.insert(rule)
            } else {
                dao.update(rule)
            }
            onComplete()
        }
    }
}
