package com.example.splitshare.ui.navigation

/**
 * Route names for the screens in the navigation model. S5+ are placeholders pending the design
 * doc's exact S-ID mapping for US4-7's screens. Add the rest as their stories are built.
 */
object Routes {
    const val LOGIN = "login"                     // S1
    const val CREATE_PROFILE = "create_profile"   // S2
    const val VIEW_PROFILE = "view_profile"
    const val GROUP_LIST = "group_list"           // S4
    const val CREATE_GROUP = "create_group"       // S5 (placeholder)

    const val GROUP_ID_ARG = "groupId"
    const val GROUP_DETAIL = "group_detail/{$GROUP_ID_ARG}"       // S6 (placeholder)
    const val MANAGE_MEMBERS = "manage_members/{$GROUP_ID_ARG}"   // S7 (placeholder)

    /** Nested graph scoping AddExpenseViewModel across both add-expense steps (US6/US7). */
    const val ADD_EXPENSE_GRAPH = "add_expense/{$GROUP_ID_ARG}"
    const val ADD_EXPENSE_PAYER_AMOUNT = "payer_amount"     // S8 (placeholder)
    const val ADD_EXPENSE_SELECT_SPLIT = "select_split"     // S9 (placeholder)

    const val EXPENSE_ID_ARG = "expenseId"
    const val EXPENSE_DETAIL = "expense_detail/{$GROUP_ID_ARG}/{$EXPENSE_ID_ARG}"   // S10 (placeholder)

    fun groupDetail(groupId: String) = "group_detail/$groupId"
    fun manageMembers(groupId: String) = "manage_members/$groupId"
    fun addExpenseGraph(groupId: String) = "add_expense/$groupId"
    fun expenseDetail(groupId: String, expenseId: String) = "expense_detail/$groupId/$expenseId"
}
