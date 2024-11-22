data class Expense(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val creator: String = "",
    val split: Boolean = false,
    val participants: List<String> = listOf(),
    val paidBy: String = "", // New field for who paid the expense
    val shares: List<Double> = listOf(), // List of shares if split
    val title: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val groupId: String = ""

)
