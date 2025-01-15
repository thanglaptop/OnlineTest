package hua.vinh.thang.currencyconverter.model

data class SuportedCode(
    val currencycode: String,
    val moneyunit: String
) {
    override fun toString(): String {
        return "$currencycode ($moneyunit)"
    }
}
