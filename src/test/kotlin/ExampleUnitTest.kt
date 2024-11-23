import com.murphy.util.parseNode
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun regexMatchesTest() {
        val regex = Regex("[wW](\\d)?")
        val matches = regex.findAll("w7 is followed by W5 and w9, bu3t not w.")
        matches.forEach {
            println(it.groupValues[1])
        }
    }

    @Test
    fun buildStringTest() {
        val char: Char? = null
        println(buildString {
            append(char)
            append("123")
        })
    }

    @Test
    fun randomNamingTest() {
        var uCount = 0
        var lCount = 0
        var _count = 0
        var dCount = 0
        val lengthToCount = HashMap<Int, Int>()
        repeat(10000) {
            val randomString = "([^CLW2]{5,10}[ ]){2,3}".parseNode().randomNaming
            println(randomString)
            val length = randomString.length
            val count = lengthToCount[length] ?: 0
            lengthToCount[length] = count + 1
            randomString.forEach {
                when (it) {
                    '_' -> _count++
                    in '1'..'9' -> dCount++
                    in 'a'..'z' -> lCount++
                    in 'A'..'Z' -> uCount++
                }
            }
        }
        val total = (uCount + lCount + _count + dCount).toDouble()
        println("${uCount / total} ${lCount / total} ${dCount / total} ${_count / total}")
        println(lengthToCount)
    }
}