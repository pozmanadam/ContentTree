<script setup>
import { ref, onMounted, nextTick, h } from "vue"
import { VueFlow, useVueFlow } from "@vue-flow/core"
import axios from "axios"
import { useDialog } from './useDialog'
import { useLayout } from './useLayout'

const nodes = ref([])
const edges = ref([])
const selectedNode = ref(null)
const dialog = useDialog()
const { layout } = useLayout()
const { fitView } = useVueFlow()
const lastDirection = ref('TB')

const intersections = ref([])
const intersectionIds = ref([])

const draggedStartPosition = ref(null)

const {onNodeDragStart, onNodeClick, onPaneClick, onNodeDragStop, onNodeDrag,getIntersectingNodes, updateNode, onNodesChange, applyNodeChanges } = useVueFlow()

const API = (import.meta.env.VITE_API_URL ?? "http://localhost:8080/api").replace(/\/$/, "")

function convertTreeToFlow(root) {
  const flowNodes = []
  const flowEdges = []

  function traverse(node) {
    flowNodes.push({
      id: node.id.toString(),
      label: node.name,
      position: {
        x: 0,
        y: 0
      },
      data: {
        content: node.content
      }
    })

    if (node.parentId != null) {
      flowEdges.push({
        id: `e${node.parentId}-${node.id}`,
        source: node.parentId.toString(),
        target: node.id.toString()
      })
    }
    node.childrens?.forEach((child) => {
      traverse(child)
    })
  }

  traverse(root)

  return { flowNodes, flowEdges }
}

function dialogMsg(id) {
  return h(
    'span',
    {
      style: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '8px',
      },
    },
    [`Are you sure?`, h('br'), h('span', `[ELEMENT_ID: ${id}]`)],
  )
}

onNodesChange(async (changes) => {
  const nextChanges = []

  for (const change of changes) {
    if (change.type === 'remove') {
      const isConfirmed = await dialog.confirm(dialogMsg(change.id))

      if (isConfirmed) {
        nextChanges.push(change)
      }
    } else {
      nextChanges.push(change)
    }
  }

  applyNodeChanges(nextChanges)
})

async function loadTree() {
  const res = await axios.get(`${API}/tree`)

  const { flowNodes, flowEdges } = convertTreeToFlow(res.data)

  nodes.value = flowNodes
  edges.value = flowEdges

  await nextTick()
  layoutGraph(lastDirection.value)
}

onPaneClick((  ) => {
  if (selectedNode.value) {
    updateNode(selectedNode.value.id, { class: '' })
    selectedNode.value = null
  }
})

onNodeClick((event) => {
  if (selectedNode.value) {
    updateNode(selectedNode.value.id, { class: '' })
  }
  selectedNode.value = event.node
  updateNode(event.node.id, { class: 'selected' })
})

async function addNode() {
  if (selectedNode.value == null) {
    alert("Select a parent node first")
    return
  }
    const name = prompt("Node name")
    const content = prompt("Node content")

    if(!name || !content) {
      return
    }

    const res = await axios.post(`${API}/tree`, {
      "name": name,
      "content": content,
      "parentId": selectedNode.value.id
    })
    const newNode = res.data

    nodes.value.push({
      id: newNode.id.toString(),
      label: newNode.name,
      position: { x: 0, y: 0 },
      data: { content: newNode.content }
    })

    edges.value.push({
      id: `e${selectedNode.value.id}-${newNode.id}`,
      source: selectedNode.value.id.toString(),
      target: newNode.id.toString()
    })
    await nextTick()
    layoutGraph(lastDirection.value)

  }

async function deleteNodes() {
  if (!selectedNode.value) return

  if (!confirm("Delete node and all children?")) return

  await axios.delete(`${API}/tree`, { params: { id: parseInt(selectedNode.value.id) } })

  loadTree() 
}

onMounted(loadTree)

async function handleUpdate() {
  if (!selectedNode.value) return
  updateNode(selectedNode.value.id, { label: label.value, data: { content: content.value } })
  setTimeout(() => {
  axios.put(`${API}/tree`, {
    "id": selectedNode.value.id,
    "name": selectedNode.value.label,
    "content": selectedNode.value.data.content  
  })
  },1000)
}

async function handleSearch() {
  setTimeout(() => {
  if (search.value.trim() == "") {
        nodes.value.forEach((node) => {
        updateNode(node.id, { class: '' })
    })
  }
    else{
    let searchText = search.value
    axios.get(`${API}/tree`, { params: { search: searchText } }).then((res) => {
      const foundIds = res.data
      nodes.value.forEach((node) => {
          updateNode(node.id, { class: foundIds.includes(parseInt(node.id)) ? 'found' : 'notFound' })
      })
    })
  }
},1000)
}


onNodeDragStart(({ node: draggedNode }) => {
  console.log('Drag started:', draggedNode.id)
  draggedStartPosition.value = draggedNode.position
})

onNodeDrag(({ node: draggedNode }) => {
  intersections.value = getIntersectingNodes(draggedNode)
  intersectionIds.value = intersections.value.map((intersection) => intersection.id)

  for (const node of nodes.value) {
    const isIntersecting = intersectionIds.value.includes(node.id)

    updateNode(node.id, { class: isIntersecting ? 'intersecting' : '' })
  }
})

onNodeDragStop(({ node: draggedNode }) => {
  if (intersections.value.length == 1)  {
    let targetNode = intersections.value[0]

    axios.post(`${API}/tree/reorganise`, {
      nodeId: draggedNode.id,
      newParentId: targetNode.id
    }).then(() => {
      loadTree()
    })
  }

  intersections.value = []
  intersectionIds.value = []
  for (const node of nodes.value) {
    updateNode(node.id, { class: '' })
  }
})

async function layoutGraph(direction) {
  nodes.value = layout(nodes.value, edges.value, direction)
  lastDirection.value = direction

  nextTick(() => {
    fitView()
  })
}

</script>

<template>
  <div class="layout">

    <!-- LEFT: Tree panel -->
    <section class="tree-panel">
      <VueFlow
        v-model:nodes="nodes"
        v-model:edges="edges"
        :nodes-connectable="false"
        class="flow"
      />
    </section>


    <!-- RIGHT: Content panel -->
    <section class="content-panel">

      <!-- Actions -->
      <div class="toolbar">
        <button @click="addNode">New</button>
        <button @click="deleteNodes">Delete</button>
      </div>

      <h2>Content</h2>

      <div v-if="selectedNode" class="editor">
        <div class="field">
          <label for="label">Label</label>
          <input
            id="label"
            v-model="selectedNode.label"
            @input="handleUpdate"
          />
        </div>

        <div class="field">
          <label for="content">Content</label>
          <input
            id="content"
            v-model="selectedNode.data.content"
            @input="handleUpdate"
          />
        </div>
      </div>

      <div v-else>
        Select a node
      </div>

      <div class="field">
        <label for="search">Search in tree</label>
        <input id="search" @input="handleSearch" />
      </div>

      <!-- Layout controls -->
      <div class="layout-panel">
        <button @click="layoutGraph('LR')" title="Set horizontal layout">
          Horizontal
        </button>

        <button @click="layoutGraph('TB')" title="Set vertical layout">
          Vertical
        </button>
      </div>

    </section>

  </div>
</template>
