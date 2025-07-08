package cn.vove7.andro_accessibility_api.demo.service

object AccessibilityHelper {
    private var service: AppAccessibilityService? = null

    fun setService(accessibilityService: AppAccessibilityService) {
        service = accessibilityService
    }

    fun getService(): AppAccessibilityService? {
        return service
    }
}