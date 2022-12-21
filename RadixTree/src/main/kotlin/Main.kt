
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

    val serialized = filterTree.serialize()
    val filterTreeCopy = FilterTree().apply{
        deserialize(serialized)
    }
    val reSerialized = filterTreeCopy.serialize()
    println("serialized  : ${serialized.substring(0..100)}")
    println("reSerialized: ${reSerialized.substring(0..100)}")
    println("same: ${reSerialized == serialized}")

//    val serialized = filterTree.serialize()
//    val reSerialized = FilterTree().apply{
//        deserialize(serialized)
//    }.serialize()
//    println("same: ${serialized == reSerialized}")
//
//    File("blocklist").bufferedWriter().use{ writer ->
//        writer.write(filterTree.serialize())
//    }


//    val tree = FilterTree().apply{
//        for((filter, exceptions) in test){
//            val exceptionTree = FilterTree().apply{
//                for(exception in exceptions){
//                    addFilter(exception)
//                }
//            }
//            addFilter(filter, exceptionTree)
//        }
//    }
//    tree.print()
//    println("root id is ${tree.id}")
//    val serialized = tree.serialize()
//    val copy = FilterTree().apply{
//        println("root id is $id")
//        deserialize(serialized)
//    }
//    val reSerialized = copy.serialize()
//    println("-".repeat(50))
//    println("-".repeat(50))
//    println("b: $serialized")
//    println("a: $reSerialized")
//    println("same: ${serialized == reSerialized}")
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
    "google.ru" to arrayOf("google.ru", "google.tu", "google.au"),
    "google.tu" to arrayOf("google.tu", "google.tu", "google.au"),
    "google.au" to arrayOf("google.au", "google.tu", "google.au"),
    "ads.google.ru" to arrayOf("ads.google.ru", "google.tu", "google.au"),
    "ads.google.au" to arrayOf("ads.google.au", "google.tu", "google.au"),
    "gooogle.au" to arrayOf("gooogle.au", "google.tu", "google.au"),
    "gooogle.ru" to arrayOf("gooogle.ru", "google.tu", "google.au")
)