package com.ptc.demo;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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

    @org.springframework.beans.factory.annotation.Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void getTreeReturnsDefaultRootNode() throws Exception {
        mockMvc.perform(get("/api/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(0))
            .andExpect(jsonPath("$.name").value("root"))
            .andExpect(jsonPath("$.content").value("Start of the thee"))
            .andExpect(jsonPath("$.childrens").isArray());
    }

    @Test
    void createNodeReturnsCreatedNode() throws Exception {
        mockMvc.perform(post("/api/tree")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "name": "Chapter 1",
                      "content": "Intro",
                      "parentId": "0"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Chapter 1"))
            .andExpect(jsonPath("$.content").value("Intro"))
            .andExpect(jsonPath("$.parentId").value(0));
    }

    @Test
    void createNodeRequiresNameAndContent() throws Exception {
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
    void getNodeReturnsCreatedNode() throws Exception {
        createNode("Node A", "Alpha", "0");

        mockMvc.perform(get("/api/tree").param("id", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Node A"))
            .andExpect(jsonPath("$.content").value("Alpha"));
    }

    @Test
    void updateNodeReturnsUpdatedNode() throws Exception {
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
    void updateNodeReturnsNotFoundForMissingNode() throws Exception {
        mockMvc.perform(put("/api/tree")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "id": "999",
                      "name": "Updated",
                      "content": "After"
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
    }

    @Test
    void searchNodesReturnsMatchingIds() throws Exception {
        createNode("Readme", "Contains searchable text", "0");
        createNode("Notes", "No match here", "0");

        mockMvc.perform(get("/api/tree").param("search", "searchable"))
            .andExpect(status().isOk())
            .andExpect(content().json("[1]"));
    }

    @Test
    void deleteNodeReturnsOkThenNotFoundForSecondDelete() throws Exception {
        createNode("Disposable", "Delete me", "0");

        mockMvc.perform(delete("/api/tree").param("id", "1"))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/tree").param("id", "1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void reorganiseMovesNodeUnderNewParent() throws Exception {
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
    }

    @Test
    void helloEndpointReturnsGreeting() throws Exception {
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
