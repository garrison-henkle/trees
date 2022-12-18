
import dev.henkle.trees.ad.Filter
import dev.henkle.trees.radix.RadixTree
import dev.henkle.trees.radix.ReversedRadixTree
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.net.URL

@OptIn(ExperimentalSerializationApi::class)
fun main(){
    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    val jsonStream = Main::class.java.getResourceAsStream("/blocklist.json")
    val filterObjects = json.decodeFromStream<List<Filter>>(Main::class.java.getResourceAsStream("/blocklist.json"))
    jsonStream.close()

    val filters = filterObjects.map{
        it.trigger.urlFilter
            .substringAfterLast('?')
            .replace("\\", "")
    }

    val filterRegexes = filterObjects.map{ it.trigger.urlFilter.toRegex() }

    val tree = ReversedRadixTree()
    for(filter in filters){
        tree.addString(filter)
    }
//    tree.print()
//    println("serialized: ${tree.sprint()}")

    val allDomains = filters.map{ "https://${getDomain(it)}" }.toMutableList()
    val testUrls = mutableListOf<String>()
    repeat(5_500){
        val domain = allDomains.random()
        allDomains.remove(domain)
        testUrls.add(domain)
    }

    var start = System.nanoTime()
    var domain: String
    var treeBlockCount = 0
    for(url in testUrls){
        domain = getDomain(url)
        if(tree.exists(domain)){
            treeBlockCount += 1
        } else{
            println("tree: did not match url '$url' ($domain)")
        }
    }
    val treeElapsed = System.nanoTime() - start
    println("tree elapsed: ${treeElapsed / 1_000_000}ms")

    start = System.nanoTime()
    var regexBlockCount = 0
    outer@for(url in testUrls) {
        for(regex in filterRegexes){
            if(regex.matches(url)){
                regexBlockCount += 1
                continue@outer
            }
        }
        println("regex: did not match url '$url'")
    }
    val regexElapsed = System.nanoTime() - start
    println("regex elapsed: ${regexElapsed / 1_000_000}ms")
    println("tree blocked: $treeBlockCount")
    println("regex blocked: $regexBlockCount")
    println("radix tree was ${regexElapsed / treeElapsed}x faster")
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
