package com.digitaltalent.permissionhandler

interface PermissionCallback {
    fun permissionsDenied(permissions:List<String>)
    fun permissionsGranted(permissions:List<String>)
    fun shouldShowRequestPermissionRationale(permissions: List<String>)
}