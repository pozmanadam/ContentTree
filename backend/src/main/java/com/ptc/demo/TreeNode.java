package com.ptc.demo;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TreeNode {

    public TreeNode(String name, String content, String parentId) {
        this.name = name;
        this.content = content;
        try {
            this.parentId = Integer.parseInt(parentId);
        } catch (NumberFormatException e) {
            this.parentId = null;
        }
        this.childrens =  new ArrayList<>();
    }

    public TreeNode(String name, String content) {
        this(name, content, "");
    }

    private int id;
    private String name;
    private String content;
    private Integer parentId;
    
    private List<TreeNode> childrens;
}
