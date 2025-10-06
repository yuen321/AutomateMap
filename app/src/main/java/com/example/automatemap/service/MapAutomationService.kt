package com.example.automatemap.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.automatemap.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapAutomationService: AccessibilityService() {
    companion object AutomationConstants {
        const val LOG = "MapAutomationService"
        const val ACTION_AUTOMATION_COMPLETE = "AutomationCompleted"
        const val PLACE_DETAIL = "PlaceDetail"

        const val SEARCH_BAR_TEXT = "Search here"
        const val SEARCH_QUERY = "KL Eco City"
        const val SEARCH_RESULT_TEXT = "KL Eco City - Menara 2"
        const val PHOTO_TEXT = "Photo"
        // Text to find photo details
        const val PHOTO_DETAILS_TEXT1 = "Sekai"
        const val PHOTO_DETAILS_TEXT2 = "ago"

        @Volatile
        private var instance: MapAutomationService? = null
        fun getInstance(): MapAutomationService? = instance
    }
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var automationStep = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if(event?.packageName != MainViewModel.MAPS_PACKAGE){
            return
        }
        when(event.eventType){
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                coroutineScope.launch {
                    performAutomationStep()
                }
            }
            else -> {}
        }
    }

    override fun onInterrupt() {
        Log.d(LOG, "service interrupted")
        resetAutomationSteps()
    }

    fun startAutomation(){
        automationStep = 1
        Log.d(LOG, "automation started")
    }

    fun resetAutomationSteps(){
        automationStep = 0
    }

    private suspend fun performAutomationStep(){
        Log.d(LOG, "performing step $automationStep")
        when(automationStep){
            1 -> { //insert key words
                delay(1000)
                step1TapSearchButton()
            }
            2 -> { //tap on search bar
                delay(2000)
                step2InsertSearchQuery()
            }
            3 -> { //tap on search record
                delay(2000)
                step3TapSearchResult()
            }
            4-> {
                //tap on first photo
                delay(2000)
                step4TapFirstPhoto()
            }
            5-> {
                //get photo detail
                delay(2000)
                step5DisplayPhotoDetail()
            }
            else ->{ }
        }
    }

    private fun step1TapSearchButton(){
        findNodeByText(SEARCH_BAR_TEXT)?.let {
            it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            automationStep = 2
        }
    }

    private fun step2InsertSearchQuery(){
        findNodeContainingTextOrDesc(SEARCH_BAR_TEXT)?.let {
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, SEARCH_QUERY)
            it.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            automationStep = 3
        }
    }
    private fun step3TapSearchResult(){
        findNodeByText(SEARCH_RESULT_TEXT)?.let{
            it.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            automationStep = 4
        }
    }

    private fun step4TapFirstPhoto(){
        findNodeByText(PHOTO_TEXT)?.let {
            it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            automationStep = 5
        }
    }

    private fun step5DisplayPhotoDetail(){
        var photoDetail = ""
        findNodeByText(PHOTO_DETAILS_TEXT1)?.let {
            photoDetail += it.text
        }
        findNodeByText(PHOTO_DETAILS_TEXT2)?.let {
            photoDetail += it.text
        }

        // navigate back to previous page and display toast
        if(automationStep > 0){
            coroutineScope.launch {
                repeat(3){
                    delay(500)
                    performGlobalAction(GLOBAL_ACTION_BACK) // Exit full screen photo
                }
            }
            reportResult(photoDetail)
        }
        resetAutomationSteps()
    }

    private fun findNodeByText(text: String): AccessibilityNodeInfo? {
        return rootInActiveWindow
            ?.findAccessibilityNodeInfosByText(text)
            ?.firstOrNull()
    }

    private fun findNodeContainingTextOrDesc(text: String): AccessibilityNodeInfo? {
        val matches = mutableListOf<AccessibilityNodeInfo>()
        fun traverse(node: AccessibilityNodeInfo?) {
            if (node == null) return
            val nodeText = node.text?.toString() ?: ""
            val contentDesc = node.contentDescription?.toString() ?: ""

            if (nodeText.contains(text, ignoreCase = true) ||
                contentDesc.contains(text, ignoreCase = true)) {
                matches.add(node)
            }

            for (i in 0 until node.childCount) {
                traverse(node.getChild(i))
            }
        }

        traverse(rootInActiveWindow)
        return matches.firstOrNull()
    }

    private fun reportResult(message: String) {
        val intent = Intent(ACTION_AUTOMATION_COMPLETE).apply {
            putExtra(PLACE_DETAIL, message)
        }
        sendBroadcast(intent)
    }
}

