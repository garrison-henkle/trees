
import dev.henkle.trees.ad.Filter
import dev.henkle.trees.ad.FilterTree
import dev.henkle.trees.radix.ReversedRadixTree
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
fun main(){
    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    val jsonStream = Main::class.java.getResourceAsStream("/blocklist.json")
    val filterObjects = json.decodeFromStream<List<Filter>>(Main::class.java.getResourceAsStream("/blocklist.json"))
    jsonStream.close()

    val filters = filterObjects.map{ filter ->
        filter.trigger.urlFilter
            .substringAfterLast('?')
            .replace("\\", "") to
                filter.trigger.unlessDomain.map{ it.replace("*", "") }
    }

    val filterTree = FilterTree()
    var exceptionTree: FilterTree
    for((filter, exceptions) in filters){
        exceptionTree = FilterTree()
        exceptions.forEach { exception -> exceptionTree.addFilter(filter = exception, exceptions = null) }
        filterTree.addFilter(
            filter = filter,
            exceptions = exceptionTree
        )
    }

    filterTree.print()

    File("blocklist").bufferedWriter().use{ writer ->
        writer.write(filterTree.serialize())
    }

    println("ads.tiktok.com: ${filterTree.matchesFilter(url = "ads.tiktok.com", exact = false)}")
    println("ad.tiktok.com: ${filterTree.matchesFilter(url = "ad.tiktok.com", exact = false)}")
    println("analytics.tiktok.com: ${filterTree.matchesFilter(url = "analytics.tiktok.com", exact = false)}")
    println("ads-sg.tiktok.com: ${filterTree.matchesFilter(url = "ads-sg.tiktok.com", exact = false)}")
    println("tiktok.com: ${filterTree.matchesFilter(url = "tiktok.com", exact = false)}")
    println("pasta.tiktok.com: ${filterTree.matchesFilter(url = "pasta.tiktok.com", exact = false)}")
    println("trending.tiktok.com: ${filterTree.matchesFilter(url = "trending.tiktok.com", exact = false)}")
}

private fun getDomain(url: String): String{
    var slashCount = 0
    var lastSlashIndex = 0
    url.forEachIndexed{ i, char ->
        if(char == '/'){
            if(slashCount == 2){
                return url.substring(lastSlashIndex + 1, i)
            }
            lastSlashIndex = i
            slashCount += 1
        }
    }
    return if(slashCount == 2){
        url.substring(lastSlashIndex + 1)
    } else url
}

object Main




val test = arrayOf(
    "remains",
    "remain",
    "remainder",
    "remainders",
    "reps",
    "repeat",
    "repeats",
    "sredn",
    "asredn",
    "sredns",
    "sredne",
    "srednes",
    "remains",
    "remain",
    "remainder",
    "remainders",
    "reps",
    "repeat",
    "repeats",
    "sredn",
    "asredn",
    "sredns",
    "sredne",
    "srednes",
    "remains",
    "remain",
    "remainder",
    "remainders",
    "reps",
    "repeat",
    "repeats",
    "sredn",
    "asredn",
    "sredns",
    "sredne",
    "srednes",
    "remains",
    "remain",
    "remainder",
    "remainders",
    "reps",
    "repeat",
    "repeats",
    "sredn",
    "asredn",
    "sredns",
    "sredne",
    "srednes"
)