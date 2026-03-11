package com.ptc.demo;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TreeNodeControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void getTreeWhenCalledReturnsDefaultRootNode() throws Exception {
        mockMvc.perform(get("/api/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(0))
            .andExpect(jsonPath("$.name").value("root"))
            .andExpect(jsonPath("$.content").value("Start of the tree"))
            .andExpect(jsonPath("$.childrens").isArray());
    }

    @Test
    void createNodeWhenPayloadIsValidReturnsCreatedNode() throws Exception {
        mockMvc.perform(post("/api/tree")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "name": "node",
                      "content": "content",
                      "parentId": "0"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("node"))
            .andExpect(jsonPath("$.content").value("content"))
            .andExpect(jsonPath("$.parentId").value("0"));
    }

    @Test
    void createNodeWhenContentIsMissingReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tree")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "name": "Missing content"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(""));
    }

    @Test
    void createNodeWhenNameIsMissingReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tree")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "content": "Has content"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(""));
    }

    @Test
    void getNodeWhenNodeExistsReturnsNode() throws Exception {
        createNode("Node A", "Alpha", "0");

        mockMvc.perform(get("/api/tree").param("id", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Node A"))
            .andExpect(jsonPath("$.content").value("Alpha"));
    }

    @Test
    void getNodeWhenNodeDoesNotExistReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/tree").param("id", "-1"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
    }

    @Test
    void updateNodeWhenPayloadIsValidReturnsUpdatedNode() throws Exception {
        createNode("Original", "Before", "0");

        mockMvc.perform(put("/api/tree")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "id": "1",
                      "name": "Updated",
                      "content": "After"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Updated"))
            .andExpect(jsonPath("$.content").value("After"));
    }

    @Test
    void updateNodeWhenNodeDoesNotExistReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/tree")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "id": "-1",
                      "name": "Updated",
                      "content": "After"
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
    }

    @Test
    void updateNodeWhenIdIsMissingReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/tree")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "name": "Updated",
                      "content": "After"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(""));
    }
    
    @Test
    void updateNodeWhenNameIsMissingReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/tree")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "id": "1",
                      "content": "After"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(""));
    }
    
    @Test
    void updateNodeWhenContentIsMissingReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/tree")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "id": "1",
                      "name": "Updated",
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(""));
    }
    
    @Test
    void searchNodesWhenSearchMatchesReturnsMatchingIds() throws Exception {
        createNode("Readme", "Contains searchable text", "0");
        createNode("Notes", "No match here", "0");

        mockMvc.perform(get("/api/tree").param("search", "searchable"))
            .andExpect(status().isOk())
            .andExpect(content().json("[1]"));
    }

    @Test
    void deleteNodeWhenNodeIsDeletedTwiceReturnsOkThenNotFound() throws Exception {
        createNode("Disposable", "Delete me", "0");

        mockMvc.perform(delete("/api/tree").param("id", "1"))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/tree").param("id", "1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void reorganiseWhenNewParentIsValidMovesNodeUnderNewParent() throws Exception {
        createNode("Parent A", "A", "0");
        createNode("Parent B", "B", "0");
        createNode("Child", "C", "1");

        mockMvc.perform(post("/api/tree/reorganise")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "nodeId": "3",
                      "newParentId": "2"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/tree").param("id", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.parentId").value(2));

        mockMvc.perform(get("/api/tree").param("id", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.childrens[0].id").value(3))
            .andExpect(jsonPath("$.childrens[0].parentId").value(2));
    }

    @Test
    void getHelloWhenCalledReturnsGreeting() throws Exception {
        mockMvc.perform(get("/api/tree/hello"))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello, World!"));
    }

    private void createNode(String name, String content, String parentId) throws Exception {
        mockMvc.perform(post("/api/tree")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "name": "%s",
                      "content": "%s",
                      "parentId": "%s"
                    }
                    """.formatted(name, content, parentId)))
            .andExpect(status().isCreated());
    }
}
