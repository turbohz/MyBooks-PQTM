package edu.uoc.gruizto.mybooks.form

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner;

@RunWith(ParameterizedRobolectricTestRunner::class)
class PurchaseRequestTest(
        private val url: String,
        private val expected: Boolean)
{
    @Test
    fun testIsValidPurchase() {
        val actual = PurchaseRequest(url).isValid
        assertEquals(expected, actual)
    }

    companion object {
        private const val baseURL = "content://edu.uoc.gruizto.mybooks.fileprovider/web/form.html"
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{index}: isValid({0})={1}")
        fun data(): Iterable<Array<Any>> {

            // "buy" argument is the submit button, we don't care about that

            return listOf(
                    // All empty
                    arrayOf("$baseURL?name=&num=&date=&buy=Submit", false),
                    // Just one field set
                    arrayOf("$baseURL?name=Foo&num=&date=&buy=Submit", false),
                    arrayOf("$baseURL?name=&num=Bar&date=&buy=Submit", false),
                    arrayOf("$baseURL?name=&num=&date=Baz&buy=Submit", false),
                    // Two fields set
                    arrayOf("$baseURL?name=Foo&num=Bar&date=&buy=Submit", false),
                    arrayOf("$baseURL?name=Foo&num=&date=Baz&buy=Submit", false),
                    arrayOf("$baseURL?name=&num=Bar&date=Baz&buy=Submit", false),
                    // All fields set, form should be valid
                    arrayOf("$baseURL?name=Foo&num=Bar&date=Baz&buy=Submit", true)
            )
        }
    }
}