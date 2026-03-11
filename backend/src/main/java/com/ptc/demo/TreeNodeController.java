package com.ptc.demo;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/tree")
@CrossOrigin(origins = "http://localhost:5173")
public class TreeNodeController {
    
    @Autowired
    private TreeNodeService service;

    public TreeNodeController(TreeNodeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TreeNode> createNode(@RequestBody Map<String, String> payload) {
        if(payload.get("name") == null || payload.get("content") == null) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(null);
        }

        var savedNode = service.createNode(new TreeNode(
            payload.get("name"),
            payload.get("content"),
            payload.get("parentId")
        ));

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(savedNode);   
         }

    @PutMapping
    public ResponseEntity<TreeNode> updateNode(@RequestBody Map<String, String> payload) {
        if (payload.get("id") != null && payload.get("name") != null && payload.get("content") != null) {
            var node = service.getNode(Integer.valueOf(payload.get("id")));
            if(node == null) {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(null);
            }
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(service.updateNode(node, payload.get("name"), payload.get("content")));
        }        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(null);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam int id) {
        return ResponseEntity.status(service.delete(id) ? HttpStatus.OK : HttpStatus.NOT_FOUND).build();
    }

    @GetMapping
    public ResponseEntity<TreeNode> getTree() {
        return new ResponseEntity<TreeNode>(service.getRootNode(), HttpStatus.OK);
    }

    @GetMapping(params = "id")
    public ResponseEntity<TreeNode> getNode(@RequestParam int id) {
        if(service.getNode(id) == null) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(null);
        }
        return new ResponseEntity<TreeNode>(service.getNode(id), HttpStatus.OK);
    }
    
    @GetMapping(params = "search")
    public ResponseEntity<List<Integer>> searchNodes(@RequestParam String search) {
        return ResponseEntity.status(HttpStatus.OK).body(service.searchNodes(search));
    }

    @PostMapping("/reorganise")
    public ResponseEntity<Void> postMethodName(@RequestBody Map<String, String> payload) {
        service.reorganise(Integer.valueOf(payload.get("nodeId")), Integer.valueOf(payload.get("newParentId")));
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    
    
    @GetMapping("/hello")
    public ResponseEntity<String> getHello() {
        return new ResponseEntity<String>("Hello, World!", HttpStatus.OK);
    }
}
