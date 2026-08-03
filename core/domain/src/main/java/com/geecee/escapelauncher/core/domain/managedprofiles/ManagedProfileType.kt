package com.geecee.escapelauncher.core.domain.managedprofiles

sealed class ManagedProfileType {
    object PrivateSpace: ManagedProfileType()
    object WorkApps: ManagedProfileType()
}