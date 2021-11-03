import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.lang.Integer.max
import java.lang.Integer.min
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files
import java.util.zip.GZIPInputStream
import kotlin.io.path.Path

data class InputEdge(val from: Int, val to: Int, val weight: Int)
data class InputGraph(val nodes: Int, val edges: List<InputEdge>, val minWeight: Int, val maxWeight: Int)

class Graph {
    companion object {
        private const val path = "./graphs/"

        val emptyGraph = InputGraph(INITIAL_SIZE, listOf(), 0, 0)

        private val urls = mapOf(
            "NY" to "https://www.diag.uniroma1.it//challenge9/data/USA-road-d/USA-road-d.NY.gr.gz",
            "USA" to "https://www.diag.uniroma1.it//challenge9/data/USA-road-d/USA-road-d.USA.gr.gz",
            "WEST" to "https://www.diag.uniroma1.it//challenge9/data/USA-road-d/USA-road-d.W.gr.gz"
        )

        private fun downloadGraph(name: String) {
            println("Downloading")
            val input = Channels.newChannel(URL(urls[name]!!).openStream())
            if (!Files.exists(Path(path + name))) {
                val output = FileOutputStream(path + name)
                output.channel.transferFrom(input, 0, Long.MAX_VALUE)
                input.close()
                output.close()
            }
            println("Finished")
        }

        private fun parseGrFile(file: String, gzipped: Boolean): InputGraph {
            val edges = mutableListOf<InputEdge>()
            val nodes = mutableSetOf<Int>()
            var minWeight = Int.MAX_VALUE
            var maxWeight = Int.MIN_VALUE

            val inputStream = if (gzipped) GZIPInputStream(FileInputStream(file)) else FileInputStream(file)

            InputStreamReader(inputStream).buffered().useLines {
                it.forEach { line ->
                    if (line.startsWith("a")) {
                        val (_, u, v, w) = line.split(' ')
                        val from = u.toInt()
                        val to = v.toInt()
                        val weight = w.toInt()
                        edges.add(InputEdge(from, to, weight))
                        nodes.add(from)
                        nodes.add(to)

                        minWeight = min(minWeight, weight)
                        maxWeight = max(maxWeight, weight)
                    }
                }
            }

            val nodesCount = nodes.size
            assert(nodesCount == nodes.maxOrNull()!!)
            assert(nodes.contains(0))

            return InputGraph(nodesCount, edges, minWeight, maxWeight)
        }

        fun getGraph(name: String): InputGraph {
            println("Getting graph")
            if (!Files.exists(Path(path + name))) {
                println("Couldn't find" + Path(path).toAbsolutePath() + "name")
                downloadGraph(name)
            }
            return parseGrFile(path + name, true).also { println("Got graph") }
        }
    }
}