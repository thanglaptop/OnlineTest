package hua.vinh.thang.currencyconverter.model

data class ConversionRate(
    val last_update: String,
    val next_update: String,
    val base_code: String,
    val conversion_rates: Map<String, Double>
)
