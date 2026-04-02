package io.github.badgersmc.advancements.domain

data class TreeDef(
    val namespace: String,
    val backgroundTexture: String,
    val nodes: List<AdvancementNodeDef>
) {

    val root: AdvancementNodeDef
        get() = nodes.single { it.parentKey == null }

    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        val keys = nodes.map { it.key }.toSet()

        // REQ-TREE-04: Single root enforcement
        val roots = nodes.filter { it.parentKey == null }
        if (roots.size != 1) {
            errors.add("Tree '$namespace' has ${roots.size} root nodes, expected exactly 1")
        }

        // REQ-TREE-05: Parent reference validation
        for (node in nodes) {
            if (node.parentKey != null && node.parentKey !in keys) {
                errors.add("Node '${node.key}' references parent '${node.parentKey}' which does not exist in tree '$namespace'")
            }
        }

        // REQ-TREE-06: Cycle detection (only if parent refs are valid)
        if (errors.none { "does not exist" in it }) {
            val visited = mutableSetOf<String>()
            val inStack = mutableSetOf<String>()
            val childrenMap = nodes.groupBy { it.parentKey }

            fun dfs(key: String): Boolean {
                if (key in inStack) return true
                if (key in visited) return false
                visited.add(key)
                inStack.add(key)
                for (child in childrenMap[key].orEmpty()) {
                    if (dfs(child.key)) return true
                }
                inStack.remove(key)
                return false
            }

            for (node in roots) {
                if (dfs(node.key)) {
                    errors.add("Tree '$namespace' contains a cycle")
                    break
                }
            }

            // Nodes not reachable from roots indicate a cycle among non-root nodes
            val unreached = nodes.filter { it.key !in visited }
            if (unreached.isNotEmpty() && roots.size == 1) {
                errors.add("Tree '$namespace' contains a cycle involving nodes: ${unreached.map { it.key }}")
            }
        }

        return errors
    }

    fun sortedNodes(): List<AdvancementNodeDef> {
        // REQ-TREE-07: Kahn's algorithm topological sort
        val nodeMap = nodes.associateBy { it.key }
        val inDegree = nodes.associate { it.key to if (it.parentKey == null) 0 else 1 }.toMutableMap()
        val children = mutableMapOf<String, MutableList<String>>()

        for (node in nodes) {
            if (node.parentKey != null) {
                children.getOrPut(node.parentKey) { mutableListOf() }.add(node.key)
            }
        }

        val queue = ArrayDeque<String>()
        for ((key, degree) in inDegree) {
            if (degree == 0) queue.add(key)
        }

        val result = mutableListOf<AdvancementNodeDef>()
        while (queue.isNotEmpty()) {
            val key = queue.removeFirst()
            result.add(nodeMap[key]!!)
            for (childKey in children[key].orEmpty()) {
                inDegree[childKey] = inDegree[childKey]!! - 1
                if (inDegree[childKey] == 0) queue.add(childKey)
            }
        }

        return result
    }
}
