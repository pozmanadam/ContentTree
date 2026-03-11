package com.ptc.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class TreeNodeService {

    private TreeNode rootNode = new TreeNode("root", "Start of the thee");
    private final Map<Integer, TreeNode> nodes = new HashMap<>() {{ put(rootNode.getId(), rootNode); }};
    private int idCounter = 0;

    public TreeNode createNode(TreeNode node) {
        node.setId(++idCounter);
        nodes.put(node.getId(), node);
        if(node.getParentId() != null && nodes.containsKey(node.getParentId())) {
            nodes.get(node.getParentId()).getChildrens().add(node);
        }
        if(rootNode == null) {
            rootNode = node;
        }
        return node;
    }

    public TreeNode updateNode(TreeNode node, String name, String content) {

        node.setName(name);
        node.setContent(content);
         
        return node;
    }

    public TreeNode getNode(int id) {
        return nodes.get(id);
    }

    public Boolean delete(int id) {
        TreeNode node = nodes.get(id);
        if (node != null) {
            if (node.getParentId() != null) {
                nodes.get(node.getParentId()).getChildrens().remove(node);
            }
            for (TreeNode child : new ArrayList<>(node.getChildrens())) {
                delete(child.getId());
            }
            nodes.remove(id);
        } else{
            return false;
        }
        if(nodes.isEmpty()){
            nodes.put(rootNode.getId(), rootNode);
        }
        return true;
    }

    public TreeNode getRootNode() {
        return rootNode;
    }

    public List<Integer> searchNodes(String search) {
        return nodes.values().stream()
            .filter(node -> node.getName().contains(search) || node.getContent().contains(search))
            .map(TreeNode::getId)
            .toList();
    }

    public void reorganise(Integer nodeId, Integer newParentId) {
        TreeNode node = nodes.get(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("Node with id " + nodeId + " does not exist.");
        }

        if (newParentId != null) {
            TreeNode newParent = nodes.get(newParentId);
            if (newParent == null) {
                throw new IllegalArgumentException("New parent node with id " + newParentId + " does not exist.");
            }
            if(isDescendant( newParent,node)) {
                newParent.setParentId(node.getParentId());
                if(node.getParentId() != null) {
                    nodes.get(node.getParentId()).getChildrens().add(newParent);
                } else {
                    rootNode = newParent;
                }
            }
            if (node.getParentId() != null) {
                nodes.get(node.getParentId()).getChildrens().remove(node);
            }
            newParent.getChildrens().add(node);
            node.setParentId(newParentId);
        } else {
            nodes.get(node.getParentId()).getChildrens().remove(node);
            node.setParentId(null);
        }
    }

    public boolean isDescendant(TreeNode node, TreeNode ancestor) {
    for (TreeNode child : ancestor.getChildrens()) {
        if (child.getId() == node.getId()) {
            ancestor.getChildrens().remove(node);

            return true;
        }

        if (isDescendant( node, child)) {
            return true;
        }
    }

    return false;
}
}
