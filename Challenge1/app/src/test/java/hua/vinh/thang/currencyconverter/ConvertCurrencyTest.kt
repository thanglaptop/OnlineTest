package hua.vinh.thang.currencyconverter

import junit.framework.TestCase.assertEquals
import org.junit.Test

class ConvertCurrencyTest {
    val rate = 25406.3029
    private fun convertCurrency(amount: kotlin.Double, rate: kotlin.Double): kotlin.Double {
        val converted = amount * rate
        return converted
    }

    @Test
    fun convertWithValidIntegerTest(){
        val amount = 1.0;
        val result = convertCurrency(amount, rate)
        assertEquals(25406.3029, result)
    }

    @Test
    fun convertWithValidDoubleTest(){
        val amount = 1.5;
        val rate = 25406.3029
        val result = convertCurrency(amount, rate)
        assertEquals(38109.45435, result)
    }

    @Test
    fun convertWithAmount0(){
        val amount = 0.0;
        val rate = 25406.3029
        val result = convertCurrency(amount, rate)
        assertEquals(0.0, result)
    }
}