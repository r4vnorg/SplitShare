package com.example.splitshare.ui.navigation

/**
 * Route names for the screens in the navigation model. S5+ are placeholders pending the design
 * doc's exact S-ID mapping for US4-7's screens. Add the rest as their stories are built.
 */
object Routes {
    const val LOGIN = "login"                     // S1
    const val CREATE_PROFILE = "create_profile"   // S2
    const val GROUP_LIST = "group_list"           // S4
    const val CREATE_GROUP = "create_group"       // S5 (placeholder)

    const val GROUP_ID_ARG = "groupId"
    const val GROUP_DETAIL = "group_detail/{$GROUP_ID_ARG}"       // S6 (placeholder)
    const val MANAGE_MEMBERS = "manage_members/{$GROUP_ID_ARG}"   // S7 (placeholder)

    fun groupDetail(groupId: String) = "group_detail/$groupId"
    fun manageMembers(groupId: String) = "manage_members/$groupId"
}
