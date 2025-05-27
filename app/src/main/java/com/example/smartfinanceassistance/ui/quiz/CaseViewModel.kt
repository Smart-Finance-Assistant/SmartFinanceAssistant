package com.example.smartfinanceassistance.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartfinanceassistance.data.db.AppDatabase
import com.example.smartfinanceassistance.data.db.CaseEntity
import com.example.smartfinanceassistance.data.repository.CaseRepository
import kotlinx.coroutines.launch

class CaseViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).caseDao()
    private val repository = CaseRepository(dao)

    private val _cases = MutableLiveData<List<CaseEntity>>()
    val cases: LiveData<List<CaseEntity>> get() = _cases

    fun loadCases(type: String) {
        viewModelScope.launch {
            _cases.value = repository.getCasesByType(type)
        }
    }
}
